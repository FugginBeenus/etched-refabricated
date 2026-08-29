package gg.moonflower.etched.common.menu;

import gg.moonflower.etched.core.registry.EtchedBlocks;
import gg.moonflower.etched.core.registry.EtchedMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class RadioMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    // Workaround for thread concurrency issues
    private final Consumer<String> urlConsumer;

    public RadioMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL, url -> {
        });
    }

    public RadioMenu(int id, Inventory inventory, ContainerLevelAccess access, Consumer<String> containerLevelAccess) {
        super(EtchedMenus.RADIO_MENU, id);
        this.access = access;
        this.urlConsumer = containerLevelAccess;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, EtchedBlocks.RADIO.get());
    }

    public void setUrl(String url) {
        // Typing a link by hand is a manual override, so the radio leaves whatever station it was on.
        // Otherwise the station would keep pushing its own track over the top of it.
        this.tuneTo(null);
        this.urlConsumer.accept(url);
    }

    /**
     * Tunes this radio to a station, or back to its own link when given null. Leaving a station first
     * matters: otherwise the old one keeps pushing tracks at a radio that has moved on.
     */
    public void tuneTo(@org.jetbrains.annotations.Nullable net.minecraft.core.BlockPos stationPos) {
        this.access.execute((level, radioPos) -> {
            if (!(level.getBlockEntity(radioPos) instanceof gg.moonflower.etched.common.blockentity.RadioBlockEntity radio)) {
                return;
            }

            net.minecraft.core.BlockPos previous = radio.getStationPos();
            if (previous != null && level.getBlockEntity(previous) instanceof gg.moonflower.etched.common.blockentity.RadioStationBlockEntity old) {
                old.untune(radioPos);
            }

            if (stationPos == null) {
                radio.setStation(null);
                return;
            }
            if (level.getBlockEntity(stationPos) instanceof gg.moonflower.etched.common.blockentity.RadioStationBlockEntity station) {
                radio.setStation(stationPos);
                station.tune(radioPos);
            }
        });
    }
}