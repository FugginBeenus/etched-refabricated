package gg.moonflower.etched.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.util.EtchedResourceLocation;
import gg.moonflower.etched.client.render.item.AlbumCoverItemRenderer;
import gg.moonflower.etched.common.block.AlbumJukeboxBlock;
import gg.moonflower.etched.common.blockentity.AlbumJukeboxBlockEntity;
import gg.moonflower.etched.common.item.AlbumCoverItem;
import gg.moonflower.etched.common.item.EtchedMusicDiscItem;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.mixin.LevelRendererAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Draws what the block model can't: the record on the turntable, spinning while it plays, and the
 * playing album's art behind the front window. Loose discs leave the window dark.
 */
@Environment(EnvType.CLIENT)
public class AlbumJukeboxRenderer implements BlockEntityRenderer<AlbumJukeboxBlockEntity> {

    private static final ResourceLocation SHEET = EtchedResourceLocation.of(Etched.MOD_ID, "block/album_jukebox");
    private static final float SHEET_WIDTH = 64.0F;
    private static final float SHEET_HEIGHT = 32.0F;

    // Pixel rectangles on the sheet: the grey record, half of its label, the window shine.
    private static final int[] RECORD = {3, 19, 13, 29};
    private static final int[] LABEL = {19, 19, 29, 29};
    private static final int[] GLASS = {51, 3, 61, 13};

    private static final float RECORD_RADIUS = 5.0F / 16.0F;
    private static final float RECORD_LIFT = 0.003F;
    private static final float DEGREES_PER_TICK = 5.0F;

    // The window glass spans pixels 3 to 13 of the front face. The art lies flat on the back of the
    // one-pixel recess; the shine sits at the front of it.
    private static final float WINDOW_MIN = 3.0F / 16.0F;
    private static final float WINDOW_MAX = 13.0F / 16.0F;
    private static final float ART_DEPTH = 1.0F / 16.0F - 0.004F;
    private static final float GLASS_DEPTH = -0.002F;

    private static final int VANILLA_DISC = 0x2C2C2C;
    private static final int PLAIN_DISC = 0x515151;
    private static final int PLAIN_LABEL = 0xFFFFFF;
    // Sampled from each vanilla disc's own item texture, so the record on the turntable matches it.
    private static final Map<String, Integer> VANILLA_LABELS = Map.ofEntries(
            Map.entry("minecraft:music_disc_13", 0xFFD800), Map.entry("minecraft:music_disc_cat", 0x4CFF00),
            Map.entry("minecraft:music_disc_blocks", 0xE2543B), Map.entry("minecraft:music_disc_chirp", 0x7F0002),
            Map.entry("minecraft:music_disc_far", 0xB6FF00), Map.entry("minecraft:music_disc_mall", 0x9A75FF),
            Map.entry("minecraft:music_disc_mellohi", 0xB200FF), Map.entry("minecraft:music_disc_stal", 0x2A2A2A),
            Map.entry("minecraft:music_disc_strad", 0xF2F2F2), Map.entry("minecraft:music_disc_ward", 0x006B57),
            Map.entry("minecraft:music_disc_11", 0x5E5E5E), Map.entry("minecraft:music_disc_wait", 0x81A9E2),
            Map.entry("minecraft:music_disc_otherside", 0x31A2F2), Map.entry("minecraft:music_disc_5", 0x034150),
            Map.entry("minecraft:music_disc_pigstep", 0x723232), Map.entry("minecraft:music_disc_relic", 0x754236),
            Map.entry("minecraft:music_disc_creator", 0x2D4D40), Map.entry("minecraft:music_disc_creator_music_box", 0xC26B4C),
            Map.entry("minecraft:music_disc_precipice", 0x4D3C32));

    // Where each record has turned to, so it stops where it is instead of snapping back.
    private final Map<AlbumJukeboxBlockEntity, float[]> spin = new WeakHashMap<>();

    public AlbumJukeboxRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlbumJukeboxBlockEntity jukebox, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Level level = jukebox.getLevel();
        if (level == null) {
            return;
        }

        int index = playingSlot(jukebox);
        ItemStack current = index < 0 ? ItemStack.EMPTY : jukebox.getItem(index);
        if (current.isEmpty()) {
            return;
        }

        Direction facing = jukebox.getBlockState().getValue(AlbumJukeboxBlock.FACING);
        TextureAtlasSprite sheet = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(SHEET);

        int track = index == jukebox.getPlayingIndex() ? jukebox.getTrack() : 0;
        ItemStack record = recordFor(current, track);
        if (!record.isEmpty()) {
            float angle = this.turn(jukebox, level.getGameTime() + partialTick, isSpinning(jukebox));
            int topLight = LevelRenderer.getLightColor(level, jukebox.getBlockPos().above());
            this.renderRecord(record, angle, pose, buffers, sheet, topLight);
        }

        // Only albums have art to show. A loose disc counts as part of the album it was unpacked from.
        ItemStack art = current.getItem() instanceof AlbumCoverItem ? current : jukebox.getStoredCover();
        if (!art.isEmpty()) {
            int frontLight = LevelRenderer.getLightColor(level, jukebox.getBlockPos().relative(facing));
            this.renderArt(art, facing, pose, buffers, sheet, frontLight);
        }
    }

    private void renderRecord(ItemStack record, float angle, PoseStack pose, MultiBufferSource buffers, TextureAtlasSprite sheet, int light) {
        int body;
        int topLabel;
        int bottomLabel;
        if (record.getItem() instanceof EtchedMusicDiscItem) {
            body = EtchedMusicDiscItem.getDiscColor(record);
            topLabel = EtchedMusicDiscItem.getLabelPrimaryColor(record);
            bottomLabel = EtchedMusicDiscItem.getLabelSecondaryColor(record);
        } else {
            Integer label = VANILLA_LABELS.get(BuiltInRegistries.ITEM.getKey(record.getItem()).toString());
            body = label != null ? VANILLA_DISC : PLAIN_DISC;
            topLabel = bottomLabel = label != null ? label : PLAIN_LABEL;
        }

        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        pose.pushPose();
        pose.translate(0.5F, 1.0F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        PoseStack.Pose last = pose.last();

        flat(consumer, last, sheet, RECORD, RECORD_LIFT, body, light);
        flat(consumer, last, sheet, LABEL, RECORD_LIFT + 0.001F, topLabel, light);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        flat(consumer, pose.last(), sheet, LABEL, RECORD_LIFT + 0.001F, bottomLabel, light);
        pose.popPose();
    }

    private void renderArt(ItemStack art, Direction facing, PoseStack pose, MultiBufferSource buffers, TextureAtlasSprite sheet, int light) {
        pose.pushPose();
        // The model's front is north; turn the same way the blockstate turns it.
        pose.translate(0.5F, 0.0F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(-(facing.toYRot() + 180.0F)));
        pose.translate(-0.5F, 0.0F, -0.5F);
        PoseStack.Pose last = pose.last();

        AlbumCoverItemRenderer.FlatArt flat = AlbumCoverItemRenderer.INSTANCE.getFlatArt(art);
        window(buffers.getBuffer(RenderType.entityCutoutNoCull(flat.texture())), last, ART_DEPTH, flat.u0(), flat.v0(), flat.u1(), flat.v1(), light);

        // The same shine streaks the empty glass has, laid over the art so it still reads as glass.
        window(buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)), last, GLASS_DEPTH,
                u(sheet, GLASS[0]), v(sheet, GLASS[1]), u(sheet, GLASS[2]), v(sheet, GLASS[3]), light);
        pose.popPose();
    }

    // A square over the window glass, seen from the front, so u0 sits on the viewer's left.
    private static void window(VertexConsumer consumer, PoseStack.Pose pose, float z, float u0, float v0, float u1, float v1, int light) {
        vertex(consumer, pose, WINDOW_MAX, WINDOW_MAX, z, 0xFFFFFF, 255, u0, v0, light, 0, 0, -1);
        vertex(consumer, pose, WINDOW_MIN, WINDOW_MAX, z, 0xFFFFFF, 255, u1, v0, light, 0, 0, -1);
        vertex(consumer, pose, WINDOW_MIN, WINDOW_MIN, z, 0xFFFFFF, 255, u1, v1, light, 0, 0, -1);
        vertex(consumer, pose, WINDOW_MAX, WINDOW_MIN, z, 0xFFFFFF, 255, u0, v1, light, 0, 0, -1);
    }

    private float turn(AlbumJukeboxBlockEntity jukebox, float now, boolean spinning) {
        float[] state = this.spin.computeIfAbsent(jukebox, key -> new float[]{0.0F, now});
        float elapsed = Mth.clamp(now - state[1], 0.0F, 20.0F);
        if (spinning) {
            state[0] = (state[0] + elapsed * DEGREES_PER_TICK) % 360.0F;
        }
        state[1] = now;
        return state[0];
    }

    private static int playingSlot(AlbumJukeboxBlockEntity jukebox) {
        int index = jukebox.getPlayingIndex();
        if (index >= 0 && index < jukebox.getContainerSize() && !jukebox.getItem(index).isEmpty()) {
            return index;
        }
        for (int i = 0; i < jukebox.getContainerSize(); i++) {
            if (!jukebox.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    // An album's track count runs across all its records, so walk them to find the one playing.
    private static ItemStack recordFor(ItemStack stack, int track) {
        if (!(stack.getItem() instanceof AlbumCoverItem)) {
            return stack;
        }
        for (ItemStack record : AlbumCoverItem.getRecords(stack)) {
            int tracks = PlayableRecord.getStackTrackCount(record);
            if (track < tracks) {
                return record;
            }
            track -= tracks;
        }
        return ItemStack.EMPTY;
    }

    private static boolean isSpinning(AlbumJukeboxBlockEntity jukebox) {
        if (jukebox.getPlayingIndex() < 0) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        SoundInstance sound = ((LevelRendererAccessor) minecraft.levelRenderer).getPlayingRecords().get(jukebox.getBlockPos());
        return sound != null && minecraft.getSoundManager().isActive(sound);
    }

    private static void flat(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sheet, int[] rect, float y, int colour, int light) {
        float r = RECORD_RADIUS;
        float u0 = u(sheet, rect[0]), u1 = u(sheet, rect[2]);
        float v0 = v(sheet, rect[1]), v1 = v(sheet, rect[3]);
        vertex(consumer, pose, -r, y, -r, colour, 255, u0, v0, light, 0, 1, 0);
        vertex(consumer, pose, -r, y, r, colour, 255, u0, v1, light, 0, 1, 0);
        vertex(consumer, pose, r, y, r, colour, 255, u1, v1, light, 0, 1, 0);
        vertex(consumer, pose, r, y, -r, colour, 255, u1, v0, light, 0, 1, 0);
    }

    private static float u(TextureAtlasSprite sprite, float pixel) {
        return sprite.getU0() + (sprite.getU1() - sprite.getU0()) * pixel / SHEET_WIDTH;
    }

    private static float v(TextureAtlasSprite sprite, float pixel) {
        return sprite.getV0() + (sprite.getV1() - sprite.getV0()) * pixel / SHEET_HEIGHT;
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int rgb, int alpha, float u, float v, int light, float nx, float ny, float nz) {
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        //? if >=1.21 {
        /*consumer.addVertex(pose.pose(), x, y, z).setColor(r, g, b, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        *///?} else {
        consumer.vertex(pose.pose(), x, y, z).color(r, g, b, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), nx, ny, nz).endVertex();
        //?}
    }
}
