package gg.moonflower.etched.client.screen;

import gg.moonflower.etched.common.menu.RadioMenu;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ServerboundSetUrlPacket;
import gg.moonflower.etched.core.Etched;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RadioScreen extends AbstractContainerScreen<RadioMenu> {

    private static final ResourceLocation TEXTURE = gg.moonflower.etched.api.util.EtchedResourceLocation.of(Etched.MOD_ID, "textures/gui/radio.png");

    private static final int ROW_H = 12;
    private static final int MAX_ROWS = 6;

    private boolean canEdit;
    private EditBox url;

    // Stations the server says exist. The dropdown is drawn by hand: vanilla has no such widget, and a
    // cycling button hides how many there are.
    private java.util.List<gg.moonflower.etched.common.network.play.ClientboundStationListPacket.Entry> stations = java.util.List.of();
    private int selected = -1;
    private boolean dropdownOpen;
    private Button stationButton;

    public void setStations(java.util.List<gg.moonflower.etched.common.network.play.ClientboundStationListPacket.Entry> stations,
                            boolean tuned, net.minecraft.core.BlockPos tunedTo) {
        this.stations = stations;
        this.selected = -1;
        if (tuned) {
            for (int i = 0; i < stations.size(); i++) {
                if (stations.get(i).pos().equals(tunedTo)) {
                    this.selected = i;
                    break;
                }
            }
        }
        this.updateStationButton();
    }

    private Component stationLabel() {
        if (this.selected < 0 || this.selected >= this.stations.size()) {
            return Component.translatable("container." + Etched.MOD_ID + ".radio.no_station");
        }
        return Component.literal(this.stations.get(this.selected).name());
    }

    private void updateStationButton() {
        if (this.stationButton != null) {
            this.stationButton.setMessage(this.stationLabel());
        }
    }

    private int dropdownX() {
        return (this.width - this.imageWidth) / 2;
    }

    private int dropdownY() {
        return (this.height - this.imageHeight) / 2 + this.imageHeight + 27;
    }

    private int dropdownRows() {
        return Math.min(this.stations.size() + 1, MAX_ROWS);
    }

    // -1 is the "no station" row, which is always first so a radio can always be freed again.
    private void choose(int index) {
        this.selected = index;
        this.dropdownOpen = false;
        this.updateStationButton();
        boolean tuned = index >= 0 && index < this.stations.size();
        new gg.moonflower.etched.common.network.play.ServerboundTuneRadioPacket(
                tuned, tuned ? this.stations.get(index).pos() : net.minecraft.core.BlockPos.ZERO).sendToServer();
    }

    public RadioScreen(RadioMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageHeight = 39;
    }

    @Override
    protected void init() {
        super.init();
        this.url = new EditBox(this.font, this.leftPos + 10, this.topPos + 21, 154, 16, this.url, Component.translatable("container." + Etched.MOD_ID + ".radio.url"));
        this.url.setTextColor(-1);
        this.url.setTextColorUneditable(-1);
        this.url.setBordered(false);
        this.url.setMaxLength(32500);
        this.url.setVisible(this.canEdit);
        this.url.setCanLoseFocus(false);
        this.addRenderableWidget(this.url);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            var packet = new ServerboundSetUrlPacket(this.url.getValue());
            packet.sendToServer();
            this.minecraft.setScreen(null);
        }).bounds((this.width - this.imageWidth) / 2, (this.height - this.imageHeight) / 2 + this.imageHeight + 5, this.imageWidth, 20).build());

        // A link typed by hand still works; the dropdown is another way to fill the same radio.
        this.stationButton = this.addRenderableWidget(Button.builder(this.stationLabel(), button ->
                this.dropdownOpen = !this.dropdownOpen)
                .bounds(this.dropdownX(), (this.height - this.imageHeight) / 2 + this.imageHeight + 27, this.imageWidth, 20).build());
    }

    @Override
    public void containerTick() {
        //? if <1.21 {
        this.url.tick();
        //?}
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //? if >=1.21 {
        /*this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        *///?} else {
        this.renderBackground(guiGraphics);
        //?}
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Drawn after everything else so the open list sits over the buttons beneath it.
        if (this.dropdownOpen) {
            int x = this.dropdownX();
            int y = this.dropdownY() + 20;
            int rows = this.dropdownRows();
            guiGraphics.fill(x, y, x + this.imageWidth, y + rows * ROW_H + 2, 0xFF1A1A1A);
            guiGraphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + rows * ROW_H + 1, 0xFFC6C4C0);
            for (int i = 0; i < rows; i++) {
                int rowY = y + 1 + i * ROW_H;
                boolean hovered = mouseX >= x && mouseX < x + this.imageWidth && mouseY >= rowY && mouseY < rowY + ROW_H;
                if (hovered) {
                    guiGraphics.fill(x + 1, rowY, x + this.imageWidth - 1, rowY + ROW_H, 0xFF8B8987);
                }
                Component label = i == 0
                        ? Component.translatable("container." + Etched.MOD_ID + ".radio.no_station")
                        : Component.literal(this.stations.get(i - 1).name());
                guiGraphics.drawString(this.font, label, x + 5, rowY + 2, 0xFF404040, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.dropdownOpen) {
            int x = this.dropdownX();
            int y = this.dropdownY() + 20;
            int rows = this.dropdownRows();
            if (mouseX >= x && mouseX < x + this.imageWidth && mouseY >= y && mouseY < y + rows * ROW_H + 2) {
                int row = (int) ((mouseY - y - 1) / ROW_H);
                if (row >= 0 && row < rows) {
                    this.choose(row - 1);
                }
                return true;
            }
            this.dropdownOpen = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 18, 0, this.canEdit ? 39 : 53, 160, 14);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        return this.url.keyPressed(i, j, k) || (this.url.isFocused() && this.url.isVisible() && i != 256) || super.keyPressed(i, j, k);
    }

    public void receiveUrl(String url) {
        this.canEdit = true;
        this.url.setVisible(true);
        this.url.setValue(url);
        this.setFocused(this.url);
        this.url.setFocused(true);
    }
}
