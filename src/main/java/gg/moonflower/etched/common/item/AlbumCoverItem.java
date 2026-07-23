package gg.moonflower.etched.common.item;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.PlayableRecordItem;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.common.menu.AlbumCoverMenu;
import gg.moonflower.etched.core.fabric.EtchedConfig;
import gg.moonflower.etched.core.registry.EtchedItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AlbumCoverItem extends PlayableRecordItem implements ContainerItem {

    public static final int MAX_RECORDS = 9;

    public AlbumCoverItem(Properties properties) {
        super(properties);
    }

    // Albums only play in the album jukebox; suppress the inherited PlayableRecordItem.useOn that
    // would otherwise insert the cover into a regular jukebox.
    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isSecondaryUseActive()) {
            if (dropContents(stack, player)) {
                this.playDropContentsSound(player);
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
            return InteractionResultHolder.pass(stack);
        }

        if (!EtchedConfig.HANDLER.instance().useAlbumCoverMenu) {
            return InteractionResultHolder.fail(stack);
        }
        return this.use(this, level, player, hand);
    }

    @Override
    public AbstractContainerMenu constructMenu(int containerId, Inventory inventory, Player player, int index) {
        return new AlbumCoverMenu(containerId, inventory, index);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack albumCover, Slot slot, ClickAction clickAction, Player player) {
        if (EtchedConfig.HANDLER.instance().useAlbumCoverMenu) {
            return false;
        }
        if (clickAction != ClickAction.SECONDARY) {
            return false;
        }

        ItemStack clickItem = slot.getItem();
        if (clickItem.isEmpty()) {
            removeOne(albumCover).ifPresent(record -> {
                this.playRemoveOneSound(player);
                add(albumCover, slot.safeInsert(record));
            });
        } else if (canAdd(albumCover, clickItem)) {
            this.playInsertSound(player);
            add(albumCover, slot.safeTake(clickItem.getCount(), 1, player));
        }

        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack albumCover, ItemStack clickItem, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
        if (EtchedConfig.HANDLER.instance().useAlbumCoverMenu) {
            return false;
        }
        if (clickAction == ClickAction.SECONDARY && slot.allowModification(player)) {
            if (clickItem.isEmpty()) {
                removeOne(albumCover).ifPresent(removedRecord -> {
                    this.playRemoveOneSound(player);
                    slotAccess.set(removedRecord);
                });
            } else if (canAdd(albumCover, clickItem)) {
                this.playInsertSound(player);
                add(albumCover, clickItem);
            }

            return true;
        }

        return false;
    }

    //? if >=1.21 {
    /*@Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> list, TooltipFlag tooltipFlag) {
        for (ItemStack record : getRecords(stack)) {
            if (record.getItem() instanceof PlayableRecord) {
                record.getItem().appendHoverText(record, context, list, tooltipFlag);
            }
        }
    }
    *///?} else {
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        for (ItemStack record : getRecords(stack)) {
            if (record.getItem() instanceof PlayableRecord) {
                record.getItem().appendHoverText(record, level, list, tooltipFlag);
            }
        }
    }
    //?}

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        //? if >=1.21 {
        /*ItemUtils.onContainerDestroyed(itemEntity, getRecords(itemEntity.getItem()));
        *///?} else {
        ItemUtils.onContainerDestroyed(itemEntity, getRecords(itemEntity.getItem()).stream());
        //?}
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static Optional<ItemStack> removeOne(ItemStack albumCover) {
        List<ItemStack> records = new ArrayList<>(getRecords(albumCover));
        if (records.isEmpty()) {
            return Optional.empty();
        }
        ItemStack removed = records.remove(records.size() - 1);
        setRecords(albumCover, records);
        return Optional.of(removed);
    }

    private static boolean dropContents(ItemStack itemStack, Player player) {
        List<ItemStack> records = getRecords(itemStack);
        if (records.isEmpty()) {
            return false;
        }

        if (player instanceof ServerPlayer) {
            for (ItemStack record : records) {
                player.getInventory().placeItemBackInInventory(record);
            }
        }

        setRecords(itemStack, List.of());
        return true;
    }

    private static void add(ItemStack albumCover, ItemStack record) {
        if (!albumCover.is(EtchedItems.ALBUM_COVER.asItem()) || !AlbumCoverMenu.isValid(record)) {
            return;
        }

        List<ItemStack> records = new ArrayList<>(getRecords(albumCover));
        if (records.size() >= MAX_RECORDS) {
            return;
        }
        records.add(record.split(1));
        setRecords(albumCover, records);

        if (getCoverStack(albumCover).isEmpty()) {
            records.stream().filter(stack -> !stack.isEmpty()).findFirst().ifPresent(stack -> setCover(albumCover, stack));
        }
    }

    private static boolean canAdd(ItemStack albumCover, ItemStack record) {
        if (!albumCover.is(EtchedItems.ALBUM_COVER.asItem()) || !AlbumCoverMenu.isValid(record)) {
            return false;
        }
        return getRecords(albumCover).size() < MAX_RECORDS;
    }

    @Override
    public Optional<TrackData[]> getMusic(ItemStack stack) {
        List<ItemStack> records = getRecords(stack);
        return records.isEmpty() ? Optional.empty() : Optional.of(records.stream().filter(record -> record.getItem() instanceof PlayableRecord).flatMap(record -> Arrays.stream(((PlayableRecord) record.getItem()).getMusic(record).orElseGet(() -> new TrackData[0]))).toArray(TrackData[]::new));
    }

    @Override
    public Optional<TrackData> getAlbum(ItemStack stack) {
        return Optional.empty();
    }

    @Override
    public int getTrackCount(ItemStack stack) {
        return getRecords(stack).stream().filter(record -> record.getItem() instanceof PlayableRecord).mapToInt(record -> ((PlayableRecord) record.getItem()).getTrackCount(record)).sum();
    }

    // ---- version-abstracted storage: typed component on 1.21+, item NBT on 1.20.1 ----

    public static Optional<ItemStack> getCoverStack(ItemStack stack) {
        if (stack.getItem() != EtchedItems.ALBUM_COVER.asItem()) {
            return Optional.empty();
        }
        ItemStack cover = getCover(stack);
        return cover.isEmpty() ? Optional.empty() : Optional.of(cover);
    }

    private static ItemStack getCover(ItemStack stack) {
        //? if >=1.21 {
        /*gg.moonflower.etched.common.component.AlbumCoverComponent component = stack.get(gg.moonflower.etched.core.registry.EtchedComponents.ALBUM_COVER);
        return component != null ? component.cover() : ItemStack.EMPTY;
        *///?} else {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains("CoverRecord", Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(nbt.getCompound("CoverRecord"));
        //?}
    }

    public static List<ItemStack> getRecords(ItemStack stack) {
        if (stack.getItem() != EtchedItems.ALBUM_COVER.asItem()) {
            return Collections.emptyList();
        }
        //? if >=1.21 {
        /*gg.moonflower.etched.common.component.AlbumCoverComponent component = stack.get(gg.moonflower.etched.core.registry.EtchedComponents.ALBUM_COVER);
        if (component == null) {
            return Collections.emptyList();
        }
        List<ItemStack> list = new ArrayList<>();
        for (ItemStack record : component.records()) {
            if (!record.isEmpty() && list.size() < MAX_RECORDS) {
                list.add(record);
            }
        }
        return list;
        *///?} else {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains("Records", Tag.TAG_LIST)) {
            return Collections.emptyList();
        }

        ListTag recordsNbt = nbt.getList("Records", Tag.TAG_COMPOUND);
        if (recordsNbt.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemStack> list = new ArrayList<>(recordsNbt.size());
        for (int i = 0; i < Math.min(MAX_RECORDS, recordsNbt.size()); i++) {
            ItemStack record = ItemStack.of(recordsNbt.getCompound(i));
            if (!record.isEmpty()) {
                list.add(record);
            }
        }

        return list;
        //?}
    }

    public static void setCover(ItemStack stack, ItemStack record) {
        if (stack.getItem() != EtchedItems.ALBUM_COVER.asItem()) {
            return;
        }
        //? if >=1.21 {
        /*gg.moonflower.etched.common.component.AlbumCoverComponent component = stack.getOrDefault(gg.moonflower.etched.core.registry.EtchedComponents.ALBUM_COVER, gg.moonflower.etched.common.component.AlbumCoverComponent.EMPTY);
        stack.set(gg.moonflower.etched.core.registry.EtchedComponents.ALBUM_COVER, component.withCover(record.isEmpty() ? ItemStack.EMPTY : record.copyWithCount(1)));
        *///?} else {
        if (record.isEmpty()) {
            stack.removeTagKey("CoverRecord");
            return;
        }
        stack.getOrCreateTag().put("CoverRecord", record.save(new CompoundTag()));
        //?}
    }

    public static void setRecords(ItemStack stack, Collection<ItemStack> records) {
        if (stack.getItem() != EtchedItems.ALBUM_COVER.asItem()) {
            return;
        }
        List<ItemStack> trimmed = new ArrayList<>();
        for (ItemStack record : records) {
            if (!record.isEmpty() && trimmed.size() < MAX_RECORDS) {
                trimmed.add(record);
            }
        }
        //? if >=1.21 {
        /*gg.moonflower.etched.common.component.AlbumCoverComponent component = stack.getOrDefault(gg.moonflower.etched.core.registry.EtchedComponents.ALBUM_COVER, gg.moonflower.etched.common.component.AlbumCoverComponent.EMPTY);
        stack.set(gg.moonflower.etched.core.registry.EtchedComponents.ALBUM_COVER, component.withRecords(trimmed));
        *///?} else {
        ListTag recordsNbt = new ListTag();
        for (ItemStack record : trimmed) {
            recordsNbt.add(record.save(new CompoundTag()));
        }
        stack.getOrCreateTag().put("Records", recordsNbt);
        //?}
    }
}
