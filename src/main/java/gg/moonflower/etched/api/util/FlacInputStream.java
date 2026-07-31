package gg.moonflower.etched.api.util;

import org.jflac.FLACDecoder;
import org.jflac.frame.Frame;
import org.jflac.metadata.StreamInfo;
import org.jflac.util.ByteData;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;

/**
 * Dynamically converts FLAC data to raw audio as the stream is read.
 */
public class FlacInputStream extends InputStream {

    private final FLACDecoder decoder;
    private final AudioFormat format;

    private ByteData block;
    private int position;
    private boolean finished;

    public FlacInputStream(InputStream source) throws IOException {
        this.decoder = new FLACDecoder(source);

        // The metadata has to be consumed before any frame will decode, and it carries the only
        // description of the sample format.
        StreamInfo info;
        try {
            this.decoder.readMetadata();
            info = this.decoder.getStreamInfo();
        } catch (Throwable t) {
            throw new IOException("Failed to read FLAC metadata", t);
        }
        if (info == null) {
            throw new IOException("Not a FLAC stream");
        }

        this.format = new AudioFormat(info.getSampleRate(), info.getBitsPerSample(), info.getChannels(), true, false);
    }

    /**
     * Decodes the next frame into the block buffer.
     *
     * @return Whether the stream has reached the end
     */
    private boolean fillBuffer() throws IOException {
        if (this.finished) {
            return true;
        }

        try {
            Frame frame = this.decoder.readNextFrame();
            if (frame == null) {
                this.finished = true;
                return true;
            }
            this.block = this.decoder.decodeFrame(frame, this.block);
        } catch (Throwable t) {
            // A truncated stream is the usual cause, and there is nothing useful left to play.
            this.finished = true;
            return true;
        }

        this.position = 0;
        return this.block == null || this.block.getLen() == 0;
    }

    private boolean exhausted() {
        return this.block == null || this.position >= this.block.getLen();
    }

    @Override
    public int read() throws IOException {
        while (this.exhausted()) {
            if (this.fillBuffer()) {
                return -1;
            }
        }
        return this.block.getData(this.position++) & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = 0;
        while (read < len) {
            while (this.exhausted()) {
                if (this.fillBuffer()) {
                    return read > 0 ? read : -1;
                }
            }

            int readLength = Math.min(this.block.getLen() - this.position, len - read);
            System.arraycopy(this.block.getData(), this.position, b, off + read, readLength);
            this.position += readLength;
            read += readLength;
        }
        return read;
    }

    @Override
    public int available() {
        return this.exhausted() ? 0 : this.block.getLen() - this.position;
    }

    public AudioFormat getFormat() {
        return this.format;
    }
}
