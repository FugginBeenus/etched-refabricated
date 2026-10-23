package gg.moonflower.etched.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class SpeakerBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    // Beside a music player the speaker shifts over to sit up against it, leaving a pixel's gap.
    public static final EnumProperty<Snug> SNUG = EnumProperty.create("snug", Snug.class);
    private static final double SNUG_SHIFT = 2.0D;
    private static final Map<Direction, VoxelShape[]> SHAPES = new EnumMap<>(Direction.class);

    static {
        // A bookshelf speaker: 10 wide and 12 deep, so the outline turns with it.
        VoxelShape zShape = Block.box(3.0D, 0.0D, 2.0D, 13.0D, 16.0D, 14.0D);
        VoxelShape xShape = Block.box(2.0D, 0.0D, 3.0D, 14.0D, 16.0D, 13.0D);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            VoxelShape base = facing.getAxis() == Direction.Axis.Z ? zShape : xShape;
            VoxelShape[] shapes = new VoxelShape[Snug.values().length];
            for (Snug snug : Snug.values()) {
                Direction toward = snug.toward(facing);
                shapes[snug.ordinal()] = toward == null ? base
                        : base.move(toward.getStepX() * SNUG_SHIFT / 16.0D, 0.0D, toward.getStepZ() * SNUG_SHIFT / 16.0D);
            }
            SHAPES.put(facing, shapes);
        }
    }

    public SpeakerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SNUG, Snug.NONE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING))[state.getValue(SNUG).ordinal()];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, facing).setValue(SNUG, snugFor(context.getLevel(), context.getClickedPos(), facing));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        if (direction == facing.getClockWise() || direction == facing.getCounterClockWise()) {
            return state.setValue(SNUG, snugFor(level, pos, facing));
        }
        return state;
    }

    private static Snug snugFor(BlockGetter level, BlockPos pos, Direction facing) {
        boolean left = isMusicPlayer(level.getBlockState(pos.relative(facing.getClockWise())));
        boolean right = isMusicPlayer(level.getBlockState(pos.relative(facing.getCounterClockWise())));
        return left == right ? Snug.NONE : left ? Snug.LEFT : Snug.RIGHT;
    }

    private static boolean isMusicPlayer(BlockState state) {
        Block block = state.getBlock();
        return block instanceof JukeboxBlock || block instanceof AlbumJukeboxBlock || block instanceof RadioBlock || block instanceof StereoBlock;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        BlockState turned = state.rotate(mirror.getRotation(state.getValue(FACING)));
        return mirror == Mirror.NONE ? turned : turned.setValue(SNUG, state.getValue(SNUG).flipped());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SNUG);
    }

    // Left and right as seen from in front of the speaker.
    public enum Snug implements StringRepresentable {
        NONE("none"), LEFT("left"), RIGHT("right");

        private final String name;

        Snug(String name) {
            this.name = name;
        }

        @Nullable
        public Direction toward(Direction facing) {
            return this == LEFT ? facing.getClockWise() : this == RIGHT ? facing.getCounterClockWise() : null;
        }

        public Snug flipped() {
            return this == LEFT ? RIGHT : this == RIGHT ? LEFT : NONE;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new gg.moonflower.etched.common.blockentity.SpeakerBlockEntity(pos, state);
    }

    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }

    private net.minecraft.world.InteractionResult openVolume(net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        if (level.getBlockEntity(pos) instanceof gg.moonflower.etched.common.blockentity.SpeakerBlockEntity speaker) {
            player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                    (id, inventory, p) -> new gg.moonflower.etched.common.menu.SpeakerMenu(id, inventory, speaker),
                    net.minecraft.network.chat.Component.translatable("container." + gg.moonflower.etched.core.Etched.MOD_ID + ".speaker")));
        }
        return net.minecraft.world.InteractionResult.CONSUME;
    }

    // While a player has a stereo in link mode, clicking speakers pairs and unpairs them with it.
    private net.minecraft.world.InteractionResult tryPair(net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        if (level.isClientSide()) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        BlockPos stereoPos = StereoBlock.getLinking(player);
        if (stereoPos == null) {
            return this.openVolume(level, pos, player);
        }
        // Sneaking leaves link mode without touching this speaker, so a player can always get back to
        // the volume screen.
        if (player.isShiftKeyDown()) {
            StereoBlock.stopLinking(player);
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("block." + gg.moonflower.etched.core.Etched.MOD_ID + ".stereo.link_stop"), true);
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        if (!(level.getBlockEntity(stereoPos) instanceof gg.moonflower.etched.common.blockentity.StereoBlockEntity stereo)) {
            StereoBlock.stopLinking(player);
            return this.openVolume(level, pos, player);
        }

        String key = "block." + gg.moonflower.etched.core.Etched.MOD_ID + ".speaker.";
        if (!stereo.isPaired(pos) && !stereo.inRange(pos)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(key + "out_of_range", stereo.getRange()), true);
            return net.minecraft.world.InteractionResult.CONSUME;
        }

        boolean paired = stereo.togglePaired(pos);
        int active = stereo.getPairedSpeakers().size();
        if (paired && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            gg.moonflower.etched.core.registry.EtchedAdvancements.award(serverPlayer, gg.moonflower.etched.core.registry.EtchedAdvancements.AUDIOPHILE);
            if (active >= 6) {
                gg.moonflower.etched.core.registry.EtchedAdvancements.award(serverPlayer, gg.moonflower.etched.core.registry.EtchedAdvancements.GOES_UP_TO_11);
            }
        }
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(key + (paired ? "paired" : "unpaired"), active, stereo.getMaxSpeakers()), true);
        return net.minecraft.world.InteractionResult.CONSUME;
    }

    //? if >=1.21 {
    /*@Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hit) {
        return this.tryPair(level, pos, player);
    }
    *///?} else {
    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        return this.tryPair(level, pos, player);
    }
    //?}

    //? if >=1.21 {
    /*public static final com.mojang.serialization.MapCodec<SpeakerBlock> CODEC = simpleCodec(SpeakerBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    *///?}
}
