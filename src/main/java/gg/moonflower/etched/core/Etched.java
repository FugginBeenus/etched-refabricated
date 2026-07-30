package gg.moonflower.etched.core;

import com.tterrag.registrate.Registrate;
import gg.moonflower.etched.api.sound.download.SoundSourceManager;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.sound.download.BandcampSource;
import gg.moonflower.etched.common.sound.download.SoundCloudSource;
import gg.moonflower.etched.core.fabric.EtchedConfig;
import gg.moonflower.etched.core.registry.*;
import net.fabricmc.fabric.api.object.builder.v1.villager.VillagerProfessionBuilder;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Etched {

    public static final String MOD_ID = "etched";
    public static final Registrate REGISTRATE = Registrate.create(MOD_ID);
    public static final Logger LOGGER = LogManager.getLogger("Etched/General");


    public Etched() {

    }

    public static java.util.concurrent.Executor downloadExecutor() {
        //? if >=1.21 {
        /*return net.minecraft.Util.nonCriticalIoPool();
        *///?} else {
        return net.minecraft.util.HttpUtil.DOWNLOAD_EXECUTOR;
        //?}
    }

    public static void init() {
        //i guess following is needed to preload classes before registration or they will
        // not be registered
        EtchedTags.register();
        EtchedMessages.init();
        EtchedBlocks.register();
        EtchedEntities.register();
        EtchedItems.register();
        //? if >=1.21 {
        /*gg.moonflower.etched.core.registry.EtchedComponents.register();
        *///?}
        EtchedMenus.register();
        EtchedRecipes.register();
        EtchedSounds.register();

        REGISTRATE.register();
        EtchedConfig.HANDLER.load();
        // After the config loads: the bard reads it to decide whether to add its houses to villages.
        EtchedVillagers.registers();
        SoundSourceManager.registerSource(new SoundCloudSource());
        SoundSourceManager.registerSource(new BandcampSource());
    }

}

