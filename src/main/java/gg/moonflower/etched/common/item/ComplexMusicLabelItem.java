package gg.moonflower.etched.common.item;

import gg.moonflower.etched.api.util.EtchedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ComplexMusicLabelItem extends SimpleMusicLabelItem {

    public ComplexMusicLabelItem(Properties properties) {
        super(properties);
    }

    public static int getPrimaryColor(ItemStack stack) {
        CompoundTag compoundTag = EtchedData.getTagElement(stack, "Label");
        return compoundTag != null && compoundTag.contains("PrimaryColor", 99) ? compoundTag.getInt("PrimaryColor") : 0xFFFFFF;
    }

    public static int getSecondaryColor(ItemStack itemStack) {
        CompoundTag compoundTag = EtchedData.getTagElement(itemStack, "Label");
        return compoundTag != null && compoundTag.contains("SecondaryColor", 99) ? compoundTag.getInt("SecondaryColor") : 0xFFFFFF;
    }

    public static void setColor(ItemStack stack, int primary, int secondary) {
        if (!(stack.getItem() instanceof ComplexMusicLabelItem)) {
            return;
        }
        EtchedData.mutateTag(stack, nbt -> {
            CompoundTag tag = nbt.getCompound("Label");
            tag.putInt("PrimaryColor", primary);
            tag.putInt("SecondaryColor", secondary);
            nbt.put("Label", tag);
        });
    }
}
