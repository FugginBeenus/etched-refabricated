package gg.moonflower.etched.common.blockentity;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.common.menu.RadioStationMenu;
import gg.moonflower.etched.core.registry.EtchedBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A station holds discs and decides which track is on air. It makes no sound itself; radios tuned to it
 * are what play. The choice of track lives on the server so every listener hears the same one.
 */
public class RadioStationBlockEntity extends RandomizableContainerBlockEntity {

    public static final int SLOTS = 9;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private int discIndex;
    private int track;

    public RadioStationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public RadioStationBlockEntity(BlockPos pos, BlockState state) {
        this(EtchedBlocks.RADIO_STATION_BE.get(), pos, state);
    }

    public int getDiscIndex() {
        return this.discIndex;
    }

    public int getTrack() {
        return this.track;
    }

    /** The link currently on air, or null when the station has nothing playable in it. */
    @Nullable
    public String getCurrentUrl() {
        TrackData[] tracks = this.tracksAt(this.discIndex);
        if (tracks == null || this.track >= tracks.length) {
            return null;
        }
        String url = tracks[this.track].url();
        return TrackData.isValidURL(url) ? url : null;
    }

    @Nullable
    private TrackData[] tracksAt(int slot) {
        if (slot < 0 || slot >= SLOTS) {
            return null;
        }
        ItemStack stack = this.getItem(slot);
        if (!PlayableRecord.isPlayableRecord(stack)) {
            return null;
        }
        return PlayableRecord.getStackMusic(stack).orElse(null);
    }

    /**
     * Moves to the next track, then to the next disc holding one, wrapping around. Stops on the first
     * playable thing it finds so an empty slot in the middle of the list is simply skipped.
     */
    public void advance() {
        TrackData[] current = this.tracksAt(this.discIndex);
        if (current != null && this.track + 1 < current.length) {
            this.track++;
            this.setChanged();
            return;
        }

        for (int step = 1; step <= SLOTS; step++) {
            int slot = Math.floorMod(this.discIndex + step, SLOTS);
            if (this.tracksAt(slot) != null) {
                this.discIndex = slot;
                this.track = 0;
                this.setChanged();
                return;
            }
        }

        // Nothing playable left in the station.
        this.discIndex = 0;
        this.track = 0;
        this.setChanged();
    }

    /** Puts the station back on the first thing it can play, used when its discs change. */
    public void rewindToPlayable() {
        for (int slot = 0; slot < SLOTS; slot++) {
            if (this.tracksAt(slot) != null) {
                this.discIndex = slot;
                this.track = 0;
                this.setChanged();
                return;
            }
        }
        this.discIndex = 0;
        this.track = 0;
        this.setChanged();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        // Swapping a disc out from under the station must not leave it pointing at nothing.
        if (this.tracksAt(this.discIndex) == null) {
            this.rewindToPlayable();
        }
    }

    @Override
    public int getContainerSize() {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return PlayableRecord.isPlayableRecord(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.etched.radio_station");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new RadioStationMenu(id, inventory, this);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    //? if >=1.21 {
    /*@Override
    protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        this.items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        if (!this.tryLoadLootTable(nbt)) {
            net.minecraft.world.ContainerHelper.loadAllItems(nbt, this.items, provider);
        }
        this.discIndex = nbt.getInt("DiscIndex");
        this.track = nbt.getInt("Track");
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        if (!this.trySaveLootTable(nbt)) {
            net.minecraft.world.ContainerHelper.saveAllItems(nbt, this.items, provider);
        }
        nbt.putInt("DiscIndex", this.discIndex);
        nbt.putInt("Track", this.track);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }
    *///?} else {
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        if (!this.tryLoadLootTable(nbt)) {
            net.minecraft.world.ContainerHelper.loadAllItems(nbt, this.items);
        }
        this.discIndex = nbt.getInt("DiscIndex");
        this.track = nbt.getInt("Track");
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (!this.trySaveLootTable(nbt)) {
            net.minecraft.world.ContainerHelper.saveAllItems(nbt, this.items);
        }
        nbt.putInt("DiscIndex", this.discIndex);
        nbt.putInt("Track", this.track);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
    //?}
}
