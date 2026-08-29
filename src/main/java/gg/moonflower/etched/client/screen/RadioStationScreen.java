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

    private static final int NAME_X = 7;
    private static final int NAME_Y = 16;
    private static final int NAME_W = 162;
    private static final int NAME_H = 16;

    private EditBox nameField;

    public RadioStationScreen(RadioStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 161;
    }

    @Override
    protected void init() {
        super.init();
        this.nameField = new EditBox(this.font, this.leftPos + NAME_X + 4, this.topPos + NAME_Y + 4, NAME_W - 8, 8, Component.empty());
        this.nameField.setMaxLength(ServerboundSetStationNamePacket.MAX_LENGTH);
        this.nameField.setBordered(false);
        // Lit, not dark. The old colour was panel-grey text on a dark field and could barely be read.
        this.nameField.setTextColor(HiFiPanel.TEAL_HI);
        this.nameField.setTextColorUneditable(HiFiPanel.TEAL);
        // The client cannot reach the block through the menu, but the title it was opened with is the
        // station's display name. Anything other than the default means it has been named.
        String opened = this.title.getString();
        String fallback = Component.translatable("container.etched.radio_station").getString();
        this.nameField.setValue(opened.equals(fallback) ? "" : opened);
        this.nameField.setResponder(value -> new ServerboundSetStationNamePacket(value).sendToServer());
        this.addWidget(this.nameField);
    }

    // The name is a field the player edits, so the screen still needs its own heading above it.
    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, Component.translatable("container.etched.radio_station"), 8, 6, HiFiPanel.TEXT, false);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        HiFiPanel.panel(g, x, y, this.imageWidth, this.imageHeight);

        // The name reads as something you type, so it sits in a recessed lit field.
        HiFiPanel.glass(g, x + NAME_X, y + NAME_Y, NAME_W, NAME_H);

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
