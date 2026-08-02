package gg.moonflower.etched.api.util;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Reads cover art embedded in an audio file's tags.
 *
 * <p>Handles ID3v2 in MP3 and the picture block in FLAC, which between them cover most self hosted
 * audio. Both keep their tags at the very start of the file, so only the opening of a download has to
 * be read rather than the whole track.
 *
 * <p>The bytes handed back are whatever the file stored, usually JPEG and sometimes PNG, so they still
 * need decoding by something that copes with both.
 */
public final class EmbeddedArtwork {

    /** Stop reading rather than pull an entire album down chasing a picture that may not be there. */
    private static final int MAX_SCAN = 4 * 1024 * 1024;
    /** Sanity bound on a single picture, well above any realistic cover. */
    private static final int MAX_PICTURE = 12 * 1024 * 1024;

    private EmbeddedArtwork() {
    }

    /**
     * Finds cover art at the start of an audio stream.
     *
     * @param in The audio to read, positioned at the start of the file
     * @return The raw image bytes, if the file carries any
     */
    public static Optional<byte[]> read(InputStream in) {
        try {
            DataInputStream data = new DataInputStream(in);
            byte[] magic = new byte[4];
            data.readFully(magic);

            if (magic[0] == 'I' && magic[1] == 'D' && magic[2] == '3') {
                return readId3(data, magic[3]);
            }
            if (magic[0] == 'f' && magic[1] == 'L' && magic[2] == 'a' && magic[3] == 'C') {
                return readFlac(data);
            }
            return Optional.empty();
        } catch (Exception e) {
            // A truncated or unfamiliar file simply has no artwork to offer.
            return Optional.empty();
        }
    }

    // ---- ID3v2, used by MP3 ----

    private static Optional<byte[]> readId3(DataInputStream data, int majorVersion) throws IOException {
        data.readUnsignedByte();
        int flags = data.readUnsignedByte();
        int tagSize = readSynchSafe(data);
        if ((flags & 0x40) != 0) {
            // Skip an extended header, whose own size is the first field.
            int extended = majorVersion >= 4 ? readSynchSafe(data) : data.readInt();
            skip(data, Math.max(0, extended - 4));
            tagSize -= extended;
        }

        int read = 0;
        while (read < tagSize && read < MAX_SCAN) {
            byte[] idBytes = new byte[4];
            data.readFully(idBytes);
            String id = new String(idBytes, StandardCharsets.ISO_8859_1);
            if (id.charAt(0) == 0) {
                // Padding: the frames are over.
                return Optional.empty();
            }

            int frameSize = majorVersion >= 4 ? readSynchSafe(data) : data.readInt();
            data.readUnsignedShort();
            read += 10 + frameSize;
            if (frameSize <= 0 || frameSize > MAX_PICTURE) {
                return Optional.empty();
            }

            if (!"APIC".equals(id)) {
                skip(data, frameSize);
                continue;
            }

            byte[] frame = new byte[frameSize];
            data.readFully(frame);
            return pictureFromApic(frame);
        }
        return Optional.empty();
    }

    /**
     * An APIC frame is a text encoding byte, a null terminated MIME type, a picture type byte, a
     * description terminated the same way as the encoding implies, then the image itself.
     */
    private static Optional<byte[]> pictureFromApic(byte[] frame) {
        int i = 0;
        int encoding = frame[i++] & 0xFF;

        while (i < frame.length && frame[i] != 0) {
            i++;
        }
        i++;
        if (i >= frame.length) {
            return Optional.empty();
        }

        i++;

        // UTF-16 descriptions terminate with two nulls, the others with one.
        boolean wide = encoding == 1 || encoding == 2;
        if (wide) {
            while (i + 1 < frame.length && !(frame[i] == 0 && frame[i + 1] == 0)) {
                i += 2;
            }
            i += 2;
        } else {
            while (i < frame.length && frame[i] != 0) {
                i++;
            }
            i++;
        }

        if (i >= frame.length) {
            return Optional.empty();
        }

        byte[] image = new byte[frame.length - i];
        System.arraycopy(frame, i, image, 0, image.length);
        return Optional.of(image);
    }

    /** ID3 sizes are stored seven bits per byte so they cannot be mistaken for frame sync. */
    private static int readSynchSafe(DataInputStream data) throws IOException {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value = (value << 7) | (data.readUnsignedByte() & 0x7F);
        }
        return value;
    }

    // ---- FLAC ----

    /**
     * FLAC metadata blocks run from just after the marker until one is flagged last. Block type 6 is a
     * picture. jFLAC parses these but exposes no way to get the bytes back out, so it is read here.
     */
    private static Optional<byte[]> readFlac(DataInputStream data) throws IOException {
        int scanned = 0;
        while (scanned < MAX_SCAN) {
            int header = data.readUnsignedByte();
            boolean last = (header & 0x80) != 0;
            int type = header & 0x7F;
            int length = (data.readUnsignedByte() << 16) | (data.readUnsignedByte() << 8) | data.readUnsignedByte();
            scanned += 4 + length;

            if (type != 6) {
                skip(data, length);
                if (last) {
                    return Optional.empty();
                }
                continue;
            }

            skip(data, 4);
            skip(data, data.readInt());
            skip(data, data.readInt());
            skip(data, 16);

            int imageLength = data.readInt();
            if (imageLength <= 0 || imageLength > MAX_PICTURE) {
                return Optional.empty();
            }
            byte[] image = new byte[imageLength];
            data.readFully(image);
            return Optional.of(image);
        }
        return Optional.empty();
    }

    private static void skip(DataInputStream data, long count) throws IOException {
        long left = count;
        while (left > 0) {
            long skipped = data.skip(left);
            if (skipped <= 0) {
                if (data.read() < 0) {
                    throw new EOFException();
                }
                left--;
            } else {
                left -= skipped;
            }
        }
    }
}
