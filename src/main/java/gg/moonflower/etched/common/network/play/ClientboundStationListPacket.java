package gg.moonflower.etched.common.network.play;

import gg.moonflower.etched.common.network.EtchedMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/** The stations a radio can tune to, sent when its screen opens. */
@ApiStatus.Internal
public record ClientboundStationListPacket(List<Entry> stations, boolean tuned, BlockPos tunedTo) implements EtchedPacket {

    public record Entry(BlockPos pos, String name) {
    }

    public ClientboundStationListPacket(FriendlyByteBuf buf) {
        this(read(buf), buf.readBoolean(), buf.readBlockPos());
    }

    private static List<Entry> read(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<Entry> stations = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            stations.add(new Entry(buf.readBlockPos(), buf.readUtf(ServerboundSetStationNamePacket.MAX_LENGTH)));
        }
        return stations;
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeVarInt(this.stations.size());
        for (Entry entry : this.stations) {
            buf.writeBlockPos(entry.pos());
            buf.writeUtf(entry.name(), ServerboundSetStationNamePacket.MAX_LENGTH);
        }
        buf.writeBoolean(this.tuned);
        buf.writeBlockPos(this.tunedTo);
    }

    @Override
    public ResourceLocation getPacketId() {
        return EtchedMessages.CLIENT_STATION_LIST;
    }
}
