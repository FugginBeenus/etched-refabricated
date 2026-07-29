package gg.moonflower.etched.client.screen;

import gg.moonflower.etched.common.menu.SpeakerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * A single volume slider for a speaker.
 *
 * @author Jackson
 */
public class SpeakerScreen extends AbstractContainerScreen<SpeakerMenu> {

    private static final int PANEL = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_DARK = 0xFF555555;

    public SpeakerScreen(SpeakerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 60;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    private VolumeSlider slider;

    @Override
    protected void init() {
        super.init();
        this.slider = this.addRenderableWidget(new VolumeSlider(this.leftPos + 8, this.topPos + 26, this.imageWidth - 16, 20,
                this.menu.getVolumePercent() / 100.0));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // The real volume arrives a tick after the screen opens (via the data slot), so keep the
        // slider in step with it whenever the player isn't actively dragging. Without this the slider
        // always showed 0 on open, and a stray click then set the speaker to 0.
        if (this.slider != null) {
            this.slider.syncFrom(this.menu.getVolumePercent() / 100.0);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        g.fill(x, y, x + this.imageWidth, y + this.imageHeight, PANEL);
        g.fill(x, y, x + this.imageWidth - 1, y + 1, PANEL_LIGHT);
        g.fill(x, y, x + 1, y + this.imageHeight - 1, PANEL_LIGHT);
        g.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, PANEL_DARK);
        g.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, PANEL_DARK);
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

    private class VolumeSlider extends AbstractSliderButton {

        private boolean dragging;

        VolumeSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Component.empty(), value);
            this.updateMessage();
        }

        // Adopt the synced volume unless the player is currently moving the slider.
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
            this.setMessage(Component.translatable("container.etched.speaker.volume", Math.round(this.value * 100.0)));
        }

        @Override
        protected void applyValue() {
            SpeakerScreen.this.minecraft.gameMode.handleInventoryButtonClick(
                    SpeakerScreen.this.menu.containerId, (int) Math.round(this.value * 100.0));
        }
    }
}
