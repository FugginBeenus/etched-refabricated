package gg.moonflower.etched.core.registry;

import com.google.common.collect.ImmutableSet;
import gg.moonflower.etched.api.util.EtchedResourceLocation;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.fabric.EtchedConfig;
import gg.moonflower.etched.core.mixin.StructureTemplatePoolAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class EtchedVillagers {

    private static final String BARD = "bard";

    public static final PoiType BARD_POI = PointOfInterestHelper.register(
            EtchedResourceLocation.of(Etched.MOD_ID, BARD), 1, 1,
            blockStates(EtchedBlocks.ETCHING_TABLE.get()));

    public static final VillagerProfession BARD_PROFESSION = Registry.register(
            BuiltInRegistries.VILLAGER_PROFESSION,
            EtchedResourceLocation.of(Etched.MOD_ID, BARD),
            new VillagerProfession(Etched.MOD_ID + ":" + BARD,
                    poi -> poi.value().equals(BARD_POI),
                    poi -> poi.value().equals(BARD_POI),
                    ImmutableSet.of(), ImmutableSet.of(), null));

    @SuppressWarnings("unchecked")
    private static Set<BlockState> blockStates(Block block) {
        return ImmutableSet.copyOf((Collection<BlockState>) block.getStateDefinition().getPossibleStates());
    }

    public static void registers() {
        registerTrades();
        ServerLifecycleEvents.SERVER_STARTING.register(EtchedVillagers::addBardHouses);
    }

    // ---- trades ----

    private static void registerTrades() {
        TradeOfferHelper.registerVillagerOffers(BARD_PROFESSION, 1, trades -> {
            trades.add(buy(Items.MUSIC_DISC_13, 8, 1, 4, 20));
            trades.add(buy(Items.MUSIC_DISC_11, 8, 1, 4, 20));
            trades.add(buy(Items.MUSIC_DISC_CAT, 8, 1, 4, 20));
            trades.add(buy(Items.MUSIC_DISC_OTHERSIDE, 8, 1, 4, 20));
            trades.add(sell(Items.NOTE_BLOCK, 1, 2, 16, 2));
            trades.add(sell(EtchedItems.MUSIC_LABEL, 4, 2, 16, 1));
        });

        // Getting started: the parts needed to etch a disc at all.
        TradeOfferHelper.registerVillagerOffers(BARD_PROFESSION, 2, trades -> {
            trades.add(sell(EtchedItems.BLANK_MUSIC_DISC, 28, 2, 12, 15));
            trades.add(sell(EtchedBlocks.ETCHING_TABLE, 32, 1, 8, 15));
            trades.add(sell(Items.PAPER, 1, 8, 16, 5));
        });

        // Furnishing a listening room.
        TradeOfferHelper.registerVillagerOffers(BARD_PROFESSION, 3, trades -> {
            trades.add(sell(EtchedBlocks.SPEAKER, 18, 1, 8, 10));
            trades.add(sell(EtchedBlocks.ALBUM_DISPLAY, 8, 1, 12, 5));
            trades.add(sell(EtchedBlocks.ALBUM_CRATE, 12, 1, 12, 5));
            trades.add(sell(Items.COPPER_INGOT, 1, 2, 16, 5));
            trades.add(sell(Items.JUKEBOX, 26, 1, 4, 30));
        });

        // The bigger machines.
        TradeOfferHelper.registerVillagerOffers(BARD_PROFESSION, 4, trades -> {
            trades.add(sell(EtchedItems.ALBUM_COVER, 16, 1, 4, 30));
            trades.add(sell(EtchedBlocks.RADIO, 24, 1, 4, 30));
            trades.add(sell(EtchedItems.JUKEBOX_MINECART, 28, 1, 4, 30));
            trades.add(sell(EtchedBlocks.ALBUM_JUKEBOX, 30, 1, 4, 30));
            trades.add(sell(EtchedBlocks.ALBUM_PRINTER, 34, 1, 4, 30));
        });

        // The stereo and the upgrades that drive it.
        TradeOfferHelper.registerVillagerOffers(BARD_PROFESSION, 5, trades -> {
            trades.add(buy(Items.DIAMOND, 8, 1, 8, 40));
            trades.add(sell(EtchedItems.PREAMP, 22, 1, 6, 40));
            trades.add(sell(EtchedItems.TRANSMITTER, 26, 1, 6, 40));
            trades.add(sell(EtchedBlocks.STEREO, 36, 1, 4, 40));
        });
    }

    private static VillagerTrades.ItemListing buy(ItemLike item, int emeralds, int itemCount, int maxUses, int xp) {
        return new ItemTrade(() -> item, emeralds, itemCount, maxUses, xp, true);
    }

    // Registrate's entries are themselves ItemLike, so one overload covers both vanilla items and ours.
    private static VillagerTrades.ItemListing sell(ItemLike item, int emeralds, int itemCount, int maxUses, int xp) {
        return new ItemTrade(() -> item, emeralds, itemCount, maxUses, xp, false);
    }

    // ---- village generation ----

    private static void addBardHouses(MinecraftServer server) {
        if (!EtchedConfig.HANDLER.instance().addBardHousesToVillages) {
            return;
        }

        RegistryAccess access = server.registryAccess();
        Optional<Registry<StructureTemplatePool>> poolRegistry = access.registry(Registries.TEMPLATE_POOL);
        Optional<Registry<StructureProcessorList>> processorRegistry = access.registry(Registries.PROCESSOR_LIST);
        if (poolRegistry.isEmpty() || processorRegistry.isEmpty()) {
            return;
        }

        Registry<StructureTemplatePool> pools = poolRegistry.get();
        Registry<StructureProcessorList> processors = processorRegistry.get();
        ResourceManager resources = server.getResourceManager();
        int filled = 0;
        filled += addHouses(resources, pools, processors, "plains", 2, ProcessorLists.MOSSIFY_10_PERCENT, ProcessorLists.ZOMBIE_PLAINS);
        filled += addHouses(resources, pools, processors, "desert", 2, ProcessorLists.EMPTY, ProcessorLists.ZOMBIE_DESERT);
        filled += addHouses(resources, pools, processors, "savanna", 4, ProcessorLists.EMPTY, ProcessorLists.ZOMBIE_SAVANNA);
        filled += addHouses(resources, pools, processors, "snowy", 4, ProcessorLists.EMPTY, ProcessorLists.ZOMBIE_SNOWY);
        filled += addHouses(resources, pools, processors, "taiga", 4, ProcessorLists.MOSSIFY_10_PERCENT, ProcessorLists.ZOMBIE_TAIGA);

        // Logged so a pack that never spawns a bard can be diagnosed from the log rather than guessed at.
        if (filled == 0) {
            Etched.LOGGER.warn("Found no village house pools to add bard houses to. Bards will only appear "
                    + "where an etching table is placed by hand.");
        } else {
            Etched.LOGGER.info("Added bard houses to {} village house pools", filled);
        }
    }

    /**
     * Adds every bard house that exists for a village type to every house pool belonging to that type.
     *
     * <p>The pools are searched for rather than named outright. Vanilla's are
     * {@code minecraft:village/<type>/houses} and its zombie twin, but a village overhaul usually ships
     * pools under its own ids, and naming only vanilla's is why bards never turned up in those packs.
     *
     * @return How many pools received houses
     */
    private static int addHouses(ResourceManager resources,
                                 Registry<StructureTemplatePool> pools, Registry<StructureProcessorList> processors,
                                 String village, int weight,
                                 ResourceKey<StructureProcessorList> processor,
                                 ResourceKey<StructureProcessorList> zombieProcessor) {
        List<ResourceLocation> pieces = new java.util.ArrayList<>();
        for (int variant = 1; ; variant++) {
            String path = "village/" + village + "/houses/" + village + "_" + BARD + "_house_" + variant;
            if (resources.getResource(EtchedResourceLocation.of(Etched.MOD_ID, "structures/" + path + ".nbt")).isEmpty()) {
                break;
            }
            pieces.add(EtchedResourceLocation.of(Etched.MOD_ID, path));
        }
        if (pieces.isEmpty()) {
            return 0;
        }

        Holder<StructureProcessorList> normal = processors.getHolder(processor).orElse(null);
        Holder<StructureProcessorList> zombie = processors.getHolder(zombieProcessor).orElse(null);

        int filled = 0;
        for (java.util.Map.Entry<ResourceKey<StructureTemplatePool>, StructureTemplatePool> entry : pools.entrySet()) {
            String path = entry.getKey().location().getPath();
            if (!isVillageHousePool(path, village)) {
                continue;
            }

            Holder<StructureProcessorList> list = path.contains("zombie") ? zombie : normal;
            boolean added = false;
            for (ResourceLocation piece : pieces) {
                added |= addToPool(entry.getValue(), piece, list, weight);
            }
            if (added) {
                filled++;
            }
        }
        return filled;
    }

    /**
     * Deliberately loose, so it catches an overhaul's own naming as well as vanilla's. The village type
     * still has to match, so a plains bard house never lands in a desert village.
     */
    private static boolean isVillageHousePool(String path, String village) {
        return path.contains("village") && path.contains(village) && path.contains("houses");
    }

    private static boolean addToPool(@Nullable StructureTemplatePool pool, ResourceLocation pieceId,
                                     @Nullable Holder<StructureProcessorList> processorList, int weight) {
        // A missing pool means something else owns this village now; leave it alone rather than forcing
        // our way in.
        if (pool == null || processorList == null) {
            return false;
        }

        List<StructurePoolElement> templates = ((StructureTemplatePoolAccessor) pool).getTemplates();
        if (templates == null) {
            return false;
        }

        StructurePoolElement piece = StructurePoolElement.legacy(pieceId.toString(), processorList)
                .apply(StructureTemplatePool.Projection.RIGID);
        try {
            for (int i = 0; i < weight; i++) {
                templates.add(piece);
            }
            return true;
        } catch (UnsupportedOperationException e) {
            // Another mod has frozen the pool. Missing from that village beats failing to load the world.
            Etched.LOGGER.warn("Could not add {} to a village pool: its template list is not modifiable", pieceId);
            return false;
        }
    }

    private static class ItemTrade implements VillagerTrades.ItemListing {

        private final Supplier<? extends ItemLike> item;
        private final int emeralds;
        private final int itemCount;
        private final int maxUses;
        private final int xp;
        private final boolean sellToVillager;

        ItemTrade(Supplier<? extends ItemLike> item, int emeralds, int itemCount, int maxUses, int xp, boolean sellToVillager) {
            this.item = item;
            this.emeralds = emeralds;
            this.itemCount = itemCount;
            this.maxUses = maxUses;
            this.xp = xp;
            this.sellToVillager = sellToVillager;
        }

        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource random) {
            ItemLike traded = this.item.get();
            if (this.sellToVillager) {
                return this.offer(traded, this.itemCount, new ItemStack(Items.EMERALD, this.emeralds));
            }
            return this.offer(Items.EMERALD, this.emeralds, new ItemStack(traded, this.itemCount));
        }

        private MerchantOffer offer(ItemLike costItem, int costCount, ItemStack result) {
            //? if >=1.21 {
            /*return new MerchantOffer(new net.minecraft.world.item.trading.ItemCost(costItem, costCount),
                    result, this.maxUses, this.xp, 0.05F);
            *///?} else {
            return new MerchantOffer(new ItemStack(costItem, costCount), result, this.maxUses, this.xp, 0.05F);
            //?}
        }
    }
}
