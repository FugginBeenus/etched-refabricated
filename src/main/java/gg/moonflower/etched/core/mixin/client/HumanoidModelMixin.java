package gg.moonflower.etched.core.mixin.client;

import gg.moonflower.etched.common.item.BoomboxItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {

    @Final
    @Shadow
    public ModelPart leftArm;

    @Final
    @Shadow
    public ModelPart rightArm;

    // Raise angle of the holding arm, and how far it swings out from the body. These are upstream's
    // values: boombox_playing.json's thirdperson transform was authored against this exact arm pose,
    // so the two are a matched pair and should be retuned together, not separately.
    private static final float ETCHED$HOLD_X_ROT = (float) Math.PI;
    private static final float ETCHED$HOLD_Z_ROT = 0.610865F;

    // Hold the boombox arm still. setupAnim poses the arms early (poseRightArm/poseLeftArm) but then
    // runs AnimationUtils.bobModelPart on both arms near the end, which layers an idle sway back on top.
    // The tail of setupAnim, after that bob, is the only place the fixed pose sticks. One arm only: the
    // free arm keeps its normal walk and idle motion.
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    public void etched$holdBoomboxArm(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        InteractionHand playingHand = BoomboxItem.getPlayingHand(entity);
        if (playingHand == null) {
            return;
        }

        boolean mainRight = entity.getMainArm() == HumanoidArm.RIGHT;
        boolean rightHolds = (mainRight && playingHand == InteractionHand.MAIN_HAND) ||
                (!mainRight && playingHand == InteractionHand.OFF_HAND);

        ModelPart arm = rightHolds ? this.rightArm : this.leftArm;
        arm.xRot = ETCHED$HOLD_X_ROT;
        arm.yRot = 0.0F;
        arm.zRot = rightHolds ? -ETCHED$HOLD_Z_ROT : ETCHED$HOLD_Z_ROT;
    }
}
