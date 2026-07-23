package gg.moonflower.etched.core;

import gg.moonflower.etched.client.screen.*;
import gg.moonflower.etched.common.entity.MinecartJukebox;
import gg.moonflower.etched.common.menu.AlbumCoverMenu;
import gg.moonflower.etched.common.menu.AlbumJukeboxMenu;
import gg.moonflower.etched.core.registry.EtchedMenus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

import gg.moonflower.etched.client.render.EtchedModelLayers;
import gg.moonflower.etched.client.render.JukeboxMinecartRenderer;
import gg.moonflower.etched.client.render.item.AlbumCoverItemRenderer;
import gg.moonflower.etched.common.item.BlankMusicDiscItem;
import gg.moonflower.etched.common.item.ComplexMusicLabelItem;
import gg.moonflower.etched.common.item.EtchedMusicDiscItem;
import gg.moonflower.etched.common.item.MusicLabelItem;
import gg.moonflower.etched.core.registry.EtchedBlocks;
import gg.moonflower.etched.core.registry.EtchedEntities;
import gg.moonflower.etched.core.registry.EtchedItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;


@Environment(EnvType.CLIENT)
public class EtchedClient {
    public static final ModelResourceLocation BOOMBOX_IN_HAND_MODEL = new ModelResourceLocation(gg.moonflower.etched.api.util.EtchedResourceLocation.of(Etched.MOD_ID, "boombox_in_hand"), "inventory");

    public static void registerItemGroups() {
        MenuScreens.register(EtchedMenus.ETCHING_MENU, EtchingScreen::new);
        MenuScreens.register(EtchedMenus.BOOMBOX_MENU, BoomboxScreen::new);
        MenuScreens.register(EtchedMenus.RADIO_MENU, RadioScreen::new);
        MenuScreens.register(EtchedMenus.ALBUM_JUKEBOX_MENU, AlbumJukeboxScreen::new);
        MenuScreens.register(EtchedMenus.ALBUM_COVER_MENU, AlbumCoverScreen::new);
        EntityRendererRegistry.register(EtchedEntities.JUKEBOX_MINECART,context -> {
            return new JukeboxMinecartRenderer<>(context);
        });
        EntityModelLayerRegistry.registerModelLayer(EtchedModelLayers.JUKEBOX_MINECART,MinecartModel::createBodyLayer);
        //TODO: Move to registrat
        /*ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(event -> {
    	    event.accept(EtchedBlocks.ETCHING_TABLE.asStack(1));
            event.accept(EtchedBlocks.ALBUM_JUKEBOX.asStack(1));
            event.accept(EtchedBlocks.RADIO.asStack(1));
        });*/
    }

    //TODO: FIX`
    /*
    @SubscribeEvent
    public static void registerCustomModels(ModelEvent.RegisterAdditional event) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        String folder = "models/item/" + AlbumCoverItemRenderer.FOLDER_NAME;
        event.register(new ModelResourceLocation(gg.moonflower.etched.api.util.EtchedResourceLocation.of(Etched.MOD_ID, "boombox_in_hand"), "inventory"));
        for (ResourceLocation location : resourceManager.listResources(folder, name -> name.getPath().endsWith(".json")).keySet()) {
            event.register(new ModelResourceLocation(gg.moonflower.etched.api.util.EtchedResourceLocation.of(location.getNamespace(), location.getPath().substring(12, location.getPath().length() - 5)), "inventory"));
        }
    }

    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EtchedEntities.JUKEBOX_MINECART.get(), JukeboxMinecartRenderer::new);
    }

    @SubscribeEvent
    public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EtchedModelLayers.JUKEBOX_MINECART, MinecartModel::createBodyLayer);
    }
    */

    public static void registerItemColors() {
        ColorProviderRegistry.ITEM.register((stack, index) -> index == 0 || index == 1 ? opaque(MusicLabelItem.getLabelColor(stack)) : -1, EtchedItems.MUSIC_LABEL.get());
        ColorProviderRegistry.ITEM.register((stack, index) -> index == 0 ? opaque(ComplexMusicLabelItem.getPrimaryColor(stack)) : index == 1 ? opaque(ComplexMusicLabelItem.getSecondaryColor(stack)) : -1, EtchedItems.COMPLEX_MUSIC_LABEL.get());

        ColorProviderRegistry.ITEM.register((stack, index) -> index > 0 ? -1 : opaque(((BlankMusicDiscItem) stack.getItem()).getColor(stack)), EtchedItems.BLANK_MUSIC_DISC.get());
        ColorProviderRegistry.ITEM.register((stack, index) -> {
            if (index == 0) {
                return opaque(EtchedMusicDiscItem.getDiscColor(stack));
            }
            if (EtchedMusicDiscItem.getPattern(stack).isColorable()) {
                if (index == 1) {
                    return opaque(EtchedMusicDiscItem.getLabelPrimaryColor(stack));
                }
                if (index == 2) {
                    return opaque(EtchedMusicDiscItem.getLabelSecondaryColor(stack));
                }
            }
            return -1;
        }, EtchedItems.ETCHED_MUSIC_DISC.get());
    }

    // 1.21 respects the alpha byte of item tint colors; these providers return 0xRRGGBB (alpha 0),
    // which renders fully transparent. Force opaque. Harmless on 1.20.1 (tint alpha ignored there).
    private static int opaque(int color) {
        return 0xFF000000 | color;
    }
}
