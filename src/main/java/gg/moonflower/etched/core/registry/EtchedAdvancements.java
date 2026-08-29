package gg.moonflower.etched.core.registry;

import gg.moonflower.etched.core.Etched;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Grants the mod's advancements from code.
 *
 * <p>Each one is defined with an impossible trigger and awarded here instead. Linking a speaker or
 * putting a station on air are things only this mod knows about, and writing a criterion for each would
 * mean a different one per Minecraft version for no gain.
 */
public final class EtchedAdvancements {

    public static final String RECORD_SCRATCH = "record_scratch";
    public static final String AUDIOPHILE = "audiophile";
    public static final String GOES_UP_TO_11 = "goes_up_to_11";
    public static final String TOSS_A_COIN = "toss_a_coin";
    public static final String BROADCAST_RADIO = "broadcast_radio";

    /** The single criterion every one of these advancements is awarded through. */
    private static final String CRITERION = "earned";

    private EtchedAdvancements() {
    }

    public static void award(ServerPlayer player, String name) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        ResourceLocation id = gg.moonflower.etched.api.util.EtchedResourceLocation.of(Etched.MOD_ID, name);
        //? if >=1.21 {
        /*net.minecraft.advancements.AdvancementHolder advancement = server.getAdvancements().get(id);
        *///?} else {
        net.minecraft.advancements.Advancement advancement = server.getAdvancements().getAdvancement(id);
        //?}
        if (advancement != null) {
            player.getAdvancements().award(advancement, CRITERION);
        }
    }
}
