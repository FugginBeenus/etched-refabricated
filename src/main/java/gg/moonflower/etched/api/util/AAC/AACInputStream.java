package gg.moonflower.etched.api.util.AAC;

import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;

public class AACInputStream extends AsynchronousAudioInputStream {
    private final ADTSDemultiplexer adts;
    private final Decoder decoder;
    private final SampleBuffer sampleBuffer;
    private AudioFormat audioFormat = null;
    private byte[] saved;

    public AACInputStream(InputStream in) throws IOException {
        super(in, new AudioFormat(0.0F, -1, -1, true, true), -1L);
        this.adts = new ADTSDemultiplexer(in);
        this.decoder = new Decoder(this.adts.getDecoderSpecificInfo());
        this.sampleBuffer = new SampleBuffer();
        this.getFormat();
    }

    @Override
    public AudioFormat getFormat() {
        if (this.audioFormat == null) {
            try {
                this.decoder.decodeFrame(this.adts.readNextFrame(), this.sampleBuffer);
                this.audioFormat = new AudioFormat(
                        this.sampleBuffer.getSampleRate(), this.sampleBuffer.getBitsPerSample(), this.sampleBuffer.getChannels(), true, true
                );
                this.saved = this.sampleBuffer.getData();
            } catch (IOException e) {
                return null;
            }
        }

        return this.audioFormat;
    }

    @Override
    public void execute() {
        try {
            if (this.saved == null) {
                this.decoder.decodeFrame(this.adts.readNextFrame(), this.sampleBuffer);
                this.buffer.write(this.sampleBuffer.getData());
            } else {
                this.buffer.write(this.saved);
                this.saved = null;
            }
        } catch (IOException e) {
            this.buffer.close();
        }
    }
}
