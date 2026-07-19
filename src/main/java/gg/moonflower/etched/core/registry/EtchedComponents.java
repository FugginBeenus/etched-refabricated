package gg.moonflower.etched.core.registry;

// Typed data-component registry. 1.21+ only (data components did not exist before 1.20.5); on 1.20.1
// the mod stores the same data in item NBT. EtchedComponents.register() must be called from the mod
// initializer on 1.21+.
//? if >=1.21 {
/*import gg.moonflower.etched.common.component.AlbumCoverComponent;
import gg.moonflower.etched.core.Etched;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class EtchedComponents {

    private EtchedComponents() {
    }

    public static final DataComponentType<AlbumCoverComponent> ALBUM_COVER = register("album_cover",
            DataComponentType.<AlbumCoverComponent>builder()
                    .persistent(AlbumCoverComponent.CODEC)
                    .networkSynchronized(AlbumCoverComponent.STREAM_CODEC)
                    .build());

    private static <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath(Etched.MOD_ID, name), type);
    }

    public static void register() {
    }
}
*///?}
