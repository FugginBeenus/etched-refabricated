package gg.moonflower.etched.api.sound;

import gg.moonflower.etched.api.sound.source.AudioSource;
import gg.moonflower.etched.api.util.DownloadProgressListener;
import gg.moonflower.etched.client.sound.SharedAudioBuffer;
import gg.moonflower.etched.client.sound.EmptyAudioStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class SpeakerSoundInstance extends OnlineRecordSoundInstance {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final DownloadProgressListener NO_PROGRESS = new DownloadProgressListener() {
        @Override
        public void progressStartRequest(Component component) {
        }

        @Override
        public void progressStartDownload(float size) {
        }

        @Override
        public void progressStagePercentage(int percentage) {
        }

        @Override
        public void progressStartLoading() {
        }

        @Override
        public void onSuccess() {
        }

        @Override
        public void onFail() {
        }
    };

    private final String url;
    private final AudioSource.AudioFileType type;

    public SpeakerSoundInstance(String url, double x, double y, double z, float volume, int attenuationDistance, AudioSource.AudioFileType type) {
        super(url, x, y, z, volume, attenuationDistance, NO_PROGRESS, type);
        this.url = url;
        this.type = type;
    }

    public SpeakerSoundInstance(String url, java.util.function.Supplier<net.minecraft.world.phys.Vec3> position, float volume, int attenuationDistance, AudioSource.AudioFileType type) {
        super(url, position, volume, attenuationDistance, NO_PROGRESS, type);
        this.url = url;
        this.type = type;
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean repeatInstantly) {
        return SharedAudioBuffer.get(this.url, this.type).thenApply(SharedAudioBuffer::openReader).exceptionally(e -> {
            LOGGER.error("Failed to load speaker audio from url: {}", this.url, e);
            return EmptyAudioStream.INSTANCE;
        });
    }
}
