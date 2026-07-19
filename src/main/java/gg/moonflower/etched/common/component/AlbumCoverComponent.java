package gg.moonflower.etched.common.component;

// Typed data component for album-cover contents (record stacks + cover art). 1.21+ only; on 1.20.1
// the album cover stores this data in the item NBT instead (see AlbumCoverItem). Kept intentionally
// close to the mod's existing data shape ("Records" list + "CoverRecord") so the two backends round-trip
// the same logical data.
//? if >=1.21 {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record AlbumCoverComponent(List<ItemStack> records, ItemStack cover) {

    public static final int MAX_RECORDS = 9;
    public static final AlbumCoverComponent EMPTY = new AlbumCoverComponent(List.of(), ItemStack.EMPTY);

    public static final Codec<AlbumCoverComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf(0, MAX_RECORDS).optionalFieldOf("Records", List.of()).forGetter(AlbumCoverComponent::records),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("CoverRecord", ItemStack.EMPTY).forGetter(AlbumCoverComponent::cover)
    ).apply(instance, AlbumCoverComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlbumCoverComponent> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_RECORDS)), AlbumCoverComponent::records,
            ItemStack.OPTIONAL_STREAM_CODEC, AlbumCoverComponent::cover,
            AlbumCoverComponent::new
    );

    public boolean isEmpty() {
        return this.records.isEmpty() && this.cover.isEmpty();
    }

    public AlbumCoverComponent withCover(ItemStack cover) {
        return new AlbumCoverComponent(this.records, cover);
    }

    public AlbumCoverComponent withRecords(List<ItemStack> records) {
        return new AlbumCoverComponent(records, this.cover);
    }
}
*///?}
