package gg.moonflower.etched.api.sound.stream;

import net.minecraft.client.sounds.AudioStream;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Starts a track part way in, by decoding the beginning and throwing it away.
 *
 * <p>Nothing in the audio path can seek, so this is the only way to join a station's track already in
 * progress. It is done once, up front, on a background thread, rather than while the sound is playing.
 */
public final class AudioSkip {

    /** Long enough to get through a skip quickly without holding much at a time. */
    private static final int CHUNK = 1 << 16;

    private AudioSkip() {
    }

    public static AudioStream skip(AudioStream source, float seconds) throws IOException {
        if (seconds <= 0.0F) {
            return source;
        }

        AudioFormat format = source.getFormat();
        long bytesPerSecond = (long) format.getSampleRate() * format.getChannels() * (format.getSampleSizeInBits() / Byte.SIZE);
        if (bytesPerSecond <= 0L) {
            return source;
        }

        long remaining = (long) (seconds * bytesPerSecond);
        while (remaining > 0L) {
            ByteBuffer discarded = source.read((int) Math.min(remaining, CHUNK));
            int read = discarded.remaining();
            // The track is shorter than the point being skipped to, so there is nothing left to play.
            if (read <= 0) {
                break;
            }
            remaining -= read;
        }
        return source;
    }
}
