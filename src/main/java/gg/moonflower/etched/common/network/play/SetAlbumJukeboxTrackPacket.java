package gg.moonflower.etched.common.network.play;

import gg.moonflower.etched.common.network.EtchedMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Carries the jukebox's position so a listener can follow a track change without having the screen
 * open. Without it, only the player looking at the menu heard the change and everyone else nearby
 * stayed on the old track.
 *
 * <p>It also carries whether the jukebox should be quiet. The block update that holds the mode and the
 * paused flag is sent at the end of the tick and so can arrive after this, which left the client
 * deciding with stale state.
 */
@ApiStatus.Internal
public record SetAlbumJukeboxTrackPacket(BlockPos pos, int playingIndex, int track, boolean silenced) implements EtchedPacket {

    public SetAlbumJukeboxTrackPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarInt(this.playingIndex);
        buf.writeVarInt(this.track);
        buf.writeBoolean(this.silenced);
    }

    @Override
    public ResourceLocation getPacketId() {
        return EtchedMessages.SHARED_SET_ALBUM_JUKEBOX_TRACK;
    }
}
