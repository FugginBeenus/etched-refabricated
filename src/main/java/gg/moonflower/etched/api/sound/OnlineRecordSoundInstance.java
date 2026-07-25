package gg.moonflower.etched.api.sound;

import gg.moonflower.etched.api.sound.source.AudioSource;
import gg.moonflower.etched.api.util.DownloadProgressListener;
import gg.moonflower.etched.core.Etched;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/**
 * @author Ocelot
 */
public class OnlineRecordSoundInstance extends AbstractOnlineSoundInstance implements TickableSoundInstance {

    private final Entity entity;
    // For block records: recomputed each tick so the sound can follow speakers as they connect/move.
    private java.util.function.Supplier<net.minecraft.world.phys.Vec3> positionSupplier;
    // For extra speaker sounds: stop as soon as the record driving them stops.
    private java.util.function.BooleanSupplier stopCondition;
    private boolean stopped;

    /**
     * Stops this sound as soon as the given condition reports <code>true</code>.
     *
     * @param condition The condition to test each tick
     * @return This sound, for chaining
     */
    public OnlineRecordSoundInstance stopWhen(java.util.function.BooleanSupplier condition) {
        this.stopCondition = condition;
        return this;
    }

    public OnlineRecordSoundInstance(String url, Entity entity, float volume, int attenuationDistance, DownloadProgressListener progressListener, AudioSource.AudioFileType type) {
        super(url, null, attenuationDistance, SoundSource.RECORDS, progressListener, type, entity == Minecraft.getInstance().player);
        this.volume = volume;
        this.entity = entity;
    }

    public OnlineRecordSoundInstance(String url, Entity entity, int attenuationDistance, DownloadProgressListener progressListener, AudioSource.AudioFileType type) {
        this(url, entity, 4.0F, attenuationDistance, progressListener, type);
    }

    public OnlineRecordSoundInstance(String url, double x, double y, double z, float volume, int attenuationDistance, DownloadProgressListener progressListener, AudioSource.AudioFileType type) {
        this(url, (Entity) null, volume, attenuationDistance, progressListener, type);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public OnlineRecordSoundInstance(String url, double x, double y, double z, int attenuationDistance, DownloadProgressListener progressListener, AudioSource.AudioFileType type) {
        this(url, x, y, z, 4.0F, attenuationDistance, progressListener, type);
    }

    public OnlineRecordSoundInstance(String url, java.util.function.Supplier<net.minecraft.world.phys.Vec3> position, float volume, int attenuationDistance, DownloadProgressListener progressListener, AudioSource.AudioFileType type) {
        this(url, (Entity) null, volume, attenuationDistance, progressListener, type);
        this.positionSupplier = position;
        net.minecraft.world.phys.Vec3 p = position.get();
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
    }

    @Override
    public void tick() {
        if (this.stopCondition != null && this.stopCondition.getAsBoolean()) {
            this.stopped = true;
            return;
        }
        if (this.entity != null) {
            if (!this.entity.isAlive()) {
                this.stopped = true;
            } else {
                this.x = this.entity.getX();
                this.y = this.entity.getY();
                this.z = this.entity.getZ();
            }
        } else if (this.positionSupplier != null) {
            net.minecraft.world.phys.Vec3 p = this.positionSupplier.get();
            this.x = p.x;
            this.y = p.y;
            this.z = p.z;
        }
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }
}
