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

/**
 * A speaker that takes over a jukebox's audio: when connected, the record is heard from the speakers
 * instead of the jukebox itself (see {@code SoundTracker}). Speakers touching a jukebox are connected
 * automatically; further away they are paired to a {@link StereoBlock} sitting on top of it.
 */
public class SpeakerBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public SpeakerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
