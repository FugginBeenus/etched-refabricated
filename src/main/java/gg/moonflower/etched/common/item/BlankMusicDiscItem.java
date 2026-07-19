package gg.moonflower.etched.common.item;

//? if >=1.21 {
/*import net.minecraft.world.item.component.DyedItemColor;
*///?} else {
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeableLeatherItem;
//?}
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

//? if >=1.21 {
/*public class BlankMusicDiscItem extends Item {
*///?} else {
public class BlankMusicDiscItem extends Item implements DyeableLeatherItem {
//?}

    public BlankMusicDiscItem(Properties properties) {
        super(properties);
    }

    //? if >=1.21 {
    /*public int getColor(ItemStack itemStack) {
        return DyedItemColor.getOrDefault(itemStack, 0x515151);
    }
    *///?} else {
    @Override
    public int getColor(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement("display");
        return compoundTag != null && compoundTag.contains("color", 99) ? compoundTag.getInt("color") : 0x515151;
    }
    //?}
}
