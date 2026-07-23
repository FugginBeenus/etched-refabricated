package gg.moonflower.etched.core.mixin.jukebox;

// 1.21-only: JukeboxSongPlayer did not exist before 1.21, so the whole mixin is version-guarded.
//? if >=1.21 {
/*import net.minecraft.world.item.JukeboxSongPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(JukeboxSongPlayer.class)
public interface JukeboxSongPlayerAccessor {

    @Accessor("ticksSinceSongStarted")
    void setTicksSinceSongStarted(long ticks);
}
*///?}
