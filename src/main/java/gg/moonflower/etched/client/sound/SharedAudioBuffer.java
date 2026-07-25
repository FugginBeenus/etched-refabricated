package gg.moonflower.etched.client.sound;

import gg.moonflower.etched.api.sound.AbstractOnlineSoundInstance;
import gg.moonflower.etched.api.sound.source.AudioSource;
import gg.moonflower.etched.api.sound.stream.MonoWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.sounds.AudioStream;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A track decoded once into a single in-memory PCM buffer that any number of sounds can play from.
 * <p>
 * Playing the same record from several speakers used to give each sound its own download and decode,
 * so they finished at different times and started seconds apart. Here the work happens once per URL
 * and every speaker takes a lightweight reader over the same samples. Because they all wait on the
 * same future, they are released together and stay in sync as OpenAL plays each at real time.
 *
 * @author Jackson
 */
@Environment(EnvType.CLIENT)
public final class SharedAudioBuffer {

    // Mono 16-bit 44.1kHz is ~5MB/minute, so this allows roughly 25 minutes of audio per track.
    private static final int MAX_BYTES = 128 * 1024 * 1024;
    private static final int READ_CHUNK = 65536;

    private static final Map<String, CompletableFuture<SharedAudioBuffer>> CACHE = new ConcurrentHashMap<>();

    private final AudioFormat format;
    private final byte[] samples;

    private SharedAudioBuffer(AudioFormat format, byte[] samples) {
        this.format = format;
        this.samples = samples;
    }

    /**
     * Retrieves the decoded buffer for a track, decoding it if this is the first request. Concurrent
     * requests for the same url share a single decode.
     *
     * @param url  The track to decode
     * @param type The type of audio to accept
     * @return A future to the shared buffer
     */
    public static CompletableFuture<SharedAudioBuffer> get(String url, AudioSource.AudioFileType type) {
        return CACHE.computeIfAbsent(url, key -> SoundCache.getAudioStream(key, null, type)
                .thenCompose(AudioSource::openStream)
                .thenApplyAsync(stream -> {
                    // Always mono: OpenAL only attenuates and positions single-channel audio.
                    try (AudioStream decoded = new MonoWrapper(AbstractOnlineSoundInstance.decodeAudio(stream, false))) {
                        return new SharedAudioBuffer(decoded.getFormat(), drain(decoded));
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }, Util.backgroundExecutor()));
    }

    private static byte[] drain(AudioStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while (out.size() < MAX_BYTES) {
            ByteBuffer buffer = stream.read(READ_CHUNK);
            int available = buffer.remaining();
            if (available <= 0) {
                break;
            }
            byte[] chunk = new byte[available];
            buffer.get(chunk);
            out.write(chunk);
        }
        return out.toByteArray();
    }

    /**
     * @return A new stream over these samples, starting from the beginning
     */
    public AudioStream openReader() {
        return new Reader();
    }

    /**
     * Drops every cached track. Called when leaving a world so buffers are not held forever.
     */
    public static void clear() {
        CACHE.clear();
    }

    private class Reader implements AudioStream {

        private int position;

        @Override
        public AudioFormat getFormat() {
            return SharedAudioBuffer.this.format;
        }

        @Override
        public ByteBuffer read(int amount) {
            byte[] samples = SharedAudioBuffer.this.samples;
            int remaining = samples.length - this.position;
            if (remaining <= 0) {
                return BufferUtils.createByteBuffer(0);
            }

            // Never split a frame, or the samples after it would be read misaligned.
            int frameSize = Math.max(1, SharedAudioBuffer.this.format.getFrameSize());
            int length = Math.min(amount, remaining);
            if (length > frameSize) {
                length -= length % frameSize;
            }

            ByteBuffer buffer = BufferUtils.createByteBuffer(length);
            buffer.put(samples, this.position, length);
            buffer.flip();
            this.position += length;
            return buffer;
        }

        @Override
        public void close() {
        }
    }
}
