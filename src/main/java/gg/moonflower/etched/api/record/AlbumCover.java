package gg.moonflower.etched.api.record;

import com.mojang.blaze3d.platform.NativeImage;
import gg.moonflower.etched.client.render.item.ImageAlbumCover;
import gg.moonflower.etched.client.render.item.ModelAlbumCover;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public interface AlbumCover {

    AlbumCover EMPTY = new AlbumCover() {
    };

    static AlbumCover of(NativeImage image) {
        return new ImageAlbumCover(image);
    }

    static AlbumCover of(ModelResourceLocation location) {
        return new ModelAlbumCover(location);
    }

    static AlbumCover of(ResourceLocation location) {
        return new ModelAlbumCover(modelLocation(location));
    }

    static ModelResourceLocation modelLocation(ResourceLocation location) {
        //? if >=1.21 {
        /*return new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(location.getNamespace(), "item/" + location.getPath()), "fabric_resource");
        *///?} else {
        return new ModelResourceLocation(location, "inventory");
        //?}
    }
}
