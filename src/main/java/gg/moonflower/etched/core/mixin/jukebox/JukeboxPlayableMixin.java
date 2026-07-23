package gg.moonflower.etched.core.mixin.jukebox;

// 1.21-only: JukeboxPlayable/tryInsertIntoJukebox is the new data-component insertion path.
// Custom Etched discs lack the vanilla JUKEBOX_PLAYABLE component, so vanilla bails out of
// tryInsertIntoJukebox; we catch that early return and insert the record ourselves.
//? if >=1.21 {
/*import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.common.item.AlbumCoverItem;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JukeboxPlayable.class)
public class JukeboxPlayableMixin {

    @Inject(method = "tryInsertIntoJukebox", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    private static void etched$insertPlayableRecord(Level level, BlockPos pos, ItemStack stack, Player player, CallbackInfoReturnable<ItemInteractionResult> cir) {
        // Album covers only play in the album jukebox, not a regular one.
        if (!PlayableRecord.isPlayableRecord(stack) || stack.getItem() instanceof AlbumCoverItem) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.JUKEBOX) && !state.getValue(JukeboxBlock.HAS_RECORD)) {
            if (!level.isClientSide()) {
                ItemStack record = stack.consumeAndReturn(1, player);
                if (level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
                    jukebox.setTheItem(record);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
                }
                player.awardStat(Stats.PLAY_RECORD);
            }
            cir.setReturnValue(ItemInteractionResult.sidedSuccess(level.isClientSide()));
        }
    }
}
*///?}
