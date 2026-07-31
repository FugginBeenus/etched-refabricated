package gg.moonflower.etched.api.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public final class EtchedData {

    private EtchedData() {
    }

    public static CompoundTag getTag(ItemStack stack) {
        //? if >=1.21 {
        /*net.minecraft.world.item.component.CustomData data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : null;
        *///?} else {
        return stack.getTag();
        //?}
    }

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

    public static CompoundTag getTagElement(ItemStack stack, String key) {
        CompoundTag tag = getTag(stack);
        return tag != null && tag.contains(key, net.minecraft.nbt.Tag.TAG_COMPOUND) ? tag.getCompound(key) : null;
    }

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
