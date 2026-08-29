package gg.moonflower.etched.common.network.play.handler;

import gg.moonflower.etched.common.item.SimpleMusicLabelItem;
import gg.moonflower.etched.common.menu.AlbumJukeboxMenu;
import gg.moonflower.etched.common.menu.AlbumPrinterMenu;
import gg.moonflower.etched.common.menu.EtchingMenu;
import gg.moonflower.etched.common.menu.RadioMenu;
//import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ServerboundEditMusicLabelPacket;
import gg.moonflower.etched.common.network.play.ServerboundSetUrlPacket;
import gg.moonflower.etched.common.network.play.SetAlbumJukeboxTrackPacket;
import gg.moonflower.etched.common.network.play.SetCoverArtPacket;
import gg.moonflower.etched.core.registry.EtchedItems;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EtchedServerPlayPacketHandler {

    public static void handleSetUrl(ServerboundSetUrlPacket pkt, MinecraftServer server, ServerPlayer player) {
        if (player == null) {
            return;
        }

        // Checked here rather than in the screen. This is the only point the server accepts a URL from a
        // client, for the etching table and the radio both, so a client that never opens the screen still
        // cannot get an unlisted domain past it. An empty string is the field being cleared.
        String url = pkt.url();
        if (!url.isEmpty() && !gg.moonflower.etched.core.fabric.EtchedConfig.isDomainAllowed(url)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.etched.domain_not_allowed"), true);
            return;
        }

        if (player.containerMenu instanceof EtchingMenu menu) {
            server.execute(() -> menu.setUrl(url));
        } else if (player.containerMenu instanceof RadioMenu menu) {
            server.execute(() -> menu.setUrl(url));
        }
    }

    public static void handleSetStationName(gg.moonflower.etched.common.network.play.ServerboundSetStationNamePacket pkt, MinecraftServer server, ServerPlayer player) {
        if (player == null) {
            return;
        }
        if (player.containerMenu instanceof gg.moonflower.etched.common.menu.RadioStationMenu menu) {
            server.execute(() -> menu.setStationName(pkt.name()));
        }
    }

    public static void handleEditMusicLabel(ServerboundEditMusicLabelPacket pkt, MinecraftServer server, ServerPlayer player) {
        int slot = pkt.slot();
        if (!Inventory.isHotbarSlot(slot) && slot != 40) {
            return;
        }

        if (player == null) {
            return;
        }

        ItemStack labelStack = player.getInventory().getItem(slot);
        if (!labelStack.is(EtchedItems.MUSIC_LABEL.asItem())) {
            return;
        }

        server.execute(() -> {
            SimpleMusicLabelItem.setTitle(labelStack, StringUtils.normalizeSpace(pkt.title()));
            SimpleMusicLabelItem.setAuthor(labelStack, StringUtils.normalizeSpace(pkt.author()));
        });
    }

    public static void handleSetCoverArt(SetCoverArtPacket pkt, MinecraftServer server, ServerPlayer player) {
        if (player == null) {
            return;
        }
        if (player.containerMenu instanceof AlbumPrinterMenu menu) {
            server.execute(() -> menu.setPendingImage(pkt.image()));
        }
    }

    public static void handleSetAlbumJukeboxTrack(SetAlbumJukeboxTrackPacket pkt, MinecraftServer server, ServerPlayer player) {
        if (player == null) {
            return;
        }

        if (player.containerMenu instanceof AlbumJukeboxMenu menu) {
            server.execute(() -> {
                ServerLevel level = player.serverLevel();
                if (menu.setPlayingTrack(level, pkt)) {
                    // The menu's position is the authority; a client does not get to name the block it
                    // is changing.
                    var packet = new SetAlbumJukeboxTrackPacket(menu.getPos(), pkt.playingIndex(), pkt.track(), false);
                    packet.sendToClients(PlayerLookup.tracking(level, level.getChunkAt(menu.getPos()).getPos()));
                    //EtchedMessages.PLAY.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(menu.getPos())), new SetAlbumJukeboxTrackPacket(pkt.playingIndex(), pkt.track()));
                }
            });
        }
    }
}
