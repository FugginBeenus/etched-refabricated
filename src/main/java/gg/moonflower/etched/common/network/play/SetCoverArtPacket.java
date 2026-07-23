package gg.moonflower.etched.common.network.play;

import gg.moonflower.etched.common.network.EtchedMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Sent client -> server when a player uploads a local image in the Album Printer. Carries the
 * already-processed, cover-sized PNG bytes to bake onto the output album cover.
 *
 * @param image The processed PNG image bytes
 */
@ApiStatus.Internal
public record SetCoverArtPacket(byte[] image) implements EtchedPacket {

    // Album art is small (a cover-sized PNG); cap generously to reject anything unexpected.
    private static final int MAX_BYTES = 256 * 1024;

    public SetCoverArtPacket(FriendlyByteBuf buf) {
        this(buf.readByteArray(MAX_BYTES));
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeByteArray(this.image);
    }

    @Override
    public ResourceLocation getPacketId() {
        return EtchedMessages.SERVER_SET_COVER_ART;
    }
}
