package gg.moonflower.etched.common.blockentity;

import gg.moonflower.etched.api.sound.SoundTracker;
import gg.moonflower.etched.common.block.RadioBlock;
import gg.moonflower.etched.core.registry.EtchedBlocks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class RadioBlockEntity extends BlockEntity implements Clearable {

    private String url;
    private boolean loaded;
    // Set when tuned to a station. The station pushes each track's link here as it goes on air.
    @Nullable
    private BlockPos stationPos;
    // Switched off by hand. The link and the station are kept, so turning it back on resumes.
    private boolean switchedOff;

    public RadioBlockEntity(BlockPos pos, BlockState state) {
        this(EtchedBlocks.RADIO_BE.get(), pos, state);
    }
    public RadioBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RadioBlockEntity blockEntity) {
        if (level == null || !level.isClientSide()) {
            return;
        }

        if (!blockEntity.loaded) {
            blockEntity.loaded = true;
            SoundTracker.playRadio(blockEntity.url, state, (ClientLevel) level, pos);
        }

        if (blockEntity.isPlaying()) {
            AABB range = new AABB(pos).inflate(3.45);
            List<LivingEntity> livingEntities = level.getEntitiesOfClass(LivingEntity.class, range);
            livingEntities.forEach(living -> living.setRecordPlayingNearby(pos, true));
        }
    }

    //? if >=1.21 {
    /*@Override
    protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        this.url = nbt.contains("Url", Tag.TAG_STRING) ? nbt.getString("Url") : null;
        this.stationPos = nbt.contains("StationPos") ? BlockPos.of(nbt.getLong("StationPos")) : null;
        this.switchedOff = nbt.getBoolean("SwitchedOff");
        if (this.loaded) {
            SoundTracker.playRadio(this.url, this.getBlockState(), (ClientLevel) this.level, this.getBlockPos());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider provider) {
        if (this.url != null) {
            nbt.putString("Url", this.url);
        }
        if (this.stationPos != null) {
            nbt.putLong("StationPos", this.stationPos.asLong());
        }
        if (this.switchedOff) {
            nbt.putBoolean("SwitchedOff", true);
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
        this.url = nbt.contains("Url", Tag.TAG_STRING) ? nbt.getString("Url") : null;
        this.stationPos = nbt.contains("StationPos") ? BlockPos.of(nbt.getLong("StationPos")) : null;
        this.switchedOff = nbt.getBoolean("SwitchedOff");
        if (this.loaded) {
            SoundTracker.playRadio(this.url, this.getBlockState(), (ClientLevel) this.level, this.getBlockPos());
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        if (this.url != null) {
            nbt.putString("Url", this.url);
        }
        if (this.stationPos != null) {
            nbt.putLong("StationPos", this.stationPos.asLong());
        }
        if (this.switchedOff) {
            nbt.putBoolean("SwitchedOff", true);
        }
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

    @Override
    public void clearContent() {
        this.url = null;
        if (this.level != null && this.level.isClientSide()) {
            SoundTracker.playRadio(this.url, this.getBlockState(), (ClientLevel) this.level, this.getBlockPos());
        }
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        if (!Objects.equals(this.url, url)) {
            this.url = url;
            this.setChanged();
            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    /**
     * Sets the link and always tells listeners, even when it has not changed. A station moving on to
     * another copy of the same track, or wrapping back to its only disc, has to restart it; the ordinary
     * setter would see no change and leave the radio silent.
     */
    public void setUrlAndRestart(String url) {
        this.url = url;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Nullable
    public BlockPos getStationPos() {
        return this.stationPos;
    }

    /** Tunes to a station, or clears back to a plain link when given null. */
    public void setStation(@Nullable BlockPos stationPos) {
        this.stationPos = stationPos == null ? null : stationPos.immutable();
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean isSwitchedOff() {
        return this.switchedOff;
    }

    /** Flips the switch, returning the new state so the block can tell the player. */
    public boolean toggleSwitchedOff() {
        this.switchedOff = !this.switchedOff;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
        return this.switchedOff;
    }

    public boolean isPlaying() {
        BlockState state = this.getBlockState();
        return !this.switchedOff
                && (!state.hasProperty(RadioBlock.POWERED) || !state.getValue(RadioBlock.POWERED))
                && !StringUtil.isNullOrEmpty(this.url);
    }
}
