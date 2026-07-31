package gg.moonflower.etched.core.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import gg.moonflower.etched.common.block.AlbumJukeboxBlock;
import gg.moonflower.etched.common.block.EtchingTableBlock;
import gg.moonflower.etched.common.block.RadioBlock;
import gg.moonflower.etched.common.blockentity.AlbumJukeboxBlockEntity;
import gg.moonflower.etched.common.blockentity.RadioBlockEntity;
import gg.moonflower.etched.common.item.PortalRadioItem;
import gg.moonflower.etched.core.Etched;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.codec.language.bm.Lang;

public class EtchedBlocks {

    //public static final ResourceKey<Registry<Block>> BLOCK = ResourceKey.createRegistryKey(gg.moonflower.etched.api.util.EtchedResourceLocation.of("etched_blocks"));
    //public static final ResourceKey<Registry<BlockEntity>> BLOCK_ENTITIES = ResourceKey.createRegistryKey(gg.moonflower.etched.api.util.EtchedResourceLocation.of("etched_block_entities"));

    public static final BlockEntry<EtchingTableBlock> ETCHING_TABLE =
            Etched.REGISTRATE.block("etching_table", EtchingTableBlock::new)
                    .properties(properties -> properties
                            .mapColor(MapColor.PODZOL)
                            .strength(2.5f)
                            .sound(SoundType.WOOD)
                    )
                    .item().tab(CreativeModeTabs.TOOLS_AND_UTILITIES).build()
                    .register();
    //registerWithItem("etching_table", () -> new EtchingTableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).strength(2.5F).sound(SoundType.WOOD)), new Item.Properties());
    public static final BlockEntry<AlbumJukeboxBlock> ALBUM_JUKEBOX =
            Etched.REGISTRATE.block("album_jukebox", AlbumJukeboxBlock::new)
                    .initialProperties(() -> Blocks.JUKEBOX)
                    .item().tab(CreativeModeTabs.TOOLS_AND_UTILITIES).build()
                    .register();
    //registerWithItem("album_jukebox", () -> new AlbumJukeboxBlock(BlockBehaviour.Properties.copy(Blocks.JUKEBOX)), new Item.Properties());
    public static final BlockEntry<RadioBlock> RADIO =
            Etched.REGISTRATE.block("radio", RadioBlock::new)
                    .initialProperties(() -> Blocks.JUKEBOX)
                    .properties(p->p.noOcclusion())
                    .item().tab(CreativeModeTabs.TOOLS_AND_UTILITIES).build()
                    .register();
    public static final BlockEntry<gg.moonflower.etched.common.block.AlbumPrinterBlock> ALBUM_PRINTER =
            Etched.REGISTRATE.block("album_printer", gg.moonflower.etched.common.block.AlbumPrinterBlock::new)
                    .properties(properties -> properties
                            .mapColor(MapColor.PODZOL)
                            .strength(2.5f)
                            .sound(SoundType.WOOD)
                            // Custom model isn't a solid full cube, so it mustn't occlude, or vanilla
                            // culls the touching faces of adjacent blocks and you see through them.
                            .noOcclusion()
                    )
                    .item().tab(CreativeModeTabs.TOOLS_AND_UTILITIES).build()
                    .register();
    public static final BlockEntry<gg.moonflower.etched.common.block.SpeakerBlock> SPEAKER =
            Etched.REGISTRATE.block("speaker", gg.moonflower.etched.common.block.SpeakerBlock::new)
                    .initialProperties(() -> Blocks.NOTE_BLOCK)
                    .properties(p -> p.noOcclusion())
                    .item().tab(CreativeModeTabs.TOOLS_AND_UTILITIES).build()
                    .register();
    public static final BlockEntry<gg.moonflower.etched.common.block.StereoBlock> STEREO =
            Etched.REGISTRATE.block("stereo", gg.moonflower.etched.common.block.StereoBlock::new)
                    .initialProperties(() -> Blocks.NOTE_BLOCK)
                    .properties(p -> p.noOcclusion())
                    .item().tab(CreativeModeTabs.TOOLS_AND_UTILITIES).build()
                    .register();
    public static final BlockEntry<gg.moonflower.etched.common.block.AlbumDisplayBlock> ALBUM_DISPLAY =
            Etched.REGISTRATE.block("album_display", gg.moonflower.etched.common.block.AlbumDisplayBlock::new)
                    .properties(properties -> properties
                            .mapColor(MapColor.PODZOL)
                            .strength(1.0f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                    )
                    .item().tab(CreativeModeTabs.FUNCTIONAL_BLOCKS).build()
                    .register();
    //registerWithItem("radio", () -> new RadioBlock(BlockBehaviour.Properties.copy(Blocks.JUKEBOX).noOcclusion()), new Item.Properties());
    /*
    public static final ItemEntry<PortalRadioItem> PORTAL_RADIO_ITEM =
            Etched.REGISTRATE.block("portal_radio", RadioBlock::new)
                    .item(PortalRadioItem::new).tab(CreativeModeTabs.TOOLS_AND_UTILITIES).build()
                    .register();
            EtchedItems.register("portal_radio", () -> new PortalRadioItem(RADIO.get(), new Item.Properties()));
*/
    public static final BlockEntityEntry<AlbumJukeboxBlockEntity> ALBUM_JUKEBOX_BE =
            (BlockEntityEntry<AlbumJukeboxBlockEntity>)(Object)Etched.REGISTRATE.blockEntity("album_jukebox", AlbumJukeboxBlockEntity::new)
                    .validBlocks(ALBUM_JUKEBOX)
                    .register();
    //BLOCK_ENTITIES.register("album_jukebox", () -> BlockEntityType.Builder.of(AlbumJukeboxBlockEntity::new, ALBUM_JUKEBOX.get()).build(null));
    public static final BlockEntityEntry<RadioBlockEntity> RADIO_BE =
        (BlockEntityEntry<RadioBlockEntity>)(Object)Etched.REGISTRATE.blockEntity("radio", RadioBlockEntity::new)
                    .validBlocks(RADIO)
                    .register();
    public static final BlockEntityEntry<gg.moonflower.etched.common.blockentity.SpeakerBlockEntity> SPEAKER_BE =
        (BlockEntityEntry<gg.moonflower.etched.common.blockentity.SpeakerBlockEntity>)(Object)Etched.REGISTRATE.blockEntity("speaker", gg.moonflower.etched.common.blockentity.SpeakerBlockEntity::new)
                    .validBlocks(SPEAKER)
                    .register();
    public static final BlockEntityEntry<gg.moonflower.etched.common.blockentity.StereoBlockEntity> STEREO_BE =
        (BlockEntityEntry<gg.moonflower.etched.common.blockentity.StereoBlockEntity>)(Object)Etched.REGISTRATE.blockEntity("stereo", gg.moonflower.etched.common.blockentity.StereoBlockEntity::new)
                    .validBlocks(STEREO)
                    .register();
    public static final BlockEntityEntry<gg.moonflower.etched.common.blockentity.AlbumDisplayBlockEntity> ALBUM_DISPLAY_BE =
        (BlockEntityEntry<gg.moonflower.etched.common.blockentity.AlbumDisplayBlockEntity>)(Object)Etched.REGISTRATE.blockEntity("album_display", gg.moonflower.etched.common.blockentity.AlbumDisplayBlockEntity::new)
                    .validBlocks(ALBUM_DISPLAY)
                    .register();
            //BLOCK_ENTITIES.register("radio", () -> BlockEntityType.Builder.of(RadioBlockEntity::new, RADIO.get()).build(null))
    public static void register() {} // i guess this is a hack?
}
