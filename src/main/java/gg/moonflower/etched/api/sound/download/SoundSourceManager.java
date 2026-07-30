package gg.moonflower.etched.api.sound.download;

import gg.moonflower.etched.api.record.AlbumCover;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.api.sound.source.AudioSource;
import gg.moonflower.etched.api.sound.source.RawAudioSource;
import gg.moonflower.etched.api.sound.source.StreamingAudioSource;
import gg.moonflower.etched.api.util.DownloadProgressListener;
import gg.moonflower.etched.client.AlbumCoverCache;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.HttpUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class SoundSourceManager {

    private static final Set<SoundDownloadSource> SOURCES = new HashSet<>();
    private static final Logger LOGGER = LogManager.getLogger();

    private SoundSourceManager() {
    }

    public static synchronized void registerSource(SoundDownloadSource source) {
        SOURCES.add(source);
    }

    public static CompletableFuture<AudioSource> getAudioSource(String url, @Nullable DownloadProgressListener listener, Proxy proxy, AudioSource.AudioFileType type) throws MalformedURLException {
        Optional<SoundDownloadSource> sourceOptional = SOURCES.stream().filter(s -> s.isValidUrl(url)).findFirst();
        CompletableFuture<List<URL>> urlFuture = sourceOptional.isPresent() ? CompletableFuture.supplyAsync(() -> {
            SoundDownloadSource source = sourceOptional.get();
            try {
                return source.resolveUrl(url, listener, proxy);
            } catch (Exception e) {
                throw new CompletionException("Failed to connect to " + source.getApiName() + " API", e);
            }
        }, gg.moonflower.etched.core.Etched.downloadExecutor()) : CompletableFuture.completedFuture(Collections.singletonList(new URL(url)));

        return urlFuture.thenApplyAsync(urls -> {
            try {
                if (urls.isEmpty()) {
                    throw new IOException("No audio data was found at the source!");
                }
                if (urls.size() == 1) {
                    return new RawAudioSource(urls.get(0), listener, sourceOptional.map(s -> s.isTemporary(url)).orElse(false), type);
                }
                return new StreamingAudioSource(urls.toArray(URL[]::new), listener, sourceOptional.map(s -> s.isTemporary(url)).orElse(false), type);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, gg.moonflower.etched.core.Etched.downloadExecutor());
    }

    public static CompletableFuture<TrackData[]> resolveTracks(String url, @Nullable DownloadProgressListener listener, Proxy proxy) throws IOException {
        SoundDownloadSource source = SOURCES.stream().filter(s -> s.isValidUrl(url)).findFirst().orElseThrow(() -> new IOException("Unknown source for: " + url));
        return CompletableFuture.supplyAsync(() -> {
            try {
                return source.resolveTracks(url, listener, proxy).toArray(TrackData[]::new);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, gg.moonflower.etched.core.Etched.downloadExecutor());
    }

    public static CompletableFuture<AlbumCover> resolveAlbumCover(String url, @Nullable DownloadProgressListener listener, Proxy proxy, ResourceManager resourceManager) {
        return CompletableFuture.supplyAsync(() -> SOURCES.stream().filter(s -> s.isValidUrl(url)).findFirst().flatMap(source -> {
            try {
                return source.resolveAlbumCover(url, listener, proxy, resourceManager);
            } catch (Exception e) {
                LOGGER.error("Failed to connect to " + source.getApiName() + " API", e);
                return Optional.empty();
            }
        }), gg.moonflower.etched.core.Etched.downloadExecutor()).thenCompose(coverUrl -> coverUrl.map(AlbumCoverCache::requestResource).orElseGet(() -> CompletableFuture.completedFuture(AlbumCover.EMPTY)));
    }

    public static Optional<Component> getBrandText(String url) {
        return SOURCES.stream().filter(source -> source.isValidUrl(url)).findFirst().flatMap(s -> s.getBrandText(url));
    }

    public static boolean isValidUrl(String url) {
        return SOURCES.stream().anyMatch(s -> s.isValidUrl(url));
    }
}
