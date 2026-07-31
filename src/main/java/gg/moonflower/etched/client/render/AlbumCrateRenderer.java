package gg.moonflower.etched.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import gg.moonflower.etched.common.block.AlbumCrateBlock;
import gg.moonflower.etched.common.block.AlbumDisplayBlock;
import gg.moonflower.etched.common.blockentity.AlbumCrateBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class AlbumCrateRenderer implements BlockEntityRenderer<AlbumCrateBlockEntity> {

    private static final float TILT = 12.0F;
    private static final float SCALE = 0.72F;
    private static final float BACK_Z = 0.82F;
    private static final float STEP_Z = 0.0594F;
    private static final float BACK_Y = 0.625F;
    private static final float STEP_Y = 0.0114F;
    private static final float[] DRIFT = {0.0F, 1.0F, -1.0F, 2.0F, 0.0F, -2.0F,
                                          1.0F, -1.0F, 0.0F, 2.0F, -1.0F, 1.0F};

    public AlbumCrateRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlbumCrateBlockEntity crate, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        float yRot = AlbumDisplayBlock.degreesFor(crate.getBlockState().getValue(AlbumCrateBlock.ROTATION));
        int seed = (int) crate.getBlockPos().asLong();

        for (int i = 0; i < crate.getContainerSize(); i++) {
            ItemStack stack = crate.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            pose.pushPose();
            pose.translate(0.5F, BACK_Y - i * STEP_Y, 0.5F);
            pose.mulPose(Axis.YP.rotationDegrees(-yRot));
            pose.translate(DRIFT[i % DRIFT.length] / 16.0F, 0.0F, BACK_Z - i * STEP_Z - 0.5F);
            pose.mulPose(Axis.XP.rotationDegrees(TILT));
            pose.scale(SCALE, SCALE, SCALE);
            pose.translate(0.0F, 0.5F, 0.0F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay,
                    pose, buffers, crate.getLevel(), seed + i);
            pose.popPose();
        }
    }
}
