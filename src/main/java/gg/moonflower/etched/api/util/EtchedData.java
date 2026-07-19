package gg.moonflower.etched.api.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Version-abstracted access to the mod's custom item data.
 * <p>
 * Prior to 1.20.5 mods stored arbitrary data in the item stack's NBT tag. From 1.20.5 onward that
 * tag is gone and custom NBT lives in the vanilla {@code minecraft:custom_data} component. Routing
 * every read/write through here keeps the mod's data logic identical on every supported version,
 * with only the storage backend differing.
 */
public final class EtchedData {

    private EtchedData() {
    }

    /**
     * @return the mod's custom NBT for the stack, or {@code null} if none is present. The returned
     * tag is a copy on 1.21+ and must not be mutated in place; use {@link #mutateTag} to write.
     */
    public static CompoundTag getTag(ItemStack stack) {
        //? if >=1.21 {
        /*net.minecraft.world.item.component.CustomData data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : null;
        *///?} else {
        return stack.getTag();
        //?}
    }

    /**
     * Applies {@code mutator} to the stack's custom NBT and persists the result, creating the tag
     * if absent.
     */
    public static void mutateTag(ItemStack stack, Consumer<CompoundTag> mutator) {
        //? if >=1.21 {
        /*net.minecraft.world.item.component.CustomData existing = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();
        mutator.accept(tag);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        *///?} else {
        mutator.accept(stack.getOrCreateTag());
        //?}
    }

    /**
     * Removes {@code key} from the stack's custom NBT, clearing the storage entirely if it becomes empty.
     */
    public static void removeTag(ItemStack stack, String key) {
        //? if >=1.21 {
        /*net.minecraft.world.item.component.CustomData existing = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (existing == null) {
            return;
        }
        CompoundTag tag = existing.copyTag();
        tag.remove(key);
        if (tag.isEmpty()) {
            stack.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        } else {
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        }
        *///?} else {
        stack.removeTagKey(key);
        //?}
    }
}
