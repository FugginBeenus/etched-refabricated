package gg.moonflower.etched.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import gg.moonflower.etched.api.util.EtchedResourceLocation;
import gg.moonflower.etched.common.block.StereoBlock;
import gg.moonflower.etched.common.blockentity.StereoBlockEntity;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.mixin.LevelRendererAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * Lights the stereo's screen while the player under it has music going: the little waveform the
 * block model draws gets covered by a glowing copy whose peaks rise and fall.
 */
@Environment(EnvType.CLIENT)
public class StereoRenderer implements BlockEntityRenderer<StereoBlockEntity> {

    private static final ResourceLocation SCREEN = EtchedResourceLocation.of(Etched.MOD_ID, "block/stereo_screen");

    // The screen on the model's front, in block units. The waveform is 15 by 3 pixels of a 16 pixel frame.
    private static final float LEFT = 11.774F / 16.0F;
    private static final float RIGHT = 4.226F / 16.0F;
    private static final float TOP = 3.6F / 16.0F;
    private static final float BOTTOM = 2.1F / 16.0F;
    private static final float DEPTH = 0.2F / 16.0F - 0.002F;

    public StereoRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(StereoBlockEntity stereo, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!isPlaying(stereo)) {
            return;
        }

        Direction facing = stereo.getBlockState().getValue(StereoBlock.FACING);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(SCREEN);
        float u0 = sprite.getU0(), v0 = sprite.getV0();
        float u1 = u0 + (sprite.getU1() - u0) * 15.0F / 16.0F;
        float v1 = v0 + (sprite.getV1() - v0) * 3.0F / 16.0F;

        pose.pushPose();
        pose.translate(0.5F, 0.0F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(-(facing.toYRot() + 180.0F)));
        pose.translate(-0.5F, 0.0F, -0.5F);
        PoseStack.Pose last = pose.last();
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        vertex(consumer, last, LEFT, TOP, u0, v0);
        vertex(consumer, last, RIGHT, TOP, u1, v0);
        vertex(consumer, last, RIGHT, BOTTOM, u1, v1);
        vertex(consumer, last, LEFT, BOTTOM, u0, v1);
        pose.popPose();
    }

    // Playing means the record under the stereo is actually sounding here, the same test the album
    // jukebox uses to spin its record.
    private static boolean isPlaying(StereoBlockEntity stereo) {
        Minecraft minecraft = Minecraft.getInstance();
        SoundInstance sound = ((LevelRendererAccessor) minecraft.levelRenderer).getPlayingRecords().get(stereo.getSourcePos());
        return sound != null && minecraft.getSoundManager().isActive(sound);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v) {
        //? if >=1.21 {
        /*consumer.addVertex(pose.pose(), x, y, DEPTH).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0, 0, -1);
        *///?} else {
        consumer.vertex(pose.pose(), x, y, DEPTH).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(pose.normal(), 0, 0, -1).endVertex();
        //?}
    }
}
