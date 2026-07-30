package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.core.registry.EtchedItems;
import gg.moonflower.etched.core.registry.EtchedMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class AlbumPrinterMenu extends AbstractContainerMenu {

    public static final int MODE_PATTERN = 0;
    public static final int MODE_IMAGE = 1;
    // Cap layered patterns like banners do (6 patterns over a base).
    public static final int MAX_LAYERS = 6;

    private final ContainerLevelAccess access;
    private final Player player;
    private final Container input;
    private final Container result;
    private final Slot coverSlot;
    private final Slot dyeSlot;
    private final Slot resultSlot;
    // Selected mode / pattern, synced to the client for the screen.
    private final DataSlot mode = DataSlot.standalone();
    private final DataSlot selectedPattern = DataSlot.standalone();
    // Processed image bytes uploaded this session (image mode); server-side only.
    private byte[] pendingImage;

    public AlbumPrinterMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public AlbumPrinterMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(EtchedMenus.ALBUM_PRINTER_MENU, id);
        this.player = inventory.player;
        this.access = access;
        this.input = new SimpleContainer(2) {
            @Override
            public void setChanged() {
                super.setChanged();
                AlbumPrinterMenu.this.slotsChanged(this);
            }
        };
        this.result = new SimpleContainer(1);

        this.coverSlot = this.addSlot(new Slot(this.input, 0, 13, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(EtchedItems.ALBUM_COVER.asItem());
            }
        });
        this.dyeSlot = this.addSlot(new Slot(this.input, 1, 13, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof DyeItem;
            }
        });
        this.resultSlot = this.addSlot(new Slot(this.result, 0, 150, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                AlbumPrinterMenu.this.coverSlot.remove(1);
                if (AlbumPrinterMenu.this.mode.get() == MODE_PATTERN) {
                    AlbumPrinterMenu.this.dyeSlot.remove(1);
                }
                AlbumPrinterMenu.this.setupResultSlot();
                super.onTake(player, stack);
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        this.addDataSlot(this.mode);
        this.addDataSlot(this.selectedPattern);
    }

    public int getMode() {
        return this.mode.get();
    }

    public int getSelectedPattern() {
        return this.selectedPattern.get();
    }

    public ItemStack getCoverStack() {
        return this.coverSlot.getItem();
    }

    public ItemStack getDyeStack() {
        return this.dyeSlot.getItem();
    }

    public void setPendingImage(byte[] image) {
        this.pendingImage = image;
        this.mode.set(MODE_IMAGE);
        this.setupResultSlot();
    }

    // Rebuilds the result from the current cover + selected design (uploaded image or a dye-tinted
    // procedural pattern).
    private void setupResultSlot() {
        ItemStack cover = this.coverSlot.getItem();
        ItemStack result = ItemStack.EMPTY;
        if (!cover.isEmpty()) {
            if (this.mode.get() == MODE_IMAGE && this.pendingImage != null) {
                result = cover.copyWithCount(1);
                gg.moonflower.etched.common.item.CoverArt.setImage(result, this.pendingImage);
            } else if (this.mode.get() == MODE_PATTERN && this.selectedPattern.get() >= 0 && !this.dyeSlot.getItem().isEmpty()) {
                // Banner-style layering: keep the cover's existing layers and add one more on top.
                java.util.Optional<gg.moonflower.etched.common.item.CoverArt.PatternDesign> existing =
                        gg.moonflower.etched.common.item.CoverArt.getPattern(cover);
                java.util.List<gg.moonflower.etched.common.item.CoverArt.Layer> layers =
                        new java.util.ArrayList<>(existing.map(gg.moonflower.etched.common.item.CoverArt.PatternDesign::layers).orElse(java.util.List.of()));
                if (layers.size() < MAX_LAYERS) {
                    int base = existing.map(gg.moonflower.etched.common.item.CoverArt.PatternDesign::baseColor).orElse(0xFFFFFF);
                    layers.add(new gg.moonflower.etched.common.item.CoverArt.Layer(this.selectedPattern.get(), dyeColor(this.dyeSlot.getItem())));
                    result = cover.copyWithCount(1);
                    gg.moonflower.etched.common.item.CoverArt.setPattern(result, base, layers);
                }
            }
        }
        this.resultSlot.set(result);
        this.broadcastChanges();
    }

    public static int dyeColor(ItemStack stack) {
        if (!(stack.getItem() instanceof DyeItem dyeItem)) {
            return 0xFFFFFF;
        }
        //? if >=1.21 {
        /*return dyeItem.getDyeColor().getTextureDiffuseColor() & 0xFFFFFF;
        *///?} else {
        float[] c = dyeItem.getDyeColor().getTextureDiffuseColors();
        return (Math.round(c[0] * 255) << 16) | (Math.round(c[1] * 255) << 8) | Math.round(c[2] * 255);
        //?}
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.input) {
            this.setupResultSlot();
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < 100) {
            // ids 0..N select a procedural pattern and switch the printer into pattern mode.
            this.mode.set(MODE_PATTERN);
            this.selectedPattern.set(id);
            this.setupResultSlot();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();
            if (index < 3) {
                if (!this.moveItemStackTo(slotStack, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, pos) -> level.getBlockState(pos).is(Blocks.LOOM) || level.getBlockState(pos).getBlock() instanceof gg.moonflower.etched.common.block.AlbumPrinterBlock, true);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.input));
    }
}
