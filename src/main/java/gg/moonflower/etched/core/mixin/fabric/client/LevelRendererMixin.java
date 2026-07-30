package gg.moonflower.etched.core.mixin.fabric.client;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.sound.StopListeningSound;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Unique
    private BlockPos pos;

    //? if >=1.21 {
    /*@Unique
    private net.minecraft.core.Holder<net.minecraft.world.item.JukeboxSong> etched$song;
    *///?}

    @Shadow
    private ClientLevel level;

    @Shadow
    protected abstract void notifyNearbyEntities(Level level, BlockPos blockPos, boolean bl);

    // In 1.21 playStreamingMusic(SoundEvent, BlockPos) became playJukeboxSong(Holder<JukeboxSong>,
    // BlockPos); the two behaviors (gate the vanilla-disc "Now Playing" toast when the jukebox is
    // obstructed, and notify nearby mobs when the sound stops) are re-targeted against it.
    //? if >=1.21 {
    /*@Inject(method = "playJukeboxSong", at = @At("HEAD"))
    public void etched$capturePos(net.minecraft.core.Holder<net.minecraft.world.item.JukeboxSong> song, BlockPos pos, CallbackInfo ci) {
        this.pos = pos;
        this.etched$song = song;
    }

    @Redirect(method = "playJukeboxSong", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setNowPlaying(Lnet/minecraft/network/chat/Component;)V"))
    public void etched$gateNowPlaying(Gui gui, Component component) {
        if (this.level.getBlockState(this.pos.above()).isAir() && PlayableRecord.canShowMessage(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5))
            gui.setNowPlaying(component);
    }

    // Replace the disc's sound the moment it's created so the change flows into both the tracking map
    // and the play() call. Must return a SimpleSoundInstance (the expression's type), so the speaker
    // sound is a SimpleSoundInstance subclass that also ticks. When speakers are connected, the tracked
    // sound follows the first speaker (falling back to the jukebox if all are removed) and the same disc
    // sound plays from the rest; each of those stops when its own speaker is broken or the record ends.
    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method = "playJukeboxSong", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forJukeboxSong(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;"))
    public net.minecraft.client.resources.sounds.SimpleSoundInstance etched$routeJukeboxToSpeakers(net.minecraft.client.resources.sounds.SimpleSoundInstance original) {
        net.minecraft.client.multiplayer.ClientLevel level = this.level;
        BlockPos jukebox = this.pos;
        if (gg.moonflower.etched.api.sound.SoundTracker.getConnectedSpeakers(level, jukebox).isEmpty()) {
            return original;
        }

        net.minecraft.sounds.SoundEvent soundEvent = this.etched$song.value().soundEvent().value();
        java.util.List<BlockPos> speakers = gg.moonflower.etched.api.sound.SoundTracker.getConnectedSpeakers(level, jukebox);

        // Companions on every speaker beyond the first; each stops when the record ends or its speaker
        // is broken.
        java.util.List<SoundInstance> companions = new java.util.ArrayList<>();
        for (int i = 1; i < speakers.size(); i++) {
            BlockPos speaker = speakers.get(i);
            companions.add(new gg.moonflower.etched.client.sound.SpeakerJukeboxSound(soundEvent, () ->
                    gg.moonflower.etched.api.sound.SoundTracker.isRecordPlaying(jukebox)
                            && level.getBlockState(speaker).getBlock() instanceof gg.moonflower.etched.common.block.SpeakerBlock
                            ? net.minecraft.world.phys.Vec3.atCenterOf(speaker)
                            : null));
        }
        gg.moonflower.etched.api.sound.SoundTracker.playSpeakerCompanions(jukebox, companions);

        // Primary: the tracked sound. Follows the first connected speaker, falling back to the jukebox
        // when none remain (vanilla stops it when the record is removed).
        return new gg.moonflower.etched.client.sound.SpeakerJukeboxSound(soundEvent, () -> {
            java.util.List<BlockPos> current = gg.moonflower.etched.api.sound.SoundTracker.getConnectedSpeakers(level, jukebox);
            return net.minecraft.world.phys.Vec3.atCenterOf(current.isEmpty() ? jukebox : current.get(0));
        });
    }
    *///?} else {
    @Redirect(method = "playStreamingMusic", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setNowPlaying(Lnet/minecraft/network/chat/Component;)V"))
    public void redirectNowPlaying(Gui gui, Component component) {
        if (this.level.getBlockState(this.pos.above()).isAir() && PlayableRecord.canShowMessage(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5))
            gui.setNowPlaying(component);
    }

    @Inject(method = "playStreamingMusic", at = @At("HEAD"))
    public void playStreamingMusic(SoundEvent soundEvent, BlockPos pos, CallbackInfo ci) {
        this.pos = pos;
    }

    @ModifyVariable(method = "playStreamingMusic", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BEFORE), index = 3)
    public SoundInstance modifySoundInstance(SoundInstance soundInstance) {
        return StopListeningSound.create(soundInstance, () -> this.notifyNearbyEntities(this.level, this.pos, false));
    }
    //?}
}
