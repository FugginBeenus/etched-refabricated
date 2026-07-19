package gg.moonflower.etched.api.util;

import net.minecraft.resources.ResourceLocation;

/**
 * Version-compat factory for {@link ResourceLocation}. The public constructors were made private in
 * 1.21 in favour of static factory methods, so all mod code routes through here.
 */
public final class EtchedResourceLocation {

    private EtchedResourceLocation() {
    }

    public static ResourceLocation of(String namespace, String path) {
        //? if >=1.21 {
        /*return ResourceLocation.fromNamespaceAndPath(namespace, path);
        *///?} else {
        return new ResourceLocation(namespace, path);
        //?}
    }

    public static ResourceLocation of(String location) {
        //? if >=1.21 {
        /*return ResourceLocation.parse(location);
        *///?} else {
        return new ResourceLocation(location);
        //?}
    }
}
