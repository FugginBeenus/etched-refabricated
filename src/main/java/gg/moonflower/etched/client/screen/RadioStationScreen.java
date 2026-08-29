package gg.moonflower.etched.client.screen;

import gg.moonflower.etched.common.blockentity.RadioStationBlockEntity;
import gg.moonflower.etched.common.menu.RadioStationMenu;
import gg.moonflower.etched.common.network.play.ServerboundSetStationNamePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RadioStationScreen extends AbstractContainerScreen<RadioStationMenu> {

    private EditBox nameField;

    public RadioStationScreen(RadioStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 193;
    }

    @Override
    protected void init() {
        super.init();
        this.nameField = new EditBox(this.font, this.leftPos + 9, this.topPos + 17, 158, 12, Component.empty());
        this.nameField.setMaxLength(ServerboundSetStationNamePacket.MAX_LENGTH);
        this.nameField.setBordered(false);
        this.nameField.setTextColor(HiFiPanel.TEXT);
        this.nameField.setValue(this.title.getString());
        this.nameField.setResponder(value -> new ServerboundSetStationNamePacket(value).sendToServer());
        this.addWidget(this.nameField);
    }

    // The title is drawn inside the name field, so only the inventory caption is left here.
    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        HiFiPanel.panel(g, x, y, this.imageWidth, this.imageHeight);

        // The name reads as something you type, so it sits in a recessed field rather than as a caption.
        HiFiPanel.glass(g, x + 7, y + 15, 162, 16);

        for (int i = 0; i < RadioStationBlockEntity.SLOTS; i++) {
            HiFiPanel.slot(g, x + RadioStationMenu.SLOT_X + (i % RadioStationMenu.COLUMNS) * 18,
                    y + RadioStationMenu.SLOT_Y + (i / RadioStationMenu.COLUMNS) * 18);
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
        this.nameField.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    // Typing in the name must not be read as an inventory hotkey, or pressing "e" would shut the screen.
    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (this.nameField.isFocused() && key != 256) {
            return this.nameField.keyPressed(key, scanCode, modifiers) || this.nameField.canConsumeInput();
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (this.nameField.isFocused()) {
            return this.nameField.charTyped(c, modifiers);
        }
        return super.charTyped(c, modifiers);
    }
}
