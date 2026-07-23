package gg.moonflower.etched.core.mixin.jukebox;

// 1.21-only rewrite of the jukebox hook. In 1.21 JukeboxBlockEntity holds a single `item` and
// delegates playback to a JukeboxSongPlayer, so the pre-1.21 approach (in core.mixin) no longer
// applies. When a playable (custom online) record is set, broadcast it to nearby clients so they
// stream it; when it is removed, broadcast an empty record so clients stop, and run the vanilla
// stop bookkeeping. Modeled on the NeoForge 5.1.0 build, adapted to Fabric networking.
//? if >=1.21 {
/*import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.common.item.AlbumCoverItem;
import gg.moonflower.etched.common.network.play.ClientboundPlayMusicPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity {

    @Shadow
    private ItemStack item;

    @Shadow
    @Final
    private JukeboxSongPlayer jukeboxSongPlayer;

    @Unique
    private boolean etched$playing;

    @Shadow
    public abstract void onSongChanged();

    public JukeboxBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Inject(method = "setTheItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/JukeboxSongPlayer;stop(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    public void etched$onSetItem(ItemStack stack, CallbackInfo ci) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos pos = this.getBlockPos();
        // Only hijack playback for actual single Etched records; vanilla discs (which merely carry
        // the JUKEBOX_PLAYABLE component) keep playing through vanilla's JukeboxSongPlayer, and
        // album covers only play in the album jukebox, never a regular one.
        if (stack.getItem() instanceof PlayableRecord record && record.canPlay(stack) && !(stack.getItem() instanceof AlbumCoverItem)) {
            new ClientboundPlayMusicPacket(stack.copy(), pos).sendToClients(PlayerLookup.around(serverLevel, pos.getCenter(), 64.0));
            this.etched$playing = true;
        } else if (this.etched$playing) {
            this.etched$playing = false;
            new ClientboundPlayMusicPacket(ItemStack.EMPTY, pos).sendToClients(PlayerLookup.around(serverLevel, pos.getCenter(), 64.0));
            ((JukeboxSongPlayerAccessor) (Object) this.jukeboxSongPlayer).setTicksSinceSongStarted(0L);
            serverLevel.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, pos, GameEvent.Context.of(this.getBlockState()));
            serverLevel.levelEvent(1011, pos, 0);
            this.onSongChanged();
        }
    }

    @Inject(method = "getComparatorOutput", at = @At("TAIL"), cancellable = true)
    public void etched$comparator(CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() == 0 && PlayableRecord.isPlayableRecord(this.item)) {
            cir.setReturnValue(15);
        }
    }

    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    public void etched$canPlace(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        JukeboxBlockEntity self = (JukeboxBlockEntity) (Object) this;
        if (PlayableRecord.isPlayableRecord(stack) && !(stack.getItem() instanceof AlbumCoverItem) && self.getItem(slot).isEmpty()) {
            cir.setReturnValue(true);
        }
    }
}
*///?}
