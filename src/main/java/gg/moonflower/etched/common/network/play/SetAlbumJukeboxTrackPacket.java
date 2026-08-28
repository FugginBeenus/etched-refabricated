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
 */
@ApiStatus.Internal
public record SetAlbumJukeboxTrackPacket(BlockPos pos, int playingIndex, int track) implements EtchedPacket {

    public SetAlbumJukeboxTrackPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarInt(this.playingIndex);
        buf.writeVarInt(this.track);
    }

    @Override
    public ResourceLocation getPacketId() {
        return EtchedMessages.SHARED_SET_ALBUM_JUKEBOX_TRACK;
    }
}
