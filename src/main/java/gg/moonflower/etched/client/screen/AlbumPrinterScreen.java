package gg.moonflower.etched.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import gg.moonflower.etched.client.render.item.AlbumCoverItemRenderer;
import gg.moonflower.etched.client.render.item.AlbumImageProcessor;
import gg.moonflower.etched.client.render.item.CoverImageUtil;
import gg.moonflower.etched.common.menu.AlbumPrinterMenu;
import gg.moonflower.etched.common.network.play.SetCoverArtPacket;
import gg.moonflower.etched.core.Etched;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;

/**
 * Loom-style Album Printer screen. Reuses the vanilla loom background for now (slot layout matches);
 * the mode toggle, procedural-pattern picker and image "Browse" button are added with those modes.
 */
public class AlbumPrinterScreen extends AbstractContainerScreen<AlbumPrinterMenu> {

    //? if >=1.21 {
    /*private static final ResourceLocation BG = ResourceLocation.withDefaultNamespace("textures/gui/container/loom.png");
    *///?} else {
    private static final ResourceLocation BG = new ResourceLocation("textures/gui/container/loom.png");
    //?}

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
        this.addRenderableWidget(Button.builder(Component.literal("Browse Image…"), b -> this.openImagePicker())
                .bounds(this.leftPos + 59, this.topPos + 13, 100, 20).build());
    }

    // Opens a native file picker off-thread (it blocks), decodes + runs the image through the album
    // "compression" processor, then sends the processed bytes to the server to bake onto the cover.
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

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(BG, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //? if >=1.21 {
        /*this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        *///?} else {
        this.renderBackground(guiGraphics);
        //?}
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
