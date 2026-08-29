package gg.moonflower.etched.common.network.play;

import gg.moonflower.etched.common.network.EtchedMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Reports that a tuned radio finished the track on air, so the station can move on. Playback runs on the
 * client, so the server has no other way to know a track has ended.
 *
 * <p>Carries the link that finished and how long it ran. The length teaches the station how long that
 * track is, so it can keep its own schedule afterwards with nobody listening at all.
 *
 * <p>Every listener reports, and the station only acts on the first, ignoring the rest because by then
 * it is already on something else.
 */
@ApiStatus.Internal
public record ServerboundStationTrackEndedPacket(BlockPos radioPos, String finishedUrl, int playedTicks) implements EtchedPacket {

    public ServerboundStationTrackEndedPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readUtf(), buf.readVarInt());
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.radioPos);
        buf.writeUtf(this.finishedUrl);
        buf.writeVarInt(this.playedTicks);
    }

    @Override
    public ResourceLocation getPacketId() {
        return EtchedMessages.SERVER_STATION_TRACK_ENDED;
    }
}
