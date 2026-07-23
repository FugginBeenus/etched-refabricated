package gg.moonflower.etched.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import gg.moonflower.etched.client.render.item.AlbumCoverItemRenderer;
import gg.moonflower.etched.client.render.item.AlbumImageProcessor;
import gg.moonflower.etched.client.render.item.CoverImageUtil;
import gg.moonflower.etched.client.render.item.CoverPatterns;
import gg.moonflower.etched.common.menu.AlbumPrinterMenu;
import gg.moonflower.etched.common.network.play.SetCoverArtPacket;
import gg.moonflower.etched.core.Etched;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;

/**
 * Album Printer screen, drawn procedurally (no texture; block/GUI art comes later). Inputs on the
 * left (cover, image-upload, dye), a scrollable procedural-pattern picker in the middle, result on
 * the right.
 */
public class AlbumPrinterScreen extends AbstractContainerScreen<AlbumPrinterMenu> {

    private static final int PANEL = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_DARK = 0xFF555555;
    private static final int SLOT_BORDER = 0xFF373737;
    private static final int SLOT_FILL = 0xFF8B8B8B;
    private static final int SELECTED = 0xFFFFFFFF;
    private static final int HOVER = 0x60FFFFFF;

    // Left-column input positions (item top-left, relative to the gui origin).
    private static final int COVER_X = 13, COVER_Y = 26;
    private static final int FOLDER_X = 33, FOLDER_Y = 26;
    private static final int DYE_X = 13, DYE_Y = 47;
    private static final int RESULT_X = 150, RESULT_Y = 35;

    private static final int GRID_X = 56;
    private static final int GRID_Y = 18;
    private static final int GRID_COLS = 4;
    private static final int GRID_ROWS = 2;
    private static final int CELL = 18;
    private static final int SCROLLBAR_X = GRID_X + GRID_COLS * CELL + 1;
    private static final int SCROLLBAR_W = 6;
    private static final int SCROLLBAR_H = GRID_ROWS * CELL;

    private static final int PREVIEW_SIZE = 16;
    private static final int NO_DYE_PREVIEW_COLOR = 0x555555;
    private final ResourceLocation[] previewIds = new ResourceLocation[CoverPatterns.COUNT];
    private final DynamicTexture[] previews = new DynamicTexture[CoverPatterns.COUNT];
    private int previewColor = Integer.MIN_VALUE;
    private int scrollRow;

    public AlbumPrinterScreen(AlbumPrinterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelX = 5;
        this.titleLabelY = 4;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < CoverPatterns.COUNT; i++) {
            this.previewIds[i] = gg.moonflower.etched.api.util.EtchedResourceLocation.of(Etched.MOD_ID, "album_printer_preview_" + i);
        }
        this.rebuildPreviews(this.previewDyeColor());
    }

    // ---- geometry ----

    private int cellX(int col) {
        return this.leftPos + GRID_X + col * CELL;
    }

    private int cellY(int row) {
        return this.topPos + GRID_Y + row * CELL;
    }

    private int maxScroll() {
        int rows = (CoverPatterns.COUNT + GRID_COLS - 1) / GRID_COLS;
        return Math.max(0, rows - GRID_ROWS);
    }

    private int patternAt(double mouseX, double mouseY) {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = (this.scrollRow + row) * GRID_COLS + col;
                if (index >= CoverPatterns.COUNT) {
                    continue;
                }
                int cx = this.cellX(col);
                int cy = this.cellY(row);
                if (mouseX >= cx && mouseX < cx + CELL && mouseY >= cy && mouseY < cy + CELL) {
                    return index;
                }
            }
        }
        return -1;
    }

    private boolean inFolder(double mouseX, double mouseY) {
        int x = this.leftPos + FOLDER_X;
        int y = this.topPos + FOLDER_Y;
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }

    // ---- previews ----

    private int previewDyeColor() {
        return this.menu.getDyeStack().isEmpty() ? NO_DYE_PREVIEW_COLOR : AlbumPrinterMenu.dyeColor(this.menu.getDyeStack());
    }

    private void rebuildPreviews(int color) {
        this.previewColor = color;
        for (int i = 0; i < CoverPatterns.COUNT; i++) {
            if (this.previews[i] != null) {
                this.minecraft.getTextureManager().release(this.previewIds[i]);
            }
            DynamicTexture texture = new DynamicTexture(CoverPatterns.preview(i, color, PREVIEW_SIZE));
            this.minecraft.getTextureManager().register(this.previewIds[i], texture);
            this.previews[i] = texture;
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        int color = this.previewDyeColor();
        if (color != this.previewColor) {
            this.rebuildPreviews(color);
        }
    }

    @Override
    public void removed() {
        super.removed();
        for (int i = 0; i < CoverPatterns.COUNT; i++) {
            if (this.previews[i] != null) {
                this.minecraft.getTextureManager().release(this.previewIds[i]);
                this.previews[i] = null;
            }
        }
    }

    // ---- image upload ----

    private void openImagePicker() {
        Util.ioPool().execute(() -> {
            String path;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filters = stack.mallocPointer(5);
                filters.put(stack.UTF8("*.png"));
                filters.put(stack.UTF8("*.jpg"));
                filters.put(stack.UTF8("*.jpeg"));
                filters.put(stack.UTF8("*.bmp"));
                filters.put(stack.UTF8("*.gif"));
                filters.flip();
                path = TinyFileDialogs.tinyfd_openFileDialog("Select album art", "", filters, "Image files", false);
            }
            if (path == null) {
                return;
            }
            try {
                NativeImage raw = CoverImageUtil.readFile(Path.of(path));
                NativeImage processed = AlbumImageProcessor.apply(raw, AlbumCoverItemRenderer.getOverlayImage());
                byte[] bytes = CoverImageUtil.encodePng(processed);
                processed.close();
                Minecraft.getInstance().execute(() -> new SetCoverArtPacket(bytes).sendToServer());
            } catch (Exception e) {
                Etched.LOGGER.error("Failed to load album art from '{}'", path, e);
            }
        });
    }

    // ---- input ----

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int index = this.patternAt(mouseX, mouseY);
        if (index >= 0) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, index);
            this.playClick();
            return true;
        }
        if (this.inFolder(mouseX, mouseY)) {
            this.openImagePicker();
            this.playClick();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void playClick() {
        this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    //? if >=1.21 {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.maxScroll() > 0 && scrollY != 0) {
            this.scrollRow = Mth.clamp(this.scrollRow - (int) Math.signum(scrollY), 0, this.maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    *///?} else {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.maxScroll() > 0 && delta != 0) {
            this.scrollRow = Mth.clamp(this.scrollRow - (int) Math.signum(delta), 0, this.maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    //?}

    // ---- rendering ----

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        panel(g, x, y, this.imageWidth, this.imageHeight);

        slot(g, x + COVER_X, y + COVER_Y);
        slot(g, x + FOLDER_X, y + FOLDER_Y);
        folderIcon(g, x + FOLDER_X, y + FOLDER_Y);
        slot(g, x + DYE_X, y + DYE_Y);
        slot(g, x + RESULT_X, y + RESULT_Y);
        arrow(g, x + 136, y + RESULT_Y + 8);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slot(g, x + 8 + col * 18, y + 84 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            slot(g, x + 8 + col * 18, y + 142);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //? if >=1.21 {
        /*this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        *///?} else {
        this.renderBackground(guiGraphics);
        //?}
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderPatternGrid(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int hovered = this.patternAt(mouseX, mouseY);
        if (hovered >= 0) {
            guiGraphics.renderTooltip(this.font, Component.literal(CoverPatterns.NAMES[hovered]), mouseX, mouseY);
        } else if (this.inFolder(mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.literal("Upload image"), mouseX, mouseY);
        }
    }

    private void renderPatternGrid(GuiGraphics g, int mouseX, int mouseY) {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = (this.scrollRow + row) * GRID_COLS + col;
                if (index >= CoverPatterns.COUNT) {
                    continue;
                }
                int cx = this.cellX(col);
                int cy = this.cellY(row);
                slot(g, cx + 1, cy + 1);
                if (this.previews[index] != null) {
                    g.blit(this.previewIds[index], cx + 1, cy + 1, 0, 0, PREVIEW_SIZE, PREVIEW_SIZE, PREVIEW_SIZE, PREVIEW_SIZE);
                }
                if (this.menu.getMode() == AlbumPrinterMenu.MODE_PATTERN && index == this.menu.getSelectedPattern()) {
                    outline(g, cx, cy, CELL, CELL, SELECTED);
                }
                if (mouseX >= cx && mouseX < cx + CELL && mouseY >= cy && mouseY < cy + CELL) {
                    g.fill(cx + 1, cy + 1, cx + CELL - 1, cy + CELL - 1, HOVER);
                }
            }
        }

        if (this.maxScroll() > 0) {
            int trackX = this.leftPos + SCROLLBAR_X;
            int trackY = this.topPos + GRID_Y;
            g.fill(trackX, trackY, trackX + SCROLLBAR_W, trackY + SCROLLBAR_H, SLOT_BORDER);
            int thumbH = Math.max(8, SCROLLBAR_H / (this.maxScroll() + 1));
            int thumbY = trackY + (SCROLLBAR_H - thumbH) * this.scrollRow / this.maxScroll();
            thumbY = Mth.clamp(thumbY, trackY, trackY + SCROLLBAR_H - thumbH);
            g.fill(trackX + 1, thumbY + 1, trackX + SCROLLBAR_W - 1, thumbY + thumbH - 1, PANEL);
        }
    }

    private static void folderIcon(GuiGraphics g, int x, int y) {
        g.fill(x + 1, y + 3, x + 7, y + 5, 0xFFD8A840);   // tab
        g.fill(x + 1, y + 5, x + 15, y + 14, 0xFFD8A840); // body
        g.fill(x + 1, y + 5, x + 15, y + 6, 0xFFF0C860);  // top highlight
        g.fill(x + 1, y + 13, x + 15, y + 14, 0xFFA87C28); // bottom shade
    }

    private static void arrow(GuiGraphics g, int x, int y) {
        g.fill(x, y - 1, x + 4, y + 1, SLOT_FILL);
        for (int i = 0; i <= 4; i++) {
            g.fill(x + 4 + i, y - 4 + i, x + 5 + i, y + 5 - i, SLOT_FILL);
        }
    }

    private static void outline(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, PANEL);
        g.fill(x, y, x + w - 1, y + 1, PANEL_LIGHT);
        g.fill(x, y, x + 1, y + h - 1, PANEL_LIGHT);
        g.fill(x + w - 1, y, x + w, y + h, PANEL_DARK);
        g.fill(x, y + h - 1, x + w, y + h, PANEL_DARK);
    }

    // Vanilla-style recessed 18x18 slot; (x,y) is where the item is rendered.
    private static void slot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, PANEL_LIGHT);
        g.fill(x - 1, y - 1, x + 16, y + 16, SLOT_BORDER);
        g.fill(x, y, x + 16, y + 16, SLOT_FILL);
    }
}
