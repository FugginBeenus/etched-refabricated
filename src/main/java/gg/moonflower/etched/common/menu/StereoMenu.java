package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.common.blockentity.StereoBlockEntity;
import gg.moonflower.etched.core.registry.EtchedMenus;
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

        for (int i = 0; i < StereoBlockEntity.UPGRADE_SLOTS; i++) {
            this.addSlot(new Slot(container, i, 44 + i * 22, 34) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return StereoBlockEntity.isUpgrade(stack);
                }
            });
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
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
        this.paired.set(stereo.getPairedSpeakers().size());
        this.volume.set(Math.round(stereo.getVolume() * 100.0F));
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

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
