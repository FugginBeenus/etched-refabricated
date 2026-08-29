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

    public static final int SLOTS = 18;
    /**
     * How long a track is assumed to run when nothing better is known. A station must keep moving no
     * matter what is in it, so an unknown length is a guess rather than a stall.
     */
    public static final int DEFAULT_TRACK_TICKS = 20 * 180;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private int discIndex;
    private int track;
    // The station keeps its own name. From 1.21 the parent's name field is private with no setter, so
    // there is nothing to hand a name typed into the screen.
    @Nullable
    private Component stationName;
    // Radios tuned to this station. Kept here so a track change reaches all of them at once.
    private final java.util.Set<BlockPos> radios = new java.util.HashSet<>();
    // What is on air and how long it has been, so the station keeps its schedule with nobody listening.
    @Nullable
    private String airUrl;
    // When the track on air began, in game time. Listeners work out how far in they are from this, so
    // one joining part way through comes in at the same point as everyone else.
    private long airStartedAt;

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
        return this.urlAt(this.discIndex, this.track);
    }

    /**
     * How many tracks the disc in this slot has, or zero when it holds nothing playable. A vanilla disc
     * counts as one: its sound event is what goes on air, exactly as a jukebox plays it.
     */
    private int trackCountAt(int slot) {
        if (slot < 0 || slot >= SLOTS) {
            return 0;
        }
        ItemStack stack = this.getItem(slot);
        if (!PlayableRecord.isPlayableRecord(stack)) {
            return 0;
        }
        TrackData[] tracks = PlayableRecord.getStackMusic(stack).orElse(null);
        if (tracks != null && tracks.length > 0) {
            return tracks.length;
        }
        return this.vanillaSound(stack) != null ? 1 : 0;
    }

    @Nullable
    private String urlAt(int slot, int track) {
        if (slot < 0 || slot >= SLOTS || track < 0) {
            return null;
        }
        ItemStack stack = this.getItem(slot);
        if (!PlayableRecord.isPlayableRecord(stack)) {
            return null;
        }

        TrackData[] tracks = PlayableRecord.getStackMusic(stack).orElse(null);
        if (tracks != null && track < tracks.length) {
            String url = tracks[track].url();
            return TrackData.isValidURL(url) ? url : null;
        }
        return track == 0 ? this.vanillaSound(stack) : null;
    }

    /** The sound a vanilla disc plays, which counts as a link the same way a local sound does. */
    @Nullable
    //? if >=1.21 {
    /*private String vanillaSound(ItemStack stack) {
        if (this.level == null) {
            return null;
        }
        return net.minecraft.world.item.JukeboxSong.fromStack(this.level.registryAccess(), stack)
                .map(song -> song.value().soundEvent().value().getLocation().toString())
                .orElse(null);
    }
    *///?} else {
    private String vanillaSound(ItemStack stack) {
        return stack.getItem() instanceof net.minecraft.world.item.RecordItem record
                ? record.getSound().getLocation().toString()
                : null;
    }
    //?}

    /**
     * Moves to the next track, then to the next disc holding one, wrapping around. Stops on the first
     * playable thing it finds so an empty slot in the middle of the list is simply skipped.
     */
    public void advance() {
        if (this.track + 1 < this.trackCountAt(this.discIndex)) {
            this.track++;
            this.putOnAir();
            return;
        }

        for (int step = 1; step <= SLOTS; step++) {
            int slot = Math.floorMod(this.discIndex + step, SLOTS);
            if (this.trackCountAt(slot) > 0) {
                this.discIndex = slot;
                this.track = 0;
                this.putOnAir();
                return;
            }
        }

        // Nothing playable left in the station.
        this.discIndex = 0;
        this.track = 0;
        this.putOnAir();
    }

    /**
     * How long the track on air runs. A vanilla disc's length is known to the game itself, so the station
     * times those exactly without anyone listening. An etched link has no length the server can read, so
     * the longest anyone has been heard playing it is used, falling back to an assumed length.
     */
    private int durationTicks() {
        int vanilla = this.vanillaLength(this.getItem(this.discIndex));
        if (vanilla > 0) {
            return vanilla;
        }
        int learned = gg.moonflower.etched.common.block.TrackDurations.get(this.airUrl);
        return learned > 0 ? learned : DEFAULT_TRACK_TICKS;
    }

    //? if >=1.21 {
    /*private int vanillaLength(ItemStack stack) {
        if (this.level == null) {
            return 0;
        }
        return net.minecraft.world.item.JukeboxSong.fromStack(this.level.registryAccess(), stack)
                .map(song -> song.value().lengthInTicks())
                .orElse(0);
    }
    *///?} else {
    private int vanillaLength(ItemStack stack) {
        return stack.getItem() instanceof net.minecraft.world.item.RecordItem record ? record.getLengthInTicks() : 0;
    }
    //?}

    /** Starts whatever is now current, resetting the clock and telling the listeners. */
    private void putOnAir() {
        this.airUrl = this.getCurrentUrl();
        this.airStartedAt = this.level != null ? this.level.getGameTime() : 0L;
        this.setChanged();
        this.broadcast(true);
    }

    public long getAirStartedAt() {
        return this.airStartedAt;
    }

    /**
     * Runs the station's schedule, entirely on its own. Nothing a radio does can move it: a radio being
     * broken or switched off used to report the track as finished and skipped it for everyone listening.
     */
    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, RadioStationBlockEntity station) {
        if (station.airUrl == null) {
            // Look again now and then, to pick up discs put in while nothing was on air.
            if (level.getGameTime() % 20L == 0L && station.getCurrentUrl() != null) {
                station.putOnAir();
            }
            return;
        }

        if (level.getGameTime() - station.airStartedAt >= station.durationTicks()) {
            station.advance();
        }
    }

    /** Puts the station back on the first thing it can play, used when its discs change. */
    public void rewindToPlayable() {
        for (int slot = 0; slot < SLOTS; slot++) {
            if (this.trackCountAt(slot) > 0) {
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
        if (this.trackCountAt(this.discIndex) == 0) {
            this.rewindToPlayable();
        }
        // Listeners follow the new list straight away rather than waiting for the current track to end.
        // Only a genuinely different track restarts the clock; adding a disc further down the list must
        // not interrupt what is already playing.
        if (!java.util.Objects.equals(this.getCurrentUrl(), this.airUrl)) {
            this.putOnAir();
        } else {
            this.broadcast();
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

    // Listed while loaded, so a radio anywhere in the world can find this station.
    @Override
    public void setLevel(net.minecraft.world.level.Level level) {
        super.setLevel(level);
        gg.moonflower.etched.common.block.RadioStations.add(level, this.worldPosition);
    }

    @Override
    public void setRemoved() {
        if (this.level != null) {
            gg.moonflower.etched.common.block.RadioStations.remove(this.level, this.worldPosition);
        }
        super.setRemoved();
    }

    public void tune(BlockPos radioPos) {
        this.radios.add(radioPos.immutable());
        this.setChanged();
        this.broadcast();
    }

    public void untune(BlockPos radioPos) {
        this.radios.remove(radioPos);
        this.setChanged();
    }

    public boolean isTuned(BlockPos radioPos) {
        return this.radios.contains(radioPos);
    }

    public int getListenerCount() {
        return this.radios.size();
    }

    /**
     * Pushes whatever is on air to every tuned radio, dropping any that have been broken or retuned. The
     * radio's own link field is what is set, so all its existing playback carries the sound.
     */
    public void broadcast() {
        this.broadcast(false);
    }

    /** @param restart whether listeners should start the track again even if the link is unchanged */
    public void broadcast(boolean restart) {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        String url = this.getCurrentUrl();
        java.util.Iterator<BlockPos> it = this.radios.iterator();
        boolean changed = false;
        while (it.hasNext()) {
            BlockPos pos = it.next();
            if (!this.level.isLoaded(pos)) {
                continue;
            }
            if (!(this.level.getBlockEntity(pos) instanceof RadioBlockEntity radio)
                    || !this.worldPosition.equals(radio.getStationPos())) {
                it.remove();
                changed = true;
                continue;
            }
            radio.setStationTrack(url, this.airStartedAt, restart);
        }
        if (changed) {
            this.setChanged();
        }
    }

    /** Renames the station from its own screen. An empty name falls back to the default. */
    public void setStationName(@Nullable Component name) {
        this.stationName = name;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // A name set in the screen wins; otherwise fall back to the parent, which is what an anvil-renamed
    // item ends up in.
    @Nullable
    @Override
    public Component getCustomName() {
        return this.stationName != null ? this.stationName : super.getCustomName();
    }

    @Override
    public Component getName() {
        Component custom = this.getCustomName();
        return custom != null ? custom : this.getDefaultName();
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
        this.airStartedAt = nbt.getLong("AirStartedAt");
        this.radios.clear();
        for (long packed : nbt.getLongArray("Radios")) {
            this.radios.add(BlockPos.of(packed));
        }
        String savedName = nbt.getString("StationName");
        this.stationName = savedName.isEmpty() ? null : Component.literal(savedName);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        if (!this.trySaveLootTable(nbt)) {
            net.minecraft.world.ContainerHelper.saveAllItems(nbt, this.items, provider);
        }
        nbt.putInt("DiscIndex", this.discIndex);
        nbt.putInt("Track", this.track);
        nbt.putLong("AirStartedAt", this.airStartedAt);
        nbt.putLongArray("Radios", this.radios.stream().mapToLong(BlockPos::asLong).toArray());
        if (this.stationName != null) {
            // Plain text: the name is typed by a player, and the component serializer takes different
            // arguments on either version.
            nbt.putString("StationName", this.stationName.getString());
        }
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
        this.airStartedAt = nbt.getLong("AirStartedAt");
        this.radios.clear();
        for (long packed : nbt.getLongArray("Radios")) {
            this.radios.add(BlockPos.of(packed));
        }
        String savedName = nbt.getString("StationName");
        this.stationName = savedName.isEmpty() ? null : Component.literal(savedName);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (!this.trySaveLootTable(nbt)) {
            net.minecraft.world.ContainerHelper.saveAllItems(nbt, this.items);
        }
        nbt.putInt("DiscIndex", this.discIndex);
        nbt.putInt("Track", this.track);
        nbt.putLong("AirStartedAt", this.airStartedAt);
        nbt.putLongArray("Radios", this.radios.stream().mapToLong(BlockPos::asLong).toArray());
        if (this.stationName != null) {
            // Plain text: the name is typed by a player, and the component serializer takes different
            // arguments on either version.
            nbt.putString("StationName", this.stationName.getString());
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
    //?}
}
