package gg.moonflower.etched.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import gg.moonflower.etched.common.block.AlbumDisplayBlock;
import gg.moonflower.etched.common.blockentity.AlbumDisplayBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the album a display stand is holding, stood up and tilted back so the artwork faces the room.
 * Album covers route through {@code AlbumCoverItemRenderer} on their own, since this goes through the
 * normal item renderer.
 */
@Environment(EnvType.CLIENT)
public class AlbumDisplayRenderer implements BlockEntityRenderer<AlbumDisplayBlockEntity> {

    private static final float TILT = 30.0F;
    private static final float SCALE = 0.72F;
    /** Where the album's bottom edge sits, just behind the stand's front lip. */
    private static final float REST_Z = 0.28F;
    private static final float REST_Y = 0.0625F;

    public AlbumDisplayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlbumDisplayBlockEntity display, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = display.getItem();
        if (stack.isEmpty()) {
            return;
        }

        // Matches how the blockstate turns the stand, so artwork faces the way the stand does.
        float yRot = AlbumDisplayBlock.degreesFor(display.getBlockState().getValue(AlbumDisplayBlock.ROTATION));

        pose.pushPose();
        // Stand the album in the groove behind the front lip and lean it back onto the ridge. Lifting
        // after the scale puts the pivot on its bottom edge, so it rocks back instead of swinging
        // through the base.
        pose.translate(0.5F, REST_Y, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(-yRot));
        pose.translate(0.0F, 0.0F, REST_Z - 0.5F);
        pose.mulPose(Axis.XP.rotationDegrees(TILT));
        pose.scale(SCALE, SCALE, SCALE);
        pose.translate(0.0F, 0.5F, 0.0F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay,
                pose, buffers, display.getLevel(), (int) display.getBlockPos().asLong());
        pose.popPose();
    }
}
