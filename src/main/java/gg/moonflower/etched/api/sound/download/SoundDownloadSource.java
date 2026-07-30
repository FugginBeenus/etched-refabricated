package gg.moonflower.etched.api.sound.download;

import com.google.gson.JsonParseException;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.api.util.DownloadProgressListener;
import gg.moonflower.etched.core.Etched;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.*;

public interface SoundDownloadSource {

    Component RESOLVING_TRACKS = Component.translatable("record." + Etched.MOD_ID + ".resolvingTracks");

    static Map<String, String> getDownloadHeaders() {
        Map<String, String> map = new HashMap<>();
        WorldVersion version = SharedConstants.getCurrentVersion();
        map.put("X-Minecraft-Version", version.getName());
        map.put("X-Minecraft-Version-ID", version.getId());
        map.put("User-Agent", "Minecraft Java/" + version.getName());
        return map;
    }

    List<URL> resolveUrl(String url, @Nullable DownloadProgressListener progressListener, Proxy proxy) throws IOException;

    List<TrackData> resolveTracks(String url, @Nullable DownloadProgressListener progressListener, Proxy proxy) throws IOException, JsonParseException;

    Optional<String> resolveAlbumCover(String url, @Nullable DownloadProgressListener progressListener, Proxy proxy, ResourceManager resourceManager) throws IOException;

    boolean isValidUrl(String url);

    boolean isTemporary(String url);

    String getApiName();

    default Optional<Component> getBrandText(String url) {
        return Optional.empty();
    }
}
