package gg.moonflower.etched.client.screen;

import gg.moonflower.etched.common.blockentity.StereoBlockEntity;
import gg.moonflower.etched.common.menu.StereoMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Settings for a stereo: upgrade slots, which speakers it drives, and how the record is spread across
 * them. Drawn in code like the album printer, until there are textures for it.
 *
 * @author Jackson
 */
public class StereoScreen extends AbstractContainerScreen<StereoMenu> {

    private static final int PANEL = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_DARK = 0xFF555555;
    private static final int SLOT_BORDER = 0xFF373737;
    private static final int SLOT_FILL = 0xFF8B8B8B;

    private Button modeButton;
    private MasterSlider masterSlider;

    public StereoScreen(StereoMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelX = 8;
        this.titleLabelY = -100;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.translatable("container.etched.stereo.link"), b ->
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, StereoMenu.BUTTON_LINK))
                .bounds(this.leftPos + 8, this.topPos + 56, 76, 18).build());
        this.modeButton = this.addRenderableWidget(Button.builder(this.modeText(), b ->
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, StereoMenu.BUTTON_MODE))
                .bounds(this.leftPos + 92, this.topPos + 56, 76, 18).build());
        this.masterSlider = this.addRenderableWidget(new MasterSlider(this.leftPos + 8, this.topPos + 4, 160, 14,
                this.menu.getVolumePercent() / 100.0));
    }

    private Component modeText() {
        return Component.translatable(this.menu.getMode() == StereoBlockEntity.MODE_NEAREST
                ? "container.etched.stereo.mode.nearest"
                : "container.etched.stereo.mode.all");
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.modeButton != null) {
            this.modeButton.setMessage(this.modeText());
        }
        // The real master volume arrives a tick after opening; keep the slider in step unless dragged.
        if (this.masterSlider != null) {
            this.masterSlider.syncFrom(this.menu.getVolumePercent() / 100.0);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        panel(g, x, y, this.imageWidth, this.imageHeight);

        for (int i = 0; i < StereoBlockEntity.UPGRADE_SLOTS; i++) {
            slot(g, x + 44 + i * 22, y + 34);
        }
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
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);
        Component stats = Component.translatable("container.etched.stereo.stats",
                this.menu.getPairedCount(), this.menu.getMaxSpeakers(), this.menu.getRange());
        g.drawString(this.font, stats, 8, 21, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        //? if >=1.21 {
        /*this.renderBackground(g, mouseX, mouseY, partialTick);
        *///?} else {
        this.renderBackground(g);
        //?}
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    private class MasterSlider extends AbstractSliderButton {

        private boolean dragging;

        MasterSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Component.empty(), value);
            this.updateMessage();
        }

        void syncFrom(double value) {
            if (!this.dragging && Math.abs(this.value - value) > 1.0E-4) {
                this.value = value;
                this.updateMessage();
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.dragging = true;
            super.onClick(mouseX, mouseY);
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            super.onRelease(mouseX, mouseY);
            this.dragging = false;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable("container.etched.stereo.volume", Math.round(this.value * 100.0)));
        }

        @Override
        protected void applyValue() {
            StereoScreen.this.minecraft.gameMode.handleInventoryButtonClick(
                    StereoScreen.this.menu.containerId, (int) Math.round(this.value * 100.0));
        }
    }

    private static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, PANEL);
        g.fill(x, y, x + w - 1, y + 1, PANEL_LIGHT);
        g.fill(x, y, x + 1, y + h - 1, PANEL_LIGHT);
        g.fill(x + w - 1, y, x + w, y + h, PANEL_DARK);
        g.fill(x, y + h - 1, x + w, y + h, PANEL_DARK);
    }

    private static void slot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, PANEL_LIGHT);
        g.fill(x - 1, y - 1, x + 16, y + 16, SLOT_BORDER);
        g.fill(x, y, x + 16, y + 16, SLOT_FILL);
    }
}
