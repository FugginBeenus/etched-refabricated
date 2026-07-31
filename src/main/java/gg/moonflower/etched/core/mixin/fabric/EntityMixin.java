package gg.moonflower.etched.core.mixin.fabric;

import gg.moonflower.etched.core.hook.EntityHook;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    // A radio carried into the Nether comes out the other side as a portal radio. changeDimension takes
    // a ServerLevel before 1.21 and a DimensionTransition after, so the two branches differ only in how
    // they get hold of the destination.
    //? if >=1.21 {
    /*@SuppressWarnings("ConstantConditions")
    @Inject(method = "changeDimension", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"))
    public void createPortalRadio(net.minecraft.world.level.portal.DimensionTransition transition, CallbackInfoReturnable<Entity> cir) {
        if ((Object) this instanceof ItemEntity item) {
            EntityHook.warpRadio(transition.newLevel(), item);
        }
    }
    *///?} else {
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "changeDimension", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"))
    public void createPortalRadio(ServerLevel server, CallbackInfoReturnable<Entity> cir) {
        if ((Object) this instanceof ItemEntity) {
            EntityHook.warpRadio(server, (ItemEntity) (Object) this);
        }
    }
    //?}
}
