package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.common.block.AlbumDisplayBlock;
import gg.moonflower.etched.common.blockentity.AlbumCrateBlockEntity;
import gg.moonflower.etched.core.registry.EtchedMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The single row of albums a crate holds.
 */
public class AlbumCrateMenu extends AbstractContainerMenu {

    /** Two rows of six, centred, so a full crate reads at a glance. */
    public static final int SLOT_X = 34;
    public static final int SLOT_Y = 20;
    public static final int INVENTORY_X = 8;
    public static final int INVENTORY_Y = 62;
    public static final int HOTBAR_Y = 120;

    private final Container container;

    public AlbumCrateMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(AlbumCrateBlockEntity.SLOTS));
    }

    public AlbumCrateMenu(int id, Inventory inventory, Container container) {
        super(EtchedMenus.ALBUM_CRATE_MENU, id);
        checkContainerSize(container, AlbumCrateBlockEntity.SLOTS);
        this.container = container;
        container.startOpen(inventory.player);

        for (int i = 0; i < AlbumCrateBlockEntity.SLOTS; i++) {
            int col = i % AlbumCrateBlockEntity.COLUMNS;
            int row = i / AlbumCrateBlockEntity.COLUMNS;
            this.addSlot(new Slot(container, i, SLOT_X + col * 18, SLOT_Y + row * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return AlbumDisplayBlock.canDisplay(stack);
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
            int crate = AlbumCrateBlockEntity.SLOTS;
            if (index < crate) {
                if (!this.moveItemStackTo(stack, crate, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, crate, false)) {
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

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }
}
