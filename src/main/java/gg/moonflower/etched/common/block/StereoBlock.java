package gg.moonflower.etched.common.block;

import gg.moonflower.etched.common.blockentity.StereoBlockEntity;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.registry.EtchedBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A receiver that sits on top of a jukebox and gives it wireless speakers. Right-clicking it starts
 * link mode, after which right-clicking speakers pairs or unpairs them.
 *
 * @author Jackson
 */
public class StereoBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);

    // Players currently choosing speakers, and the stereo they are linking to. Server side only, and
    // deliberately not saved: link mode only lasts as long as the player is using it.
    private static final Map<UUID, BlockPos> LINKING = new HashMap<>();

    public StereoBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /**
     * @return The stereo the given player is currently pairing speakers to, if any
     */
    @Nullable
    public static BlockPos getLinking(Player player) {
        return LINKING.get(player.getUUID());
    }

    public static void stopLinking(Player player) {
        LINKING.remove(player.getUUID());
    }

    /**
     * @return The stereo sitting on top of the given block, if there is one
     */
    @Nullable
    public static StereoBlockEntity getStereoFor(BlockGetter level, BlockPos sourcePos) {
        BlockEntity blockEntity = level.getBlockEntity(sourcePos.above());
        return blockEntity instanceof StereoBlockEntity stereo ? stereo : null;
    }

    private static boolean isValidSource(BlockState state) {
        return state.is(Blocks.JUKEBOX) || state.getBlock() instanceof AlbumJukeboxBlock;
    }

    private InteractionResult toggleLinkMode(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        UUID id = player.getUUID();
        if (pos.equals(LINKING.get(id))) {
            LINKING.remove(id);
            player.displayClientMessage(Component.translatable("block." + Etched.MOD_ID + ".stereo.link_stop"), true);
        } else {
            LINKING.put(id, pos.immutable());
            StereoBlockEntity stereo = level.getBlockEntity(pos) instanceof StereoBlockEntity be ? be : null;
            int paired = stereo != null ? stereo.getPairedSpeakers().size() : 0;
            int max = stereo != null ? stereo.getMaxSpeakers() : StereoBlockEntity.BASE_SPEAKERS;
            int range = stereo != null ? stereo.getRange() : StereoBlockEntity.BASE_RANGE;
            player.displayClientMessage(Component.translatable("block." + Etched.MOD_ID + ".stereo.link_start", paired, max, range), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isValidSource(level.getBlockState(pos.below()));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StereoBlockEntity(pos, state);
    }

    //? if >=1.21 {
    /*public static final com.mojang.serialization.MapCodec<StereoBlock> CODEC = simpleCodec(StereoBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return this.toggleLinkMode(level, pos, player);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !this.canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }
    *///?} else {
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return this.toggleLinkMode(level, pos, player);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !this.canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }
    //?}
}
