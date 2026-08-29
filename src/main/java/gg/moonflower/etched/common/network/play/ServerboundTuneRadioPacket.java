package gg.moonflower.etched.common.network.play;

import gg.moonflower.etched.common.network.EtchedMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/** Tunes the open radio to a station, or back to its own link when {@code tuned} is false. */
@ApiStatus.Internal
public record ServerboundTuneRadioPacket(boolean tuned, BlockPos stationPos) implements EtchedPacket {

    public ServerboundTuneRadioPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBlockPos());
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeBoolean(this.tuned);
        buf.writeBlockPos(this.stationPos);
    }

    @Override
    public ResourceLocation getPacketId() {
        return EtchedMessages.SERVER_TUNE_RADIO;
    }
}
