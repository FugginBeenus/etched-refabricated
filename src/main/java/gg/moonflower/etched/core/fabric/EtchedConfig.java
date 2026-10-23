package gg.moonflower.etched.core.fabric;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import gg.moonflower.etched.core.Etched;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class EtchedConfig {
    public static ConfigClassHandler<EtchedConfig> HANDLER = ConfigClassHandler.createBuilder(EtchedConfig.class)
            .id(gg.moonflower.etched.api.util.EtchedResourceLocation.of(Etched.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("etched.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting) // not needed, pretty print by default
                    .setJson5(true)
                    .build())
            .build();

    public static void save(){
        EtchedConfig.HANDLER.save();
    }
    public static void load(){
        EtchedConfig.HANDLER.load();
    }

    @SerialEntry(comment = "Disables right clicking music discs into boomboxes and allows the menu to be used by shift right-clicking.")
    public boolean useBoomboxMenu = false;

    @SerialEntry(comment = "Disables right clicking music discs into album covers and allows the menu to be used by shift right-clicking")
    public boolean useAlbumCoverMenu = false;

    @SerialEntry(comment = "Displays note particles appear above jukeboxes while a record is playing.")
    public boolean showNotes = true;

    @SerialEntry(comment = "Always plays tracks in stereo even when in-world.")
    public boolean forceStereo = false;

    @SerialEntry(comment = "Adds bard stalls to villages as they generate. Turn off if a village overhaul mod should own village layout entirely.")
    public boolean addBardHousesToVillages = true;

    @SerialEntry(comment = "Domains that discs and radios are allowed to stream from, such as \"files.catbox.moe\". Subdomains of a listed domain are allowed too. Leave empty to allow any domain. The server's own list is what counts, so a client cannot talk its way past it.")
    public List<String> allowedDomains = new ArrayList<>();

    /**
     * Whether a URL may be streamed, according to the allowed domain list. An empty list allows
     * anything, which is the default and what a single player world wants.
     *
     * <p>Checked against the host only. A listed domain also covers its subdomains, but a host is never
     * matched on a bare suffix, so listing {@code bandcamp.com} does not let {@code notbandcamp.com}
     * through.
     */
    public static boolean isDomainAllowed(String url) {
        List<String> allowed = HANDLER.instance().allowedDomains;
        if (allowed == null || allowed.isEmpty()) {
            return true;
        }
        // A local sound is a sound event already in the game, not something streamed off a site, so the
        // domain list has no say over it.
        if (gg.moonflower.etched.api.record.TrackData.isLocalSound(url)) {
            return true;
        }

        String host;
        try {
            host = new URI(url).getHost();
        } catch (Exception e) {
            return false;
        }
        if (host == null) {
            return false;
        }

        host = host.toLowerCase(Locale.ROOT);
        for (String entry : allowed) {
            String domain = entry.trim().toLowerCase(Locale.ROOT);
            if (!domain.isEmpty() && (host.equals(domain) || host.endsWith("." + domain))) {
                return true;
            }
        }
        return false;
    }

    public Screen getConfigScreen(Screen screen){
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Etched"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Server"))
                        .group(OptionGroup.createBuilder()
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Use Boombox Menu"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.literal("Disables right clicking music discs into boomboxes and allows the menu to be used by shift right-clicking."))
                                                .build())

                                        .binding(false,() -> this.useBoomboxMenu,aBoolean -> {this.useBoomboxMenu = aBoolean;})
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Use Album Cover"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.literal("Disables right clicking music discs into album covers and allows the menu to be used by shift right-clicking"))
                                                .build())
                                        .binding(false,() -> this.useAlbumCoverMenu,aBoolean -> {this.useAlbumCoverMenu = aBoolean;})
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Bard Stalls In Villages"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.literal("Adds bard stalls to villages as they generate. Turn off if a village overhaul mod should own village layout entirely. Takes effect next time the world loads."))
                                                .build())
                                        .binding(true,() -> this.addBardHousesToVillages,aBoolean -> {this.addBardHousesToVillages = aBoolean;})
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(ListOption.<String>createBuilder()
                                .name(Component.literal("Allowed Domains"))
                                .description(OptionDescription.createBuilder()
                                        .text(Component.literal("Domains discs and radios may stream from, such as \"files.catbox.moe\". Subdomains of a listed domain are allowed. Leave the list empty to allow any domain."))
                                        .build())
                                .binding(new ArrayList<>(), () -> this.allowedDomains, value -> this.allowedDomains = new ArrayList<>(value))
                                .controller(StringControllerBuilder::create)
                                .initial("")
                                .build())
                        .build()
                )
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Client"))
                        .group(OptionGroup.createBuilder()
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Force Stereo"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.literal("Always plays tracks in stereo even when in-world."))
                                                .build())
                                        .binding(true,() -> this.forceStereo,aBoolean -> {this.forceStereo = aBoolean;})
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Show Notes"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.literal("Displays note particles appear above jukeboxes while a record is playing."))
                                                .build())
                                        .binding(false,() -> this.showNotes,aBoolean -> {this.showNotes = aBoolean;})
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build()
                )
                .save(EtchedConfig::save)
                .build().generateScreen(screen);
    };

}
