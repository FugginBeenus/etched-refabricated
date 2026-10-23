package gg.moonflower.etched.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import gg.moonflower.etched.api.util.EtchedResourceLocation;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.registry.EtchedVillagers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import org.jetbrains.annotations.Nullable;

/**
 * The bard's gear in 3D, over the villager or zombie villager: a real feather tucked into the hat band
 * and a lute slung across the back.
 */
@Environment(EnvType.CLIENT)
public class BardGearLayer<T extends LivingEntity & VillagerDataHolder, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {

    private static final ResourceLocation LUTE_TEXTURE = EtchedResourceLocation.of(Etched.MOD_ID, "textures/entity/bard_lute.png");
    // The game's own feather, drawn as one flat plane so it stays thin from every side.
    private static final ResourceLocation FEATHER_TEXTURE = EtchedResourceLocation.of("minecraft", "textures/item/feather.png");
    // Straight up in the head's space, seen from the feather's tilted frame.
    private static final float UP_X = (float) Math.sin(Math.toRadians(-15.0));
    private static final float UP_Y = (float) -Math.cos(Math.toRadians(-15.0));

    private final ModelPart lute = createLute().bakeRoot();

    public BardGearLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    // A pear-shaped bowl, a neck and a pegbox bent back, with the soundboard facing +z.
    private static LayerDefinition createLute() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bowl", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 4.0F, 2.0F)
                .texOffs(0, 6).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 2.0F, 2.0F)
                .texOffs(0, 10).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 1.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("pegbox", CubeListBuilder.create()
                .texOffs(20, 0).addBox(-1.0F, -3.0F, -0.5F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.5F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || entity.getVillagerData().getProfession() != EtchedVillagers.BARD_PROFESSION) {
            return;
        }
        M model = this.getParentModel();

        // Model space runs y down and the face looks toward -z. The feather's quill sits in the band on
        // the hat's left, above the brim, with its tip trailing back.
        pose.pushPose();
        model.getHead().translateAndRotate(pose);
        pose.translate(4.8F / 16.0F, -7.5F / 16.0F, -1.5F / 16.0F);
        pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(15.0F));
        pose.scale(0.5F, 0.5F, 0.5F);
        pose.translate(0.45F, -0.45F, 0.0F);
        VertexConsumer feather = buffers.getBuffer(RenderType.entityCutoutNoCull(FEATHER_TEXTURE));
        PoseStack.Pose last = pose.last();
        // Local y runs down, so the texture's top goes at -y. The plane is lit as if it faced up, the
        // same from either side, so it doesn't go dark when seen from behind.
        vertex(feather, last, -0.5F, -0.5F, 0.0F, 0.0F, light);
        vertex(feather, last, -0.5F, 0.5F, 0.0F, 1.0F, light);
        vertex(feather, last, 0.5F, 0.5F, 1.0F, 1.0F, light);
        vertex(feather, last, 0.5F, -0.5F, 1.0F, 0.0F, light);
        pose.popPose();

        ModelPart body = bodyOf(model);
        if (body != null) {
            pose.pushPose();
            body.translateAndRotate(pose);
            pose.translate(0.0F, 6.0F / 16.0F, 4.1F / 16.0F);
            pose.mulPose(Axis.ZP.rotationDegrees(35.0F));
            this.lute.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(LUTE_TEXTURE)), light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v, int light) {
        //? if >=1.21 {
        /*consumer.addVertex(pose.pose(), x, y, 0.0F).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, UP_X, UP_Y, 0.0F);
        *///?} else {
        consumer.vertex(pose.pose(), x, y, 0.0F).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), UP_X, UP_Y, 0.0F).endVertex();
        //?}
    }

    @Nullable
    private static ModelPart bodyOf(EntityModel<?> model) {
        if (model instanceof HumanoidModel<?> humanoid) {
            return humanoid.body;
        }
        if (model instanceof VillagerModel<?> villager) {
            return villager.root().getChild("body");
        }
        return null;
    }
}
