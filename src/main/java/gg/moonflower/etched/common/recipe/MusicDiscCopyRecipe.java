package gg.moonflower.etched.common.recipe;

import gg.moonflower.etched.core.registry.EtchedItems;
import gg.moonflower.etched.core.registry.EtchedRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Copies an etched disc onto a blank one, the way vanilla clones a map or copies a book. The disc being
 * copied is a template rather than an ingredient, so it is handed back and only the blank is spent.
 *
 * <p>Without this the only way to get a second copy was to type the whole link again.
 */
public class MusicDiscCopyRecipe extends CustomRecipe {

    //? if >=1.21 {
    /*public MusicDiscCopyRecipe(CraftingBookCategory category) {
        super(category);
    }
    *///?} else {
    public MusicDiscCopyRecipe(ResourceLocation resourceLocation, CraftingBookCategory category) {
        super(resourceLocation, category);
    }
    //?}

    @Override
    //? if >=1.21 {
    /*public boolean matches(net.minecraft.world.item.crafting.CraftingInput inv, Level level) {
    int size = inv.size();
    *///?} else {
    public boolean matches(CraftingContainer inv, Level level) {
        int size = inv.getContainerSize();
    //?}
        int etched = 0;
        int blank = 0;

        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == EtchedItems.ETCHED_MUSIC_DISC.get()) {
                etched++;
            } else if (stack.getItem() == EtchedItems.BLANK_MUSIC_DISC.get()) {
                blank++;
            } else {
                return false;
            }
        }

        return etched == 1 && blank == 1;
    }

    @Override
    //? if >=1.21 {
    /*public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput container, net.minecraft.core.HolderLookup.Provider registryAccess) {
    int size = container.size();
    *///?} else {
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        int size = container.getContainerSize();
    //?}
        for (int i = 0; i < size; i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() == EtchedItems.ETCHED_MUSIC_DISC.get()) {
                // A plain copy carries the track, the labels and the colours with it.
                ItemStack copy = stack.copy();
                copy.setCount(1);
                return copy;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    //? if >=1.21 {
    /*public NonNullList<ItemStack> getRemainingItems(net.minecraft.world.item.crafting.CraftingInput container) {
    int size = container.size();
    *///?} else {
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        int size = container.getContainerSize();
    //?}
        NonNullList<ItemStack> remaining = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < size; i++) {
            ItemStack stack = container.getItem(i);
            // The disc being copied stays; only the blank is used up.
            if (stack.getItem() == EtchedItems.ETCHED_MUSIC_DISC.get()) {
                remaining.set(i, stack.copy());
            }
        }
        return remaining;
    }

    @Override
    //? if >=1.21 {
    /*public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider registryAccess) {
    *///?} else {
    public ItemStack getResultItem(RegistryAccess registryAccess) {
    //?}
        return new ItemStack(EtchedItems.ETCHED_MUSIC_DISC.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EtchedRecipes.COPY_MUSIC_DISC;
    }
}
