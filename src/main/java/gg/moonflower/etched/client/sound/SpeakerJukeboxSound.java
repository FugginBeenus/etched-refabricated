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
