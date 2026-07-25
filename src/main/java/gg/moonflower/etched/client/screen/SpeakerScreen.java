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

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new VolumeSlider(this.leftPos + 8, this.topPos + 26, this.imageWidth - 16, 20,
                this.menu.getVolumePercent() / 100.0));
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

        VolumeSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Component.empty(), value);
            this.updateMessage();
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
