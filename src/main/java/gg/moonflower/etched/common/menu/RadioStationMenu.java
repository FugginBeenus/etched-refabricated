package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.common.blockentity.RadioStationBlockEntity;
import gg.moonflower.etched.core.registry.EtchedMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RadioStationMenu extends AbstractContainerMenu {

    // Twenty discs as four rows of five, centred. Ten across would have needed a wider panel than the
    // one every other screen in the mod uses.
    public static final int COLUMNS = 5;
    public static final int SLOT_X = 43;
    public static final int SLOT_Y = 36;
    public static final int INVENTORY_X = 8;
    public static final int INVENTORY_Y = 112;
    public static final int HOTBAR_Y = 172;

    private final Container container;

    public RadioStationMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(RadioStationBlockEntity.SLOTS));
    }

    public RadioStationMenu(int id, Inventory inventory, Container container) {
        super(EtchedMenus.RADIO_STATION_MENU, id);
        checkContainerSize(container, RadioStationBlockEntity.SLOTS);
        this.container = container;
        container.startOpen(inventory.player);

        for (int i = 0; i < RadioStationBlockEntity.SLOTS; i++) {
            this.addSlot(new Slot(container, i, SLOT_X + (i % COLUMNS) * 18, SLOT_Y + (i / COLUMNS) * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return PlayableRecord.isPlayableRecord(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, INVENTORY_X + col * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, INVENTORY_X + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            moved = stack.copy();
            int stationSlots = RadioStationBlockEntity.SLOTS;
            if (index < stationSlots) {
                if (!this.moveItemStackTo(stack, stationSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveIntoStation(stack)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return moved;
    }

    // One disc per slot. Vanilla's own move merges into an occupied slot using the item's stack size,
    // which would walk past the limit of one.
    private boolean moveIntoStation(ItemStack stack) {
        boolean moved = false;
        for (int i = 0; i < RadioStationBlockEntity.SLOTS && !stack.isEmpty(); i++) {
            Slot slot = this.slots.get(i);
            if (slot.getItem().isEmpty() && slot.mayPlace(stack)) {
                slot.set(stack.split(1));
                moved = true;
            }
        }
        return moved;
    }

    /** Applies a name typed into the station's screen. Blank clears it back to the default. */
    public void setStationName(String name) {
        if (!(this.container instanceof RadioStationBlockEntity station)) {
            return;
        }
        String trimmed = name.trim();
        if (trimmed.length() > gg.moonflower.etched.common.network.play.ServerboundSetStationNamePacket.MAX_LENGTH) {
            trimmed = trimmed.substring(0, gg.moonflower.etched.common.network.play.ServerboundSetStationNamePacket.MAX_LENGTH);
        }
        station.setStationName(trimmed.isEmpty() ? null : net.minecraft.network.chat.Component.literal(trimmed));
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
