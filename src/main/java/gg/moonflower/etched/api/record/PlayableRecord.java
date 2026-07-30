package gg.moonflower.etched.api.record;

import gg.moonflower.etched.api.sound.SoundTracker;
import gg.moonflower.etched.common.network.play.ClientboundPlayEntityMusicPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
//? if >=1.21 {
/*import net.minecraft.core.component.DataComponents;
*///?} else {
import net.minecraft.world.item.RecordItem;
//?}

import java.net.Proxy;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PlayableRecord {

    static boolean isPlayableRecord(ItemStack stack) {
        //? if >=1.21 {
        /*return stack.getItem() instanceof PlayableRecord && ((PlayableRecord) stack.getItem()).canPlay(stack) || stack.has(DataComponents.JUKEBOX_PLAYABLE);
        *///?} else {
        return stack.getItem() instanceof PlayableRecord && ((PlayableRecord) stack.getItem()).canPlay(stack) || stack.getItem() instanceof RecordItem;
        //?}
    }

    @Environment(EnvType.CLIENT)
    static boolean canShowMessage(double x, double y, double z) {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null || player.distanceToSqr(x, y, z) <= 4096.0;
    }

    static void playEntityRecord(Entity entity, ItemStack record, boolean restart) {
        var packet = new ClientboundPlayEntityMusicPacket(record, entity, restart);
        packet.sendToClients(PlayerLookup.tracking(entity));
        //EtchedMessages.PLAY.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new ClientboundPlayEntityMusicPacket(record, entity, restart));
    }

    static void stopEntityRecord(Entity entity) {
        var packet = new ClientboundPlayEntityMusicPacket(entity);
        packet.sendToClients(PlayerLookup.tracking(entity));
        //EtchedMessages.PLAY.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new ClientboundPlayEntityMusicPacket(entity));
    }

    static Optional<TrackData[]> getStackMusic(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof PlayableRecord record)) {
            return Optional.empty();
        }
        return record.getMusic(stack);
    }

    static Optional<TrackData> getStackAlbum(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof PlayableRecord record)) {
            return Optional.empty();
        }
        return record.getAlbum(stack);
    }

    static int getStackTrackCount(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof PlayableRecord record)) {
            return 0;
        }
        return record.getTrackCount(stack);
    }

    default boolean canPlay(ItemStack stack) {
        return this.getMusic(stack).isPresent();
    }

    @Environment(EnvType.CLIENT)
    default Optional<? extends SoundInstance> createEntitySound(ItemStack stack, Entity entity, int track, int attenuationDistance) {
        return track < 0 ? Optional.empty() : this.getMusic(stack).filter(tracks -> track < tracks.length).map(tracks -> SoundTracker.getEtchedRecord(tracks[track].url(), tracks[track].getDisplayName(), entity, attenuationDistance, false));
    }

    @Environment(EnvType.CLIENT)
    default Optional<? extends SoundInstance> createEntitySound(ItemStack stack, Entity entity, int track) {
        return this.createEntitySound(stack, entity, track, 16);
    }

    @Environment(EnvType.CLIENT)
    CompletableFuture<AlbumCover> getAlbumCover(ItemStack stack, Proxy proxy, ResourceManager resourceManager);

    Optional<TrackData[]> getMusic(ItemStack stack);

    Optional<TrackData> getAlbum(ItemStack stack);

    int getTrackCount(ItemStack stack);
}
