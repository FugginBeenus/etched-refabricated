package gg.moonflower.etched.common.network.play;

import gg.moonflower.etched.common.network.EtchedMessages;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.Collection;

/**
 * A message intended for the specified message handler.
 *
 * @author Ocelot
 */
@ApiStatus.Internal
public interface EtchedPacket {

    /**
     * Writes the raw message data to the data stream.
     *
     * @param buf The buffer to write to (a RegistryFriendlyByteBuf on 1.21+)
     */
    void writePacketData(FriendlyByteBuf buf) throws IOException;

    ResourceLocation getPacketId();

    //? if >=1.21 {
    /*private EtchedPayload etched$makePayload(net.minecraft.core.RegistryAccess access) {
        net.minecraft.network.RegistryFriendlyByteBuf buf = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), access);
        try {
            writePacketData(buf);
        } catch (Exception exception) {
            EtchedMessages.LOGGER.error("Could not write buf for packet " + getPacketId(), exception);
        }
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new EtchedPayload(getPacketId(), data);
    }

    default void sendToClient(ServerPlayer player) {
        ServerPlayNetworking.send(player, etched$makePayload(player.level().registryAccess()));
    }

    default void sendToClients(Collection<ServerPlayer> players) {
        players.forEach(serverPlayer -> ServerPlayNetworking.send(serverPlayer, etched$makePayload(serverPlayer.level().registryAccess())));
    }

    @Environment(EnvType.CLIENT)
    default void sendToServer() {
        ClientPlayNetworking.send(etched$makePayload(net.minecraft.client.Minecraft.getInstance().getConnection().registryAccess()));
    }
    *///?} else {
    private FriendlyByteBuf getBuf() {
        FriendlyByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        try {
            writePacketData(buf);
        } catch (Exception exception) {
            EtchedMessages.LOGGER.error("Could not write buf for packet " + getPacketId(), exception);
        }
        return buf;
    }

    default void sendToClient(ServerPlayer player) {
        ServerPlayNetworking.send(player, getPacketId(), getBuf());
    }

    default void sendToClients(Collection<ServerPlayer> players) {
        var buf = getBuf();
        players.forEach(serverPlayer -> ServerPlayNetworking.send(serverPlayer, getPacketId(), buf));
    }

    default void sendToServer() {
        ClientPlayNetworking.send(getPacketId(), getBuf());
    }
    //?}
}
