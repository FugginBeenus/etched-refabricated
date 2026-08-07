package gg.moonflower.etched.core.registry;

import gg.moonflower.etched.common.entity.MinecartJukebox;
import gg.moonflower.etched.core.Etched;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.function.Supplier;

public class EtchedEntities {
    public static final EntityType<MinecartJukebox> JUKEBOX_MINECART = FabricEntityTypeBuilder.<MinecartJukebox>create( MobCategory.MISC,MinecartJukebox::new).dimensions(EntityDimensions.fixed(0.98F, 0.7F)).trackRangeChunks(8).build();


    public static <R extends Entity> EntityType<R> register(String name, Supplier<EntityType<R>>value) {
        var id = gg.moonflower.etched.api.util.EtchedResourceLocation.of(Etched.MOD_ID, name);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id, value.get());
    }
    public ResourceKey<PoiType> BARD_POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, gg.moonflower.etched.api.util.EtchedResourceLocation.of(Etched.MOD_ID+":bard"));

    public static void register() {
        Registry.register(BuiltInRegistries.ENTITY_TYPE,gg.moonflower.etched.api.util.EtchedResourceLocation.of(Etched.MOD_ID,"jukebox_minecart"),JUKEBOX_MINECART);
    }
}
