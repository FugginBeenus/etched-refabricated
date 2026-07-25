package gg.moonflower.etched.common.blockentity;

import gg.moonflower.etched.common.block.SpeakerBlock;
import gg.moonflower.etched.core.registry.EtchedBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Sits on top of a jukebox and drives the speakers that play its records. Speakers touching the
 * jukebox are always connected; the stereo adds wireless ones the player has paired to it, up to a
 * count and range that its upgrades raise.
 *
 * @author Jackson
 */
public class StereoBlockEntity extends BlockEntity {

    public static final int BASE_SPEAKERS = 2;
    public static final int SPEAKERS_PER_PREAMP = 2;
    public static final int BASE_RANGE = 8;
    public static final int RANGE_PER_TRANSMITTER = 16;

    private final Set<BlockPos> speakers = new LinkedHashSet<>();
    private int preamps;
    private int transmitters;

    public StereoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public StereoBlockEntity(BlockPos pos, BlockState state) {
        this(EtchedBlocks.STEREO_BE.get(), pos, state);
    }

    /**
     * @return The jukebox this stereo is sitting on
     */
    public BlockPos getSourcePos() {
        return this.worldPosition.below();
    }

    public int getMaxSpeakers() {
        return BASE_SPEAKERS + this.preamps * SPEAKERS_PER_PREAMP;
    }

    public int getRange() {
        return BASE_RANGE + this.transmitters * RANGE_PER_TRANSMITTER;
    }

    public int getPreamps() {
        return this.preamps;
    }

    public int getTransmitters() {
        return this.transmitters;
    }

    public void setUpgrades(int preamps, int transmitters) {
        this.preamps = preamps;
        this.transmitters = transmitters;
        this.sync();
    }

    /**
     * @return Every paired speaker position, whether or not it is still valid
     */
    public Set<BlockPos> getPairedSpeakers() {
        return this.speakers;
    }

    public boolean isPaired(BlockPos pos) {
        return this.speakers.contains(pos.immutable());
    }

    /**
     * Pairs a speaker, or unpairs it if it already was.
     *
     * @param pos The speaker to toggle
     * @return The state of the speaker after toggling
     */
    public boolean togglePaired(BlockPos pos) {
        BlockPos key = pos.immutable();
        boolean paired;
        if (this.speakers.remove(key)) {
            paired = false;
        } else {
            this.speakers.add(key);
            paired = true;
        }
        this.sync();
        return paired;
    }

    public boolean inRange(BlockPos pos) {
        int range = this.getRange();
        return pos.distSqr(this.worldPosition) <= (double) range * range;
    }

    /**
     * The speakers a record should currently play from: everything touching the jukebox, plus paired
     * speakers that are still present and in range, capped by the installed preamps.
     *
     * @param level The level to look up blocks in
     * @return The speaker positions to play from
     */
    public List<BlockPos> getActiveSpeakers(BlockGetter level) {
        List<BlockPos> active = new ArrayList<>();
        BlockPos source = this.getSourcePos();
        for (Direction direction : Direction.values()) {
            BlockPos side = source.relative(direction);
            if (level.getBlockState(side).getBlock() instanceof SpeakerBlock) {
                active.add(side);
            }
        }

        // Speakers touching the jukebox are wired and always play; the upgrade limit only governs how
        // many wireless ones the stereo can drive.
        int max = this.getMaxSpeakers();
        int wireless = 0;
        for (BlockPos paired : this.speakers) {
            if (wireless >= max) {
                break;
            }
            if (active.contains(paired) || !this.inRange(paired)) {
                continue;
            }
            if (level.getBlockState(paired).getBlock() instanceof SpeakerBlock) {
                active.add(paired);
                wireless++;
            }
        }
        return active;
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    private void readData(CompoundTag nbt) {
        this.speakers.clear();
        ListTag list = nbt.getList("Speakers", Tag.TAG_LONG);
        for (int i = 0; i < list.size(); i++) {
            this.speakers.add(BlockPos.of(((LongTag) list.get(i)).getAsLong()));
        }
        this.preamps = nbt.getInt("Preamps");
        this.transmitters = nbt.getInt("Transmitters");
    }

    private void writeData(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (BlockPos speaker : this.speakers) {
            list.add(LongTag.valueOf(speaker.asLong()));
        }
        nbt.put("Speakers", list);
        nbt.putInt("Preamps", this.preamps);
        nbt.putInt("Transmitters", this.transmitters);
    }

    //? if >=1.21 {
    /*@Override
    protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        this.readData(nbt);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        this.writeData(nbt);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }
    *///?} else {
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.readData(nbt);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        this.writeData(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
    //?}

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
