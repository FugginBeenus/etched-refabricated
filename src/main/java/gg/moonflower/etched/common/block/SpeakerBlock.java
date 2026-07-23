package gg.moonflower.etched.common.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * A speaker that takes over a nearby jukebox's audio: when connected, the disc's sound is emitted
 * from the speaker(s) instead of the jukebox (see {@code SoundTracker}). For now it connects simply
 * by being adjacent to the jukebox; wireless pairing via the Stereo comes later.
 */
public class SpeakerBlock extends Block {

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

    //? if >=1.21 {
    /*public static final com.mojang.serialization.MapCodec<SpeakerBlock> CODEC = simpleCodec(SpeakerBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends Block> codec() {
        return CODEC;
    }
    *///?}
}
