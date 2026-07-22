package gg.moonflower.etched.common.network;

import gg.moonflower.etched.common.network.play.*;
import gg.moonflower.etched.common.network.play.handler.EtchedClientPlayPacketHandler;
import gg.moonflower.etched.common.network.play.handler.EtchedServerPlayPacketHandler;
import gg.moonflower.etched.core.Etched;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;

public class EtchedMessages {

    public static final Logger LOGGER = LogManager.getLogger("Etched/Networking");

    public static final ResourceLocation CLIENT_INVALID_ETCH_URL = key("client_invalid_url");
    public static final ResourceLocation CLIENT_PLAY_ENTITY_MUSIC = key("client_play_entity_music");
    public static final ResourceLocation CLIENT_PLAY_MUSIC = key("client_play_music");
    public static final ResourceLocation CLIENT_SET_URL = key("client_set_url");
    public static final ResourceLocation SERVER_EDIT_MUSIC_LABEL = key("server_edit_music_label");
    public static final ResourceLocation SERVER_SET_URL = key("server_set_url");
    public static final ResourceLocation SHARED_SET_ALBUM_JUKEBOX_TRACK = key("shared_set_album_jukebox_track");

    private static ResourceLocation key(String name) {
        return gg.moonflower.etched.api.util.EtchedResourceLocation.of(Etched.MOD_ID, name);
    }

    //? if >=1.21 {
    /*private static final java.util.Map<ResourceLocation, java.util.function.BiConsumer<FriendlyByteBuf, Minecraft>> CLIENT_HANDLERS = new java.util.HashMap<>();
    private static final java.util.Map<ResourceLocation, TriConsumer> SERVER_HANDLERS = new java.util.HashMap<>();

    private interface TriConsumer {
        void accept(FriendlyByteBuf buf, net.minecraft.server.MinecraftServer server, ServerPlayer player);
    }

    public static synchronized void init() {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(EtchedPayload.TYPE, EtchedPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(EtchedPayload.TYPE, EtchedPayload.CODEC);

        server_register(ServerboundSetUrlPacket.class, SERVER_SET_URL, EtchedServerPlayPacketHandler::handleSetUrl);
        server_register(ServerboundEditMusicLabelPacket.class, SERVER_EDIT_MUSIC_LABEL, EtchedServerPlayPacketHandler::handleEditMusicLabel);
        server_register(SetAlbumJukeboxTrackPacket.class, SHARED_SET_ALBUM_JUKEBOX_TRACK, EtchedServerPlayPacketHandler::handleSetAlbumJukeboxTrack);

        ServerPlayNetworking.registerGlobalReceiver(EtchedPayload.TYPE, (payload, context) -> {
            TriConsumer handler = SERVER_HANDLERS.get(payload.packetId());
            if (handler != null) {
                net.minecraft.network.RegistryFriendlyByteBuf buf = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(payload.data()), context.player().registryAccess());
                context.player().server.execute(() -> handler.accept(buf, context.player().server, context.player()));
            }
        });
    }

    @Environment(EnvType.CLIENT)
    public static synchronized void initClient() {
        client_register(ClientboundInvalidEtchUrlPacket.class, CLIENT_INVALID_ETCH_URL, EtchedClientPlayPacketHandler::handleSetInvalidEtch);
        client_register(ClientboundPlayEntityMusicPacket.class, CLIENT_PLAY_ENTITY_MUSIC, EtchedClientPlayPacketHandler::handlePlayEntityMusicPacket);
        client_register(ClientboundPlayMusicPacket.class, CLIENT_PLAY_MUSIC, EtchedClientPlayPacketHandler::handlePlayMusicPacket);
        client_register(ClientboundSetUrlPacket.class, CLIENT_SET_URL, EtchedClientPlayPacketHandler::handleSetUrl);
        client_register(SetAlbumJukeboxTrackPacket.class, SHARED_SET_ALBUM_JUKEBOX_TRACK, EtchedClientPlayPacketHandler::handleSetAlbumJukeboxTrack);

        ClientPlayNetworking.registerGlobalReceiver(EtchedPayload.TYPE, (payload, context) -> {
            java.util.function.BiConsumer<FriendlyByteBuf, Minecraft> handler = CLIENT_HANDLERS.get(payload.packetId());
            if (handler != null) {
                net.minecraft.network.RegistryFriendlyByteBuf buf = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(payload.data()), Minecraft.getInstance().getConnection().registryAccess());
                context.client().execute(() -> handler.accept(buf, context.client()));
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private static <MSG extends EtchedPacket> void client_register(Class<MSG> clazz, ResourceLocation packet_id, EtchedClientPacketHandlerInterface<MSG> packetHandler) {
        CLIENT_HANDLERS.put(packet_id, (buf, client) -> {
            try {
                packetHandler.handle(clazz.getDeclaredConstructor(FriendlyByteBuf.class).newInstance(buf), client);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOGGER.error(e);
            }
        });
    }

    private static <MSG extends EtchedPacket> void server_register(Class<MSG> clazz, ResourceLocation packet_id, EtchedServerPacketHandlerInterface<MSG> packetHandler) {
        SERVER_HANDLERS.put(packet_id, (buf, server, player) -> {
            try {
                packetHandler.handle(clazz.getDeclaredConstructor(FriendlyByteBuf.class).newInstance(buf), server, player);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOGGER.error(e);
            }
        });
    }
    *///?} else {
    public static synchronized void init() {
        server_register(ServerboundSetUrlPacket.class, SERVER_SET_URL, EtchedServerPlayPacketHandler::handleSetUrl);
        server_register(ServerboundEditMusicLabelPacket.class, SERVER_EDIT_MUSIC_LABEL, EtchedServerPlayPacketHandler::handleEditMusicLabel);
        server_register(SetAlbumJukeboxTrackPacket.class, SHARED_SET_ALBUM_JUKEBOX_TRACK, EtchedServerPlayPacketHandler::handleSetAlbumJukeboxTrack);
    }

    @Environment(EnvType.CLIENT)
    public static synchronized void initClient() {
        client_register(ClientboundInvalidEtchUrlPacket.class, CLIENT_INVALID_ETCH_URL, EtchedClientPlayPacketHandler::handleSetInvalidEtch);
        client_register(ClientboundPlayEntityMusicPacket.class, CLIENT_PLAY_ENTITY_MUSIC, EtchedClientPlayPacketHandler::handlePlayEntityMusicPacket);
        client_register(ClientboundPlayMusicPacket.class, CLIENT_PLAY_MUSIC, EtchedClientPlayPacketHandler::handlePlayMusicPacket);
        client_register(ClientboundSetUrlPacket.class, CLIENT_SET_URL, EtchedClientPlayPacketHandler::handleSetUrl);
        client_register(SetAlbumJukeboxTrackPacket.class, SHARED_SET_ALBUM_JUKEBOX_TRACK, EtchedClientPlayPacketHandler::handleSetAlbumJukeboxTrack);
    }

    @Environment(EnvType.CLIENT)
    private static <MSG extends EtchedPacket> void client_register(Class<MSG> clazz, ResourceLocation packet_id, EtchedClientPacketHandlerInterface<MSG> packetHandler) {
        ClientPlayNetworking.registerGlobalReceiver(packet_id, (client, handler, buf, responseSender) -> {
            try {
                packetHandler.handle(clazz.getDeclaredConstructor(FriendlyByteBuf.class).newInstance(buf), client);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOGGER.error(e);
            }
        });
    }

    private static <MSG extends EtchedPacket> void server_register(Class<MSG> clazz, ResourceLocation packet_id, EtchedServerPacketHandlerInterface<MSG> packetHandler) {
        ServerPlayNetworking.registerGlobalReceiver(packet_id, (server, player, handler, buf, responseSender) -> {
            try {
                packetHandler.handle(clazz.getDeclaredConstructor(FriendlyByteBuf.class).newInstance(buf), server, player);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOGGER.error(e);
            }
        });
    }
    //?}
}
