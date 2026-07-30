package gg.moonflower.etched.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * A vanilla jukebox disc sound played from a speaker. It is built exactly like the vanilla jukebox
 * sound ({@link SimpleSoundInstance#forJukeboxSong}) so it can replace it in place, but it also ticks:
 * each tick it re-reads where it should play from and how loud. That lets it follow speakers as they
 * are placed or broken, honour the per-speaker and master volume sliders live (the same 0-1 squared
 * curve custom discs use), and stop entirely when its source supplier returns {@code null} (its speaker
 * was broken, or the record was removed).
 *
 * @author Jackson
 */
@Environment(EnvType.CLIENT)
public class SpeakerJukeboxSound extends SimpleSoundInstance implements TickableSoundInstance {

    private final Supplier<Vec3> source;
    private final DoubleSupplier gain;
    private boolean stopped;

    public SpeakerJukeboxSound(SoundEvent soundEvent, Supplier<Vec3> source, DoubleSupplier gain) {
        // The SoundEvent 11-arg constructor forJukeboxSong uses is private; this public ResourceLocation
        // form (present on both versions) gives the same linear-attenuation, records-category sound.
        super(soundEvent.getLocation(), SoundSource.RECORDS, (float) gain.getAsDouble(), 1.0F, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.LINEAR, 0.0, 0.0, 0.0, false);
        this.source = source;
        this.gain = gain;
        Vec3 pos = source.get();
        if (pos != null) {
            this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
        }
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }

    @Override
    public void tick() {
        Vec3 pos = this.source.get();
        if (pos == null) {
            this.stopped = true;
            return;
        }
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.volume = (float) this.gain.getAsDouble();
    }
}
