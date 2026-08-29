package gg.moonflower.etched.core.mixin;

import gg.moonflower.etched.core.registry.EtchedAdvancements;
import gg.moonflower.etched.core.registry.EtchedVillagers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Paying a bard is what earns "Toss a coin", so the trade itself is the moment to catch. */
@Mixin(Villager.class)
public class VillagerTradeMixin {

    @Inject(method = "rewardTradeXp", at = @At("HEAD"))
    private void etched$bardTraded(MerchantOffer offer, CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        if (villager.getVillagerData().getProfession() == EtchedVillagers.BARD_PROFESSION
                && villager.getTradingPlayer() instanceof ServerPlayer player) {
            EtchedAdvancements.award(player, EtchedAdvancements.TOSS_A_COIN);
        }
    }
}
