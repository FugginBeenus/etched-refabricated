package gg.moonflower.etched.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.WorkAtPoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class WorkAtNoteBlock extends WorkAtPoi {

    @Override
    protected void useWorkstation(ServerLevel level, Villager villager) {
        Optional<GlobalPos> jobSite = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        if (jobSite.isEmpty()) {
            return;
        }

        BlockPos pos = jobSite.get().pos();
        if (!this.playAt(level, villager, pos.below())) {
            this.playAt(level, villager, pos);
        }
    }

    private boolean playAt(ServerLevel level, Villager villager, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.NOTE_BLOCK)) {
            return false;
        }

        if (villager.getRandom().nextBoolean()) {
            state = state.cycle(NoteBlock.NOTE);
            level.setBlock(pos, state, 3);
        }

        // Triggered directly rather than through the block's own play path, which refuses to sound when
        // anything is stacked on the note block — and the bard's etching table sits right on top of it.
        level.blockEvent(pos, state.getBlock(), 0, 0);
        return true;
    }
}
