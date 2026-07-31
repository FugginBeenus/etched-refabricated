package gg.moonflower.etched.common.block;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.common.blockentity.AlbumDisplayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A stand that shows off a single album's artwork. Right-click with a record or album cover to set it,
 * right-click again to take it back.
 */
public class AlbumDisplayBlock extends BaseEntityBlock {

    /** Sixteen steps like a standing sign, so a stand can be angled rather than snapped to a wall. */
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty ROTATION =
            net.minecraft.world.level.block.state.properties.BlockStateProperties.ROTATION_16;

    // Square and centred so it stays sensible at every angle.
    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 11.0D, 13.0D);

    public AlbumDisplayBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    /**
     * @return The nearest of the sixteen rotation steps to the given yaw
     */
    public static int segmentFor(float degrees) {
        return net.minecraft.util.Mth.floor(degrees * 16.0F / 360.0F + 0.5F) & 15;
    }

    /**
     * @return The yaw a rotation step points at
     */
    public static float degreesFor(int segment) {
        return segment * 360.0F / 16.0F;
    }

    /**
     * @return Whether the stand will show the given item
     */
    public static boolean canDisplay(ItemStack stack) {
        if (PlayableRecord.isPlayableRecord(stack)) {
            return true;
        }
        // Vanilla discs are a tag before 1.21 and a data component from 1.21 on.
        //? if >=1.21 {
        /*return stack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE);
        *///?} else {
        return stack.is(ItemTags.MUSIC_DISCS);
        //?}
    }

    // Shared by both versions: swaps whatever the player is holding with whatever is on the stand.
    private InteractionResult swap(Level level, BlockPos pos, Player player, ItemStack held) {
        if (!(level.getBlockEntity(pos) instanceof AlbumDisplayBlockEntity display)) {
            return InteractionResult.PASS;
        }

        ItemStack shown = display.getItem();
        if (shown.isEmpty() && !canDisplay(held)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!shown.isEmpty()) {
            if (!player.addItem(shown)) {
                player.drop(shown, false);
            }
            display.setItem(ItemStack.EMPTY);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (!shown.isEmpty() || !canDisplay(held)) {
            return InteractionResult.CONSUME;
        }

        display.setItem(held.split(1));
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }

    //? if >=1.21 {
    /*@Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.phys.BlockHitResult hit) {
        return this.swap(level, pos, player, player.getMainHandItem());
    }

    public static final com.mojang.serialization.MapCodec<AlbumDisplayBlock> CODEC = simpleCodec(AlbumDisplayBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    *///?} else {
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        return this.swap(level, pos, player, player.getItemInHand(hand));
    }
    //?}

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof AlbumDisplayBlockEntity display && !display.getItem().isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), display.getItem());
            }
            super.onRemove(state, level, pos, newState, moving);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlbumDisplayBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // No half turn here, unlike a sign: the stand's lip is already on the side facing the player.
        return this.defaultBlockState().setValue(ROTATION, segmentFor(context.getRotation()));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), 16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }
}
