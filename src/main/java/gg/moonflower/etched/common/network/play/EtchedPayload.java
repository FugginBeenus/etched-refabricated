package gg.moonflower.etched.common.network.play;

// Single "tunnel" CustomPacketPayload used on 1.21+ to carry every mod packet (identified by its
// channel id) as a raw byte payload, so the existing writePacketData/readPacketData logic is reused
// instead of rewriting each packet into a typed payload. 1.21+ only; on 1.20.1 the mod uses the
// legacy channel+buffer networking directly.
//? if >=1.21 {
/*import gg.moonflower.etched.core.Etched;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EtchedPayload(ResourceLocation packetId, byte[] data) implements CustomPacketPayload {

    public static final Type<EtchedPayload> TYPE = new Type<>(gg.moonflower.etched.api.util.EtchedResourceLocation.of(Etched.MOD_ID, "tunnel"));

    public static final StreamCodec<FriendlyByteBuf, EtchedPayload> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, EtchedPayload::packetId,
            ByteBufCodecs.BYTE_ARRAY, EtchedPayload::data,
            EtchedPayload::new
    );

    @Override
    public Type<EtchedPayload> type() {
        return TYPE;
    }
}
*///?}
