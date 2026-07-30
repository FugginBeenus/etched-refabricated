package gg.moonflower.etched.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.api.sound.SoundTracker;
import gg.moonflower.etched.common.blockentity.AlbumJukeboxBlockEntity;
import gg.moonflower.etched.common.item.AlbumCoverItem;
import gg.moonflower.etched.common.menu.AlbumJukeboxMenu;
import gg.moonflower.etched.core.registry.EtchedItems;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.SetAlbumJukeboxTrackPacket;
import gg.moonflower.etched.core.Etched;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Optional;

public class AlbumJukeboxScreen extends AbstractContainerScreen<AlbumJukeboxMenu> {

    private static final ResourceLocation CONTAINER_LOCATION = gg.moonflower.etched.api.util.EtchedResourceLocation.of("textures/gui/container/dispenser.png");
    private static final Component NOW_PLAYING = Component.translatable("screen." + Etched.MOD_ID + ".album_jukebox.now_playing").withStyle(ChatFormatting.YELLOW);
    private static final Component UNLOAD = Component.literal("Unload");
    private static final Component REPACK = Component.literal("Repack");
    // Cover-slot frame position, relative to the gui origin (item sits at +134,+22; frame is 1px out).
    private static final int COVER_SLOT_X = 133;
    private static final int COVER_SLOT_Y = 21;

    private int playingIndex;
    private int playingTrack;
    private Button albumButton;

    public AlbumJukeboxScreen(AlbumJukeboxMenu dispenserMenu, Inventory inventory, Component component) {
        super(dispenserMenu, inventory, component);
    }

    private void update(boolean next) {
        ClientLevel level = this.minecraft.level;
        if (level == null || !this.menu.isInitialized()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(this.menu.getPos());
        if (!(blockEntity instanceof AlbumJukeboxBlockEntity albumJukebox) || !((AlbumJukeboxBlockEntity) blockEntity).isPlaying()) {
            return;
        }

        int oldIndex = albumJukebox.getPlayingIndex();
        int oldTrack = albumJukebox.getTrack();
        if (next) {
            albumJukebox.next();
        } else {
            albumJukebox.previous();
        }

        if (((albumJukebox.getPlayingIndex() == oldIndex && albumJukebox.getTrack() != oldTrack) || albumJukebox.recalculatePlayingIndex(!next)) && albumJukebox.getPlayingIndex() != -1) {
            SoundTracker.playAlbum(albumJukebox, albumJukebox.getBlockState(), level, this.menu.getPos(), true);
            var packet = new SetAlbumJukeboxTrackPacket(albumJukebox.getPlayingIndex(), albumJukebox.getTrack());
            packet.sendToServer();
            //EtchedMessages.PLAY.sendToServer(new SetAlbumJukeboxTrackPacket(albumJukebox.getPlayingIndex(), albumJukebox.getTrack()));
        }
    }

    @Override
    protected void init() {
        super.init();

        // Track controls (previous / next) grouped on the left as compact icon buttons.
        this.addRenderableWidget(Button.builder(Component.literal("|◀"), b -> this.update(false))
                .bounds(this.leftPos + 9, this.topPos + 34, 21, 20)
                .tooltip(Tooltip.create(Component.literal("Previous track"))).build());
        this.addRenderableWidget(Button.builder(Component.literal("▶|"), b -> this.update(true))
                .bounds(this.leftPos + 31, this.topPos + 34, 21, 20)
                .tooltip(Tooltip.create(Component.literal("Next track"))).build());

        // Album load/unload grouped on the right, centered under the cover slot.
        this.albumButton = this.addRenderableWidget(Button.builder(UNLOAD, b ->
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, AlbumJukeboxMenu.BUTTON_TOGGLE_ALBUM))
                .bounds(this.leftPos + 122, this.topPos + 46, 46, 18).build());
        this.updateAlbumButton();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.updateAlbumButton();
    }

    private void updateAlbumButton() {
        if (this.albumButton == null) {
            return;
        }
        ItemStack cover = this.menu.getCoverSlotItem();
        if (!cover.is(EtchedItems.ALBUM_COVER.asItem())) {
            this.albumButton.setMessage(UNLOAD);
            this.albumButton.active = false;
        } else if (!AlbumCoverItem.getRecords(cover).isEmpty()) {
            this.albumButton.setMessage(UNLOAD);
            this.albumButton.active = this.hasRecordSlot(true);
        } else {
            this.albumButton.setMessage(REPACK);
            this.albumButton.active = this.hasRecordSlot(false);
        }
    }

    // wantEmpty=true: at least one empty jukebox slot to unload into. false: at least one disc to repack.
    private boolean hasRecordSlot(boolean wantEmpty) {
        for (int i = 0; i < 9; i++) {
            if (this.menu.slots.get(i).getItem().isEmpty() == wantEmpty) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //? if >=1.21 {
        /*this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        *///?} else {
        this.renderBackground(guiGraphics);
        //?}
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CONTAINER_LOCATION, guiLeft, guiTop, 0, 0, this.imageWidth, this.imageHeight);
        // Draw a slot frame for the album-cover slot by reusing a grid slot from the dispenser texture.
        guiGraphics.blit(CONTAINER_LOCATION, guiLeft + COVER_SLOT_X, guiTop + COVER_SLOT_Y, 61, 16, 18, 18);

        this.playingIndex = -1;
        this.playingTrack = 0;
        ClientLevel level = this.minecraft.level;
        if (level == null || !this.menu.isInitialized()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(this.menu.getPos());
        if (!(blockEntity instanceof AlbumJukeboxBlockEntity)) {
            return;
        }

        this.playingIndex = ((AlbumJukeboxBlockEntity) blockEntity).getPlayingIndex();
        this.playingTrack = ((AlbumJukeboxBlockEntity) blockEntity).getTrack();
        if (this.playingIndex != -1) {
            int x = this.playingIndex % 3;
            int y = this.playingIndex / 3;
            guiGraphics.fillGradient(guiLeft + 62 + x * 18, guiTop + 17 + y * 18, guiLeft + 78 + x * 18, guiTop + 33 + y * 18, 0x3CF6FF00, 0x3CF6FF00);
        }
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> tooltip = super.getTooltipFromContainerItem(stack);

        if (this.hoveredSlot != null) {
            if (this.hoveredSlot.index == this.playingIndex) {
                if (this.playingTrack >= 0 && PlayableRecord.getStackTrackCount(stack) > 0) {
                    Optional<TrackData[]> optional = PlayableRecord.getStackMusic(stack).filter(tracks -> this.playingTrack < tracks.length);
                    if (optional.isPresent()) {
                        TrackData track = optional.get()[this.playingTrack];
                        tooltip.add(NOW_PLAYING.copy().append(": ").append(track.getDisplayName()).append(" (" + (this.playingTrack + 1) + "/" + optional.get().length + ")"));
                    } else {
                        tooltip.add(NOW_PLAYING);
                    }
                } else {
                    tooltip.add(NOW_PLAYING);
                }
            }
        }

        return tooltip;
    }
}
