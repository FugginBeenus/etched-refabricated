package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.common.blockentity.SpeakerBlockEntity;
import gg.moonflower.etched.core.registry.EtchedMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SpeakerMenu extends AbstractContainerMenu {

    private final SpeakerBlockEntity speaker;
    private final DataSlot volume = DataSlot.standalone();

    public SpeakerMenu(int id, Inventory inventory) {
        this(id, inventory, null);
    }

    public SpeakerMenu(int id, Inventory inventory, @Nullable SpeakerBlockEntity speaker) {
        super(EtchedMenus.SPEAKER_MENU, id);
        this.speaker = speaker;
        this.addDataSlot(this.volume);
        if (speaker != null) {
            this.volume.set(Math.round(speaker.getVolume() * 100.0F));
        }
    }

    public int getVolumePercent() {
        return this.volume.get();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.speaker == null || id < 0 || id > 100) {
            return false;
        }
        this.speaker.setVolume(id / 100.0F);
        this.volume.set(id);
        this.broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.speaker == null || (!this.speaker.isRemoved()
                && player.distanceToSqr(this.speaker.getBlockPos().getX() + 0.5, this.speaker.getBlockPos().getY() + 0.5, this.speaker.getBlockPos().getZ() + 0.5) <= 64.0);
    }
}
