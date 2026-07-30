package gg.moonflower.etched.client.screen;

import gg.moonflower.etched.common.menu.SpeakerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * A speaker's volume, shown as a waveform: turning the slider up opens the waveform behind the glass, so
 * the control reads as level rather than needing to be described. The only text is the title and the
 * percentage.
 *
 * @author Jackson
 */
public class SpeakerScreen extends AbstractContainerScreen<SpeakerMenu> {

    private static final int DISPLAY_X = 8;
    private static final int DISPLAY_Y = 22;
    private static final int DISPLAY_H = 40;

    private VolumeSlider slider;

    public SpeakerScreen(SpeakerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 88;
    }

    @Override
    protected void init() {
        super.init();
        this.slider = this.addRenderableWidget(new VolumeSlider(this.leftPos + 8, this.topPos + 68, this.imageWidth - 16, 12,
                this.menu.getVolumePercent() / 100.0));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // The real volume arrives a tick after the screen opens (via the data slot), so keep the slider
        // in step with it whenever the player isn't actively dragging. Without this the slider always
        // showed 0 on open, and a stray click then set the speaker to 0.
        if (this.slider != null) {
            this.slider.syncFrom(this.menu.getVolumePercent() / 100.0);
        }
    }

    /**
     * @return The level the display should show: the slider's own value, so the waveform tracks a drag
     *         immediately instead of waiting for the server to echo it back
     */
    private double level() {
        return this.slider != null ? this.slider.level() : this.menu.getVolumePercent() / 100.0;
    }

    // AbstractContainerScreen consumes drags for item quick-crafting and never passes them to widgets, so
    // the slider could be clicked but not dragged. While it is held, the drag goes to it first.
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.slider != null && this.slider.held()) {
            return this.slider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, 8, 7, HiFiPanel.TEXT, false);
        Component percent = Component.literal(Math.round(this.level() * 100.0) + "%");
        g.drawString(this.font, percent, this.imageWidth - 8 - this.font.width(percent), 7, HiFiPanel.DIM, false);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        HiFiPanel.panel(g, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        HiFiPanel.waveform(g, this.leftPos + DISPLAY_X, this.topPos + DISPLAY_Y, this.imageWidth - 16, DISPLAY_H, this.level());
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

        double level() {
            return this.value;
        }

        boolean held() {
            return this.dragging;
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

        // Kept for narration and the hover tooltip; the value is shown by the display, not on the track.
        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable("container.etched.speaker.volume", Math.round(this.value * 100.0)));
        }

        @Override
        protected void applyValue() {
            SpeakerScreen.this.minecraft.gameMode.handleInventoryButtonClick(
                    SpeakerScreen.this.menu.containerId, (int) Math.round(this.value * 100.0));
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            HiFiPanel.slider(g, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.value);
        }
    }
}
