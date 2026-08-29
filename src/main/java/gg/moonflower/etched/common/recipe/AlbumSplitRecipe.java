package gg.moonflower.etched.common.recipe;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.common.item.AlbumCoverItem;
import gg.moonflower.etched.common.item.EtchedMusicDiscItem;
import gg.moonflower.etched.core.registry.EtchedItems;
import gg.moonflower.etched.core.registry.EtchedRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns an album etched onto one disc into a real album: a cover holding one disc per track.
 *
 * <p>Etching an album link writes every track onto a single disc, for the price of a single blank, which
 * made an album cheaper than the songs on it. Splitting it out costs a blank disc for every track after
 * the first, so an album ends up costing the same as etching its tracks one at a time, plus the cover.
 */
public class AlbumSplitRecipe extends CustomRecipe {

    //? if >=1.21 {
    /*public AlbumSplitRecipe(CraftingBookCategory category) {
        super(category);
    }
    *///?} else {
    public AlbumSplitRecipe(ResourceLocation resourceLocation, CraftingBookCategory category) {
        super(resourceLocation, category);
    }
    //?}

    /** The album disc, when the grid holds exactly one cover, one multi-track disc and enough blanks. */
    private static ItemStack findAlbumDisc(java.util.function.IntFunction<ItemStack> items, int size) {
        ItemStack album = ItemStack.EMPTY;
        int covers = 0;
        int blanks = 0;

        for (int i = 0; i < size; i++) {
            ItemStack stack = items.apply(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(EtchedItems.ALBUM_COVER.asItem())) {
                // Only an empty cover, so nothing already inside it is quietly thrown away.
                if (++covers > 1 || !AlbumCoverItem.getRecords(stack).isEmpty()) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(EtchedItems.BLANK_MUSIC_DISC.asItem())) {
                blanks++;
            } else if (stack.is(EtchedItems.ETCHED_MUSIC_DISC.asItem())) {
                if (!album.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                album = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (covers != 1 || album.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int tracks = trackCount(album);
        // One disc's worth is already etched, so the rest have to be paid for.
        return tracks > 1 && blanks == tracks - 1 ? album : ItemStack.EMPTY;
    }

    private static int trackCount(ItemStack album) {
        return PlayableRecord.getStackMusic(album).map(tracks -> tracks.length).orElse(0);
    }

    @Override
    //? if >=1.21 {
    /*public boolean matches(net.minecraft.world.item.crafting.CraftingInput inv, Level level) {
    return !findAlbumDisc(inv::getItem, inv.size()).isEmpty();
    *///?} else {
    public boolean matches(CraftingContainer inv, Level level) {
        return !findAlbumDisc(inv::getItem, inv.getContainerSize()).isEmpty();
    //?}
    }

    @Override
    //? if >=1.21 {
    /*public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput container, net.minecraft.core.HolderLookup.Provider registryAccess) {
    ItemStack album = findAlbumDisc(container::getItem, container.size());
    *///?} else {
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack album = findAlbumDisc(container::getItem, container.getContainerSize());
    //?}
        if (album.isEmpty()) {
            return ItemStack.EMPTY;
        }

        TrackData[] tracks = PlayableRecord.getStackMusic(album).orElse(new TrackData[0]);
        List<ItemStack> records = new ArrayList<>(tracks.length);
        for (TrackData track : tracks) {
            ItemStack disc = new ItemStack(EtchedItems.ETCHED_MUSIC_DISC.get());
            EtchedMusicDiscItem.setMusic(disc, track);
            // The album's own look carries onto every disc cut from it.
            EtchedMusicDiscItem.setColor(disc, EtchedMusicDiscItem.getDiscColor(album),
                    EtchedMusicDiscItem.getLabelPrimaryColor(album), EtchedMusicDiscItem.getLabelSecondaryColor(album));
            EtchedMusicDiscItem.setPattern(disc, EtchedMusicDiscItem.getPattern(album));
            records.add(disc);
        }

        ItemStack cover = new ItemStack(EtchedItems.ALBUM_COVER.get());
        AlbumCoverItem.setRecords(cover, records);
        return cover;
    }

    @Override
    //? if >=1.21 {
    /*public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider registryAccess) {
    *///?} else {
    public ItemStack getResultItem(RegistryAccess registryAccess) {
    //?}
        return new ItemStack(EtchedItems.ALBUM_COVER.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EtchedRecipes.SPLIT_ALBUM;
    }
}
