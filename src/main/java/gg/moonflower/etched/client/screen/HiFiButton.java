package gg.moonflower.etched.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class HiFiButton extends Button {

    public HiFiButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        HiFiPanel.button(g, Minecraft.getInstance().font, this.getX(), this.getY(), this.getWidth(), this.getHeight(),
                this.getMessage(), this.isHoveredOrFocused(), this.active);
    }
}
