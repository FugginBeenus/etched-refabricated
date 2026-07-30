package gg.moonflower.etched.client.screen;

import gg.moonflower.etched.common.blockentity.StereoBlockEntity;
import gg.moonflower.etched.common.menu.StereoMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class StereoScreen extends AbstractContainerScreen<StereoMenu> {

    // The unit's top-left. StereoMenu.PREAMP_X is UNIT_X + 8 so the bays drawn on the top face sit
    // directly under the slots feeding them.
    private static final int UNIT_X = 58;
    private static final int UNIT_Y = 70;

    private HiFiButton modeButton;
    private MasterSlider masterSlider;

    public StereoScreen(StereoMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 198;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new HiFiButton(this.leftPos + 94, this.topPos + 5, 38, 14,
                Component.translatable("container.etched.stereo.link.short"), b ->
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, StereoMenu.BUTTON_LINK)));
        this.modeButton = this.addRenderableWidget(new HiFiButton(this.leftPos + 136, this.topPos + 5, 32, 14,
                this.modeText(), b ->
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, StereoMenu.BUTTON_MODE)));
        this.masterSlider = this.addRenderableWidget(new MasterSlider(this.leftPos + 8, this.topPos + 26, 140, 14,
                this.menu.getVolumePercent() / 100.0));
    }

    private Component modeText() {
        return Component.translatable(this.menu.getMode() == StereoBlockEntity.MODE_NEAREST
                ? "container.etched.stereo.mode.nearest.short"
                : "container.etched.stereo.mode.all.short");
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

    // Counted straight off the upgrade slots rather than from the synced stats, so pulling a part out
    // redraws the unit on the same frame instead of a tick later. Counting by item type also keeps the
    // picture right if a part ends up in the other kind of bay.
    private int countUpgrade(net.minecraft.world.item.Item upgrade) {
        int count = 0;
        for (int i = 0; i < StereoBlockEntity.UPGRADE_SLOTS; i++) {
            if (this.menu.slots.get(i).getItem().is(upgrade)) {
                count++;
            }
        }
        return count;
    }

    private int preamps() {
        return this.countUpgrade(gg.moonflower.etched.core.registry.EtchedItems.PREAMP.asItem());
    }

    private int transmitters() {
        return this.countUpgrade(gg.moonflower.etched.core.registry.EtchedItems.TRANSMITTER.asItem());
    }

    private boolean slotEmpty(int index) {
        return this.menu.slots.get(index).getItem().isEmpty();
    }

    // Only the title and the volume readout: the inventory grid needs no caption.
    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, 8, 9, HiFiPanel.TEXT, false);

        // Drawn small so the bar can run most of the panel's width; the bar is the control, the number is
        // just confirmation.
        double level = this.masterSlider != null ? this.masterSlider.level() : this.menu.getVolumePercent() / 100.0;
        Component percent = Component.literal(Math.round(level * 100.0) + "%");
        float scale = 0.75F;
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1.0F);
        int px = Math.round((this.imageWidth - 7 - this.font.width(percent) * scale) / scale);
        int py = Math.round(31 / scale);
        g.drawString(this.font, percent, px, py, HiFiPanel.DIM, false);
        g.pose().popPose();
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        HiFiPanel.panel(g, x, y, this.imageWidth, this.imageHeight);

        HiFiPanel.Anchors unit = HiFiPanel.stereoUnit(g, x + UNIT_X, y + UNIT_Y, this.preamps(), this.transmitters());

        // Upgrade slots, with a faint chip in the empty ones and a short leader line to the part of the
        // unit they feed.
        for (int i = 0; i < StereoMenu.PREAMP_SLOTS; i++) {
            int sx = x + StereoMenu.PREAMP_X + i * 22;
            int sy = y + StereoMenu.PREAMP_Y;
            HiFiPanel.slot(g, sx, sy);
            if (this.slotEmpty(i)) {
                HiFiPanel.ghostChip(g, sx, sy);
            }
            HiFiPanel.line(g, sx + 8, sy + 17, sx + 8, y + UNIT_Y + 2, HiFiPanel.SOFT);
        }
        for (int i = 0; i < StereoMenu.TRANSMITTER_SLOTS; i++) {
            int sx = x + StereoMenu.TRANSMITTER_X;
            int sy = y + StereoMenu.TRANSMITTER_Y + i * StereoMenu.TRANSMITTER_SPACING;
            HiFiPanel.slot(g, sx, sy);
            if (this.slotEmpty(StereoMenu.PREAMP_SLOTS + i)) {
                HiFiPanel.ghostChip(g, sx, sy);
            }
        }
        // No leader lines here: the dongles are drawn entering the back panel and the bays sit right
        // beside them, so a harness only added tangle over the top of the plugs.

        HiFiPanel.wirelessField(g, unit.leftX() - 2, unit.midY() + 4, this.transmitters(),
                this.menu.getPairedCount(), this.menu.getMaxSpeakers());

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                HiFiPanel.slot(g, x + 8 + col * 18, y + StereoMenu.INVENTORY_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            HiFiPanel.slot(g, x + 8 + col * 18, y + StereoMenu.HOTBAR_Y);
        }
    }

    // AbstractContainerScreen consumes drags for item quick-crafting and never passes them to widgets, so
    // the slider could be clicked but not dragged. While it is held, the drag goes to it first.
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.masterSlider != null && this.masterSlider.held()) {
            return this.masterSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    // The field draws the speakers and their reach; the exact numbers are here for anyone who wants them.
    private boolean overField(int mouseX, int mouseY) {
        int fx = this.leftPos + 8;
        int fy = this.topPos + UNIT_Y;
        return mouseX >= fx && mouseX < this.leftPos + UNIT_X && mouseY >= fy && mouseY < fy + 42;
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

        if (this.hoveredSlot == null && this.overField(mouseX, mouseY)) {
            g.renderComponentTooltip(this.font, List.of(
                    Component.translatable("container.etched.stereo.speakers",
                            this.menu.getPairedCount(), this.menu.getMaxSpeakers()),
                    Component.translatable("container.etched.stereo.range", this.menu.getRange())
            ), mouseX, mouseY);
        }
    }

    private class MasterSlider extends AbstractSliderButton {

        private boolean dragging;

        MasterSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Component.empty(), value);
            this.updateMessage();
        }

        double level() {
            return this.value;
        }

        boolean held() {
            return this.dragging;
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

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            HiFiPanel.slider(g, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.value);
        }
    }
}
