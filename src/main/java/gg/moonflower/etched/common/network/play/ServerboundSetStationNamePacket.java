package gg.moonflower.etched.common.network.play;

import gg.moonflower.etched.common.network.EtchedMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/** Names a station from its own screen, since the name is what listeners pick from on a radio. */
@ApiStatus.Internal
public record ServerboundSetStationNamePacket(String name) implements EtchedPacket {

    /** Bounded on read so a crafted packet cannot hand the server an enormous string. */
    public static final int MAX_LENGTH = 48;

    public ServerboundSetStationNamePacket(FriendlyByteBuf buf) {
        this(buf.readUtf(MAX_LENGTH));
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeUtf(this.name, MAX_LENGTH);
    }

    @Override
    public ResourceLocation getPacketId() {
        return EtchedMessages.SERVER_SET_STATION_NAME;
    }
}
