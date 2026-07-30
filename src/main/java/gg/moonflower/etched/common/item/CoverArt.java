package gg.moonflower.etched.common.item;

import gg.moonflower.etched.api.util.EtchedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public final class CoverArt {

    private static final String KEY = "CoverArt";
    private static final String IMAGE = "Image";
    private static final String BASE_COLOR = "BaseColor";
    private static final String PATTERNS = "Patterns";
    private static final String PATTERN = "Pattern";
    private static final String COLOR = "Color";

    private CoverArt() {
    }

    public static boolean has(ItemStack stack) {
        return EtchedData.getTagElement(stack, KEY) != null;
    }

    public static Optional<byte[]> getImage(ItemStack stack) {
        CompoundTag art = EtchedData.getTagElement(stack, KEY);
        if (art == null || !art.contains(IMAGE, Tag.TAG_STRING)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Base64.getDecoder().decode(art.getString(IMAGE)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static Optional<PatternDesign> getPattern(ItemStack stack) {
        CompoundTag art = EtchedData.getTagElement(stack, KEY);
        if (art == null || !art.contains(PATTERNS, Tag.TAG_LIST)) {
            return Optional.empty();
        }
        int baseColor = art.contains(BASE_COLOR, Tag.TAG_ANY_NUMERIC) ? art.getInt(BASE_COLOR) : 0xFFFFFF;
        ListTag list = art.getList(PATTERNS, Tag.TAG_COMPOUND);
        List<Layer> layers = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag layer = list.getCompound(i);
            layers.add(new Layer(layer.getInt(PATTERN), layer.getInt(COLOR)));
        }
        return layers.isEmpty() ? Optional.empty() : Optional.of(new PatternDesign(baseColor, layers));
    }

    public static void setImage(ItemStack stack, byte[] pngBytes) {
        String encoded = Base64.getEncoder().encodeToString(pngBytes);
        EtchedData.mutateTag(stack, tag -> {
            CompoundTag art = new CompoundTag();
            art.putString(IMAGE, encoded);
            tag.put(KEY, art);
        });
    }

    public static void setPattern(ItemStack stack, int baseColor, List<Layer> layers) {
        EtchedData.mutateTag(stack, tag -> {
            CompoundTag art = new CompoundTag();
            art.putInt(BASE_COLOR, baseColor);
            ListTag list = new ListTag();
            for (Layer layer : layers) {
                CompoundTag entry = new CompoundTag();
                entry.putInt(PATTERN, layer.pattern());
                entry.putInt(COLOR, layer.color());
                list.add(entry);
            }
            art.put(PATTERNS, list);
            tag.put(KEY, art);
        });
    }

    public static void clear(ItemStack stack) {
        EtchedData.removeTag(stack, KEY);
    }

    public record PatternDesign(int baseColor, List<Layer> layers) {
    }

    public record Layer(int pattern, int color) {
    }
}
