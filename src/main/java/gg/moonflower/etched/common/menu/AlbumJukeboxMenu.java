package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.common.blockentity.AlbumJukeboxBlockEntity;
import gg.moonflower.etched.common.item.AlbumCoverItem;
import gg.moonflower.etched.common.network.play.SetAlbumJukeboxTrackPacket;
import gg.moonflower.etched.core.registry.EtchedItems;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ocelot
 */
public class AlbumJukeboxMenu extends AbstractContainerMenu {

    public static final int BUTTON_TOGGLE_ALBUM = 0;

    private final BlockPos.MutableBlockPos pos;
    private final Container container;
    // Single-slot view of the block entity's parked album cover. It writes straight through to the
    // block entity (server side) so an emptied album persists in the jukebox until it's repacked.
    private final SimpleContainer coverContainer;
    private boolean initialized;

    public AlbumJukeboxMenu(int i, Inventory inventory) {
        this(i, inventory, new SimpleContainer(9), BlockPos.ZERO);
    }

    public AlbumJukeboxMenu(int i, Inventory inventory, Container container, BlockPos pos) {
        super(EtchedMenus.ALBUM_JUKEBOX_MENU, i);
        checkContainerSize(container, 9);
        this.container = container;
        container.startOpen(inventory.player);

        // Server side, the container is the block entity; write cover-slot changes straight to it so
        // the parked album persists. Client side it's a dummy that the menu sync populates.
        AlbumJukeboxBlockEntity blockEntity = container instanceof AlbumJukeboxBlockEntity ? (AlbumJukeboxBlockEntity) container : null;
        this.coverContainer = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (blockEntity != null) {
                    blockEntity.setStoredCover(this.getItem(0));
                }
            }
        };
        if (blockEntity != null) {
            this.coverContainer.setItem(0, blockEntity.getStoredCover());
        }

        this.pos = new BlockPos.MutableBlockPos().set(pos);
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return AlbumJukeboxMenu.this.pos.getX();
            }

            @Override
            public void set(int value) {
                AlbumJukeboxMenu.this.pos.setX(value);
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return AlbumJukeboxMenu.this.pos.getY();
            }

            @Override
            public void set(int value) {
                AlbumJukeboxMenu.this.pos.setY(value);
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return AlbumJukeboxMenu.this.pos.getZ();
            }

            @Override
            public void set(int value) {
                AlbumJukeboxMenu.this.pos.setZ(value);
            }
        });

        for (int n = 0; n < 3; ++n) {
            for (int m = 0; m < 3; ++m) {
                this.addSlot(new Slot(container, m + n * 3, 62 + m * 18, 17 + n * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return PlayableRecord.isPlayableRecord(stack);
                    }
                });
            }
        }

        for (int n = 0; n < 3; ++n) {
            for (int m = 0; m < 9; ++m) {
                this.addSlot(new Slot(inventory, m + n * 9 + 9, 8 + m * 18, 84 + n * 18));
            }
        }

        for (int n = 0; n < 9; ++n) {
            this.addSlot(new Slot(inventory, n, 8 + n * 18, 142));
        }

        // Album-cover slot (last index, so it doesn't shift the jukebox/inventory indices).
        this.addSlot(new Slot(this.coverContainer, 0, 134, 22) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(EtchedItems.ALBUM_COVER.asItem());
            }
        });
    }

    public ItemStack getCoverSlotItem() {
        return this.coverContainer.getItem(0);
    }

    private int firstEmptyRecordSlot() {
        for (int i = 0; i < this.container.getContainerSize(); i++) {
            if (this.container.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_TOGGLE_ALBUM) {
            return false;
        }

        ItemStack cover = this.coverContainer.getItem(0);
        if (!cover.is(EtchedItems.ALBUM_COVER.asItem())) {
            return false;
        }

        List<ItemStack> records = new ArrayList<>(AlbumCoverItem.getRecords(cover));
        if (!records.isEmpty()) {
            // Unload: spill the cover's discs into empty jukebox slots, keeping any that don't fit.
            List<ItemStack> leftover = new ArrayList<>();
            for (ItemStack record : records) {
                int slot = this.firstEmptyRecordSlot();
                if (slot < 0) {
                    leftover.add(record);
                } else {
                    this.container.setItem(slot, record.copy());
                }
            }
            AlbumCoverItem.setRecords(cover, leftover);
            if (leftover.isEmpty()) {
                AlbumCoverItem.setCover(cover, ItemStack.EMPTY);
            }
            this.coverContainer.setItem(0, cover);
            this.container.setChanged();
            return true;
        }

        // Repack: pull loose jukebox discs back into the (empty) cover.
        List<ItemStack> packed = new ArrayList<>();
        boolean changed = false;
        for (int slot = 0; slot < this.container.getContainerSize() && packed.size() < AlbumCoverItem.MAX_RECORDS; slot++) {
            ItemStack disc = this.container.getItem(slot);
            if (!disc.isEmpty() && AlbumCoverMenu.isValid(disc)) {
                packed.add(disc.copy());
                this.container.setItem(slot, ItemStack.EMPTY);
                changed = true;
            }
        }
        if (!changed) {
            return false;
        }
        AlbumCoverItem.setRecords(cover, packed);
        if (AlbumCoverItem.getCoverStack(cover).isEmpty()) {
            AlbumCoverItem.setCover(cover, packed.get(0));
        }
        this.coverContainer.setItem(0, cover);
        this.container.setChanged();
        return true;
    }

    public boolean setPlayingTrack(Level level, SetAlbumJukeboxTrackPacket pkt) {
        BlockEntity blockEntity = level.getBlockEntity(this.pos);
        if (blockEntity instanceof AlbumJukeboxBlockEntity) {
            return ((AlbumJukeboxBlockEntity) blockEntity).setPlayingIndex(pkt.playingIndex(), pkt.track());
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (i < this.container.getContainerSize()) {
                if (!this.moveItemStackTo(itemStack2, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, this.container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
        }

        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public void setData(int index, int value) {
        super.setData(index, value);
        if (index >= 0 && index < 3) {
            this.initialized = true;
        }
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public BlockPos getPos() {
        return this.pos;
    }
}
