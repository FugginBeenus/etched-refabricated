package gg.moonflower.etched.client.screen;

import gg.moonflower.etched.common.blockentity.AlbumCrateBlockEntity;
import gg.moonflower.etched.common.menu.AlbumCrateMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AlbumCrateScreen extends AbstractContainerScreen<AlbumCrateMenu> {

    public AlbumCrateScreen(AlbumCrateMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 143;
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

        for (int i = 0; i < AlbumCrateBlockEntity.SLOTS; i++) {
            HiFiPanel.slot(g, x + AlbumCrateMenu.SLOT_X + (i % AlbumCrateBlockEntity.COLUMNS) * 18,
                    y + AlbumCrateMenu.SLOT_Y + (i / AlbumCrateBlockEntity.COLUMNS) * 18);
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                HiFiPanel.slot(g, x + AlbumCrateMenu.INVENTORY_X + col * 18, y + AlbumCrateMenu.INVENTORY_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            HiFiPanel.slot(g, x + AlbumCrateMenu.INVENTORY_X + col * 18, y + AlbumCrateMenu.HOTBAR_Y);
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
