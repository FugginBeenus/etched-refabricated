package gg.moonflower.etched.common.blockentity;

import gg.moonflower.etched.core.registry.EtchedBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Holds how loud an individual speaker plays. Records are scaled by this and by the stereo's master
 * volume, so one speaker can be quiet without turning the rest down.
 *
 * @author Jackson
 */
public class SpeakerBlockEntity extends BlockEntity {

    private float volume = 1.0F;

    public SpeakerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public SpeakerBlockEntity(BlockPos pos, BlockState state) {
        this(EtchedBlocks.SPEAKER_BE.get(), pos, state);
    }

    public float getVolume() {
        return this.volume;
    }

    public void setVolume(float volume) {
        this.volume = Mth.clamp(volume, 0.0F, 1.0F);
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    /**
     * @return The volume of the speaker at the given position, or full volume if it has no data yet
     */
    public static float volumeAt(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SpeakerBlockEntity speaker ? speaker.getVolume() : 1.0F;
    }

    //? if >=1.21 {
    /*@Override
    protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        this.volume = nbt.contains("Volume") ? nbt.getFloat("Volume") : 1.0F;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        nbt.putFloat("Volume", this.volume);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }
    *///?} else {
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.volume = nbt.contains("Volume") ? nbt.getFloat("Volume") : 1.0F;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putFloat("Volume", this.volume);
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
