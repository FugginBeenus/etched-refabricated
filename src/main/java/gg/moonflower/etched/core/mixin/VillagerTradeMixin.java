package gg.moonflower.etched.core.mixin;

import gg.moonflower.etched.core.registry.EtchedAdvancements;
import gg.moonflower.etched.core.registry.EtchedVillagers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Finding a bard is what earns "Toss a coin". Opening their shop is the moment that means it: it is the
 * first thing a player does on meeting one, and it does not ask them to buy anything first.
 */
@Mixin(Villager.class)
public class VillagerTradeMixin {

    @Inject(method = "startTrading", at = @At("HEAD"))
    private void etched$bardMet(Player trader, CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        if (villager.getVillagerData().getProfession() == EtchedVillagers.BARD_PROFESSION
                && trader instanceof ServerPlayer player) {
            EtchedAdvancements.award(player, EtchedAdvancements.TOSS_A_COIN);
        }
    }
}
