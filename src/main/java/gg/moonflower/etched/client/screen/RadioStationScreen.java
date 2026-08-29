package gg.moonflower.etched.client.screen;

import gg.moonflower.etched.common.blockentity.RadioStationBlockEntity;
import gg.moonflower.etched.common.menu.RadioStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RadioStationScreen extends AbstractContainerScreen<RadioStationMenu> {

    public RadioStationScreen(RadioStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 145;
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, 8, 7, HiFiPanel.TEXT, false);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        HiFiPanel.panel(g, x, y, this.imageWidth, this.imageHeight);

        for (int i = 0; i < RadioStationBlockEntity.SLOTS; i++) {
            HiFiPanel.slot(g, x + RadioStationMenu.SLOT_X + i * 18, y + RadioStationMenu.SLOT_Y);
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                HiFiPanel.slot(g, x + RadioStationMenu.INVENTORY_X + col * 18, y + RadioStationMenu.INVENTORY_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            HiFiPanel.slot(g, x + RadioStationMenu.INVENTORY_X + col * 18, y + RadioStationMenu.HOTBAR_Y);
        }
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
}
