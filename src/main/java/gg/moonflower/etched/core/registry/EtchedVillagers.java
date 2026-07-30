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
import net.minecraft.world.level.block.Blocks;
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

/**
 * The bard: a villager who works at an etching table and deals in music.
 *
 * <p>Bard houses are appended to the village pools when a server starts rather than shipped as a
 * datapack, because a datapack file <i>replaces</i> a pool and would wipe out whatever another mod had
 * put there. Appending at runtime is additive, so villages keep everything else added to them, and a
 * village overhaul that owns its own pools simply doesn't receive the house.
 */
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

    /**
     * Registers the bard's trades and hooks village generation. Loading this class is also what registers
     * the point of interest and the profession, so this has to be called during mod init: with nothing
     * referencing the class, the static fields above never ran and the bard could never be hired.
     */
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

        TradeOfferHelper.registerVillagerOffers(BARD_PROFESSION, 2, trades -> {
            trades.add(sell(EtchedItems.BLANK_MUSIC_DISC, 28, 2, 12, 15));
            trades.add(sell(EtchedBlocks.ETCHING_TABLE, 32, 1, 8, 15));
        });

        TradeOfferHelper.registerVillagerOffers(BARD_PROFESSION, 3, trades -> {
            trades.add(sell(Blocks.CLAY, 6, 1, 16, 2));
            trades.add(sell(Blocks.HAY_BLOCK, 12, 1, 8, 2));
            trades.add(sell(Blocks.WHITE_WOOL, 8, 1, 32, 4));
            trades.add(sell(Blocks.BONE_BLOCK, 24, 1, 8, 4));
            trades.add(sell(Blocks.PACKED_ICE, 36, 1, 4, 8));
            trades.add(sell(Blocks.GOLD_BLOCK, 48, 1, 2, 10));
            trades.add(sell(Items.JUKEBOX, 26, 1, 4, 30));
        });

        TradeOfferHelper.registerVillagerOffers(BARD_PROFESSION, 4, trades -> {
            trades.add(sell(EtchedItems.ALBUM_COVER, 16, 1, 4, 30));
            trades.add(sell(EtchedItems.JUKEBOX_MINECART, 28, 1, 4, 30));
            trades.add(sell(EtchedBlocks.ALBUM_JUKEBOX, 30, 1, 4, 30));
        });

        TradeOfferHelper.registerVillagerOffers(BARD_PROFESSION, 5, trades -> {
            trades.add(buy(Items.DIAMOND, 8, 1, 8, 40));
            trades.add(buy(Items.AMETHYST_SHARD, 1, 8, 10, 40));
        });
    }

    /**
     * A trade where the player hands over items and gets emeralds back.
     */
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
        addHouse(pools, processors, "plains", 2, ProcessorLists.MOSSIFY_10_PERCENT, ProcessorLists.ZOMBIE_PLAINS);
        addHouse(pools, processors, "desert", 2, ProcessorLists.EMPTY, ProcessorLists.ZOMBIE_DESERT);
        addHouse(pools, processors, "savanna", 4, ProcessorLists.EMPTY, ProcessorLists.ZOMBIE_SAVANNA);
        addHouse(pools, processors, "snowy", 4, ProcessorLists.EMPTY, ProcessorLists.ZOMBIE_SNOWY);
        addHouse(pools, processors, "taiga", 4, ProcessorLists.MOSSIFY_10_PERCENT, ProcessorLists.ZOMBIE_TAIGA);
    }

    private static void addHouse(Registry<StructureTemplatePool> pools, Registry<StructureProcessorList> processors,
                                 String village, int weight,
                                 ResourceKey<StructureProcessorList> processor,
                                 ResourceKey<StructureProcessorList> zombieProcessor) {
        ResourceLocation piece = EtchedResourceLocation.of(Etched.MOD_ID,
                "village/" + village + "/houses/" + village + "_" + BARD + "_house_1");
        addToPool(pools.get(EtchedResourceLocation.of("village/" + village + "/houses")),
                piece, processors.getHolder(processor).orElse(null), weight);
        addToPool(pools.get(EtchedResourceLocation.of("village/" + village + "/zombie/houses")),
                piece, processors.getHolder(zombieProcessor).orElse(null), weight);
    }

    private static void addToPool(@Nullable StructureTemplatePool pool, ResourceLocation pieceId,
                                  @Nullable Holder<StructureProcessorList> processorList, int weight) {
        // A missing pool means something else owns this village now; leave it alone rather than forcing
        // our way in.
        if (pool == null || processorList == null) {
            return;
        }

        List<StructurePoolElement> templates = ((StructureTemplatePoolAccessor) pool).getTemplates();
        if (templates == null) {
            return;
        }

        StructurePoolElement piece = StructurePoolElement.legacy(pieceId.toString(), processorList)
                .apply(StructureTemplatePool.Projection.RIGID);
        try {
            for (int i = 0; i < weight; i++) {
                templates.add(piece);
            }
        } catch (UnsupportedOperationException e) {
            // Another mod has frozen the pool. Missing from that village beats failing to load the world.
            Etched.LOGGER.warn("Could not add {} to a village pool: its template list is not modifiable", pieceId);
        }
    }

    /**
     * A straightforward trade of items for emeralds, or emeralds for items. Vanilla's
     * {@code EmeraldForItems} only expresses "several items for one emerald", which is the wrong way round
     * for something like a music disc that should be worth eight.
     */
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
