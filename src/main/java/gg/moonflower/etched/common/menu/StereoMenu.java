package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.common.blockentity.StereoBlockEntity;
import gg.moonflower.etched.core.registry.EtchedItems;
import gg.moonflower.etched.core.registry.EtchedMenus;
import net.minecraft.world.item.Item;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Upgrade slots and settings for a {@link StereoBlockEntity}. The stereo's stats are sent through data
 * slots so the screen can show what the installed upgrades bought.
 *
 * @author Jackson
 */
public class StereoMenu extends AbstractContainerMenu {

    /** Turns on link mode and closes the screen so speakers can be clicked. */
    public static final int BUTTON_LINK = 200;
    /** Switches between playing from all speakers and only the nearest. */
    public static final int BUTTON_MODE = 201;
    // Ids 0-100 set the master volume as a percent.

    /** Preamps come first in the container, then transmitters. */
    public static final int PREAMP_SLOTS = 2;
    public static final int TRANSMITTER_SLOTS = StereoBlockEntity.UPGRADE_SLOTS - PREAMP_SLOTS;

    // Slot positions, shared with the screen so its drawing of the unit lines up with them.
    // PREAMP_X matches the screen's UNIT_X + 8, so each slot sits directly over the bay it feeds.
    public static final int PREAMP_X = 66;
    public static final int PREAMP_Y = 48;
    // The transmitter bays stack vertically behind the unit, clear of the dongles drawn plugging into it.
    public static final int TRANSMITTER_X = 134;
    public static final int TRANSMITTER_Y = 70;
    public static final int TRANSMITTER_SPACING = 20;
    public static final int INVENTORY_Y = 118;
    public static final int HOTBAR_Y = 176;

    private final Container container;
    private final DataSlot mode = DataSlot.standalone();
    private final DataSlot maxSpeakers = DataSlot.standalone();
    private final DataSlot range = DataSlot.standalone();
    private final DataSlot paired = DataSlot.standalone();
    private final DataSlot volume = DataSlot.standalone();

    public StereoMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(StereoBlockEntity.UPGRADE_SLOTS));
    }

    public StereoMenu(int id, Inventory inventory, Container container) {
        super(EtchedMenus.STEREO_MENU, id);
        checkContainerSize(container, StereoBlockEntity.UPGRADE_SLOTS);
        this.container = container;
        container.startOpen(inventory.player);

        // The upgrade slots sit where the hardware physically goes on the screen's drawing of the unit:
        // preamps seat on the top face, transmitters plug into the back panel. Each slot only takes its
        // own kind, so the drawing always matches what is installed.
        for (int i = 0; i < PREAMP_SLOTS; i++) {
            this.addSlot(new UpgradeSlot(container, i, PREAMP_X + i * 22, PREAMP_Y,
                    EtchedItems.PREAMP.asItem()));
        }
        for (int i = 0; i < TRANSMITTER_SLOTS; i++) {
            this.addSlot(new UpgradeSlot(container, PREAMP_SLOTS + i, TRANSMITTER_X, TRANSMITTER_Y + i * TRANSMITTER_SPACING,
                    EtchedItems.TRANSMITTER.asItem()));
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, HOTBAR_Y));
        }

        this.addDataSlot(this.mode);
        this.addDataSlot(this.maxSpeakers);
        this.addDataSlot(this.range);
        this.addDataSlot(this.paired);
        this.addDataSlot(this.volume);
        this.refreshStats();
    }

    private StereoBlockEntity stereo() {
        return this.container instanceof StereoBlockEntity blockEntity ? blockEntity : null;
    }

    private void refreshStats() {
        StereoBlockEntity stereo = this.stereo();
        if (stereo == null) {
            return;
        }
        this.mode.set(stereo.getMode());
        this.maxSpeakers.set(stereo.getMaxSpeakers());
        this.range.set(stereo.getRange());
        // Only speakers that still exist: the stored pairings outlive the blocks, so counting the raw
        // set kept climbing as speakers were broken and replaced.
        this.paired.set(stereo.pruneAndCountPaired());
        this.volume.set(Math.round(stereo.getVolume() * 100.0F));
    }

    // Keep the stats live while the screen is open, so installing an upgrade or losing a speaker shows up
    // straight away rather than waiting for the next button press.
    @Override
    public void broadcastChanges() {
        this.refreshStats();
        super.broadcastChanges();
    }

    public int getMode() {
        return this.mode.get();
    }

    public int getMaxSpeakers() {
        return this.maxSpeakers.get();
    }

    public int getRange() {
        return this.range.get();
    }

    public int getPairedCount() {
        return this.paired.get();
    }

    /**
     * @return The master volume as a percent
     */
    public int getVolumePercent() {
        return this.volume.get();
    }

    /**
     * @return The stereo's position, for the screen to start link mode against
     */
    public BlockPos getPos() {
        StereoBlockEntity stereo = this.stereo();
        return stereo != null ? stereo.getBlockPos() : BlockPos.ZERO;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        this.refreshStats();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        StereoBlockEntity stereo = this.stereo();
        if (stereo == null) {
            return false;
        }

        if (id >= 0 && id <= 100) {
            stereo.setVolume(id / 100.0F);
            this.refreshStats();
            this.broadcastChanges();
            return true;
        }
        if (id == BUTTON_MODE) {
            stereo.setMode(stereo.getMode() == StereoBlockEntity.MODE_ALL ? StereoBlockEntity.MODE_NEAREST : StereoBlockEntity.MODE_ALL);
            this.refreshStats();
            this.broadcastChanges();
            return true;
        }
        if (id == BUTTON_LINK) {
            gg.moonflower.etched.common.block.StereoBlock.startLinking(player, stereo.getBlockPos());
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                serverPlayer.closeContainer();
            }
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();
            int upgrades = StereoBlockEntity.UPGRADE_SLOTS;
            if (index < upgrades) {
                if (!this.moveItemStackTo(slotStack, upgrades, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, upgrades, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (slotStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    /**
     * A slot that only takes one kind of upgrade, so where a part sits in the menu always matches where
     * the screen draws it on the unit.
     */
    private static class UpgradeSlot extends Slot {

        private final Item upgrade;

        UpgradeSlot(Container container, int slot, int x, int y, Item upgrade) {
            super(container, slot, x, y);
            this.upgrade = upgrade;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(this.upgrade);
        }

        // One part per bay: an upgrade is a piece of hardware installed into the unit, so a stack of them
        // in a single slot shouldn't count as several. This is what makes the speaker and range ceilings
        // real rather than advisory.
        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
