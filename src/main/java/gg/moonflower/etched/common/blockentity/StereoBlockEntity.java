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
public class StereoBlockEntity extends BlockEntity implements net.minecraft.world.Container {

    public static final int BASE_SPEAKERS = 2;
    public static final int SPEAKERS_PER_PREAMP = 2;
    public static final int BASE_RANGE = 8;
    public static final int RANGE_PER_TRANSMITTER = 16;

    public static final int UPGRADE_SLOTS = 4;
    /** Every speaker plays the record at once. */
    public static final int MODE_ALL = 0;
    /** Only the speaker nearest the listener plays, and it follows them. */
    public static final int MODE_NEAREST = 1;

    private final Set<BlockPos> speakers = new LinkedHashSet<>();
    private final net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> upgrades =
            net.minecraft.core.NonNullList.withSize(UPGRADE_SLOTS, net.minecraft.world.item.ItemStack.EMPTY);
    private int mode = MODE_ALL;
    private float volume = 1.0F;

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
        return BASE_SPEAKERS + this.getPreamps() * SPEAKERS_PER_PREAMP;
    }

    public int getRange() {
        return BASE_RANGE + this.getTransmitters() * RANGE_PER_TRANSMITTER;
    }

    public int getPreamps() {
        return this.countUpgrade(gg.moonflower.etched.core.registry.EtchedItems.PREAMP.asItem());
    }

    public int getTransmitters() {
        return this.countUpgrade(gg.moonflower.etched.core.registry.EtchedItems.TRANSMITTER.asItem());
    }

    private int countUpgrade(net.minecraft.world.item.Item item) {
        int count = 0;
        for (net.minecraft.world.item.ItemStack stack : this.upgrades) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * @return The slots upgrades are installed in
     */
    public net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> getUpgrades() {
        return this.upgrades;
    }

    public int getMode() {
        return this.mode;
    }

    /**
     * @return The master volume every speaker this stereo drives is scaled by
     */
    public float getVolume() {
        return this.volume;
    }

    public void setVolume(float volume) {
        this.volume = net.minecraft.util.Mth.clamp(volume, 0.0F, 1.0F);
        this.sync();
    }

    /**
     * @return The master volume of the stereo above the given block, or full volume if there is none
     */
    public static float masterVolumeAt(net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos sourcePos) {
        return level.getBlockEntity(sourcePos.above()) instanceof StereoBlockEntity stereo ? stereo.getVolume() : 1.0F;
    }

    public void setMode(int mode) {
        this.mode = mode;
        this.sync();
    }

    public static boolean isUpgrade(net.minecraft.world.item.ItemStack stack) {
        return stack.is(gg.moonflower.etched.core.registry.EtchedItems.PREAMP.asItem())
                || stack.is(gg.moonflower.etched.core.registry.EtchedItems.TRANSMITTER.asItem());
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

    private void readCommon(CompoundTag nbt) {
        this.speakers.clear();
        ListTag list = nbt.getList("Speakers", Tag.TAG_LONG);
        for (int i = 0; i < list.size(); i++) {
            this.speakers.add(BlockPos.of(((LongTag) list.get(i)).getAsLong()));
        }
        this.mode = nbt.getInt("Mode");
        this.volume = nbt.contains("Volume") ? nbt.getFloat("Volume") : 1.0F;
    }

    private void writeCommon(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (BlockPos speaker : this.speakers) {
            list.add(LongTag.valueOf(speaker.asLong()));
        }
        nbt.put("Speakers", list);
        nbt.putInt("Mode", this.mode);
        nbt.putFloat("Volume", this.volume);
    }

    // ---- upgrade container ----

    @Override
    public int getContainerSize() {
        return UPGRADE_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (net.minecraft.world.item.ItemStack stack : this.upgrades) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public net.minecraft.world.item.ItemStack getItem(int slot) {
        return this.upgrades.get(slot);
    }

    @Override
    public net.minecraft.world.item.ItemStack removeItem(int slot, int amount) {
        net.minecraft.world.item.ItemStack stack = net.minecraft.world.ContainerHelper.removeItem(this.upgrades, slot, amount);
        this.sync();
        return stack;
    }

    @Override
    public net.minecraft.world.item.ItemStack removeItemNoUpdate(int slot) {
        net.minecraft.world.item.ItemStack stack = net.minecraft.world.ContainerHelper.takeItem(this.upgrades, slot);
        this.sync();
        return stack;
    }

    @Override
    public void setItem(int slot, net.minecraft.world.item.ItemStack stack) {
        this.upgrades.set(slot, stack);
        this.sync();
    }

    @Override
    public boolean canPlaceItem(int slot, net.minecraft.world.item.ItemStack stack) {
        return isUpgrade(stack);
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this && player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        this.upgrades.clear();
        this.sync();
    }

    //? if >=1.21 {
    /*@Override
    protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        this.readCommon(nbt);
        this.upgrades.clear();
        net.minecraft.world.ContainerHelper.loadAllItems(nbt, this.upgrades, provider);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        this.writeCommon(nbt);
        net.minecraft.world.ContainerHelper.saveAllItems(nbt, this.upgrades, provider);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }
    *///?} else {
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.readCommon(nbt);
        this.upgrades.clear();
        net.minecraft.world.ContainerHelper.loadAllItems(nbt, this.upgrades);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        this.writeCommon(nbt);
        net.minecraft.world.ContainerHelper.saveAllItems(nbt, this.upgrades);
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
