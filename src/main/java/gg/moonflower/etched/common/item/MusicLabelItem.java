package gg.moonflower.etched.common.item;

//? if >=1.21 {
/*import net.minecraft.world.item.component.DyedItemColor;
*///?} else {
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeableLeatherItem;
//?}
import net.minecraft.world.item.ItemStack;

//? if >=1.21 {
/*public class MusicLabelItem extends SimpleMusicLabelItem {
*///?} else {
public class MusicLabelItem extends SimpleMusicLabelItem implements DyeableLeatherItem {
//?}
    public MusicLabelItem(Properties properties) {
        super(properties);
    }

    //? if >=1.21 {
    /*public int getColor(ItemStack itemStack) {
        return DyedItemColor.getOrDefault(itemStack, 0xFFFFFF);
    }
    *///?} else {
    @Override
    public int getColor(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement("display");
        return compoundTag != null && compoundTag.contains("color", 99) ? compoundTag.getInt("color") : 0xFFFFFF;
    }
    //?}

    public static int getLabelColor(ItemStack stack) {
        if (stack.getItem() instanceof MusicLabelItem) {
            return ((MusicLabelItem) stack.getItem()).getColor(stack);
        }
        return -1;
    }
}
