package gg.moonflower.etched.api.sound;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.api.sound.source.AudioSource;
import gg.moonflower.etched.api.util.DownloadProgressListener;
import gg.moonflower.etched.common.block.AlbumJukeboxBlock;
import gg.moonflower.etched.common.block.RadioBlock;
import gg.moonflower.etched.common.blockentity.AlbumJukeboxBlockEntity;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.mixin.LevelRendererAccessor;
import gg.moonflower.etched.core.mixin.client.GuiAccessor;
import gg.moonflower.etched.core.registry.EtchedTags;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
//? if >=1.21 {
/*import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;
*///?} else {
import net.minecraft.network.chat.contents.LiteralContents;
//?}
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
//? if <1.21 {
import net.minecraft.world.item.RecordItem;
//?}
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.DoubleSupplier;

/**
 * Tracks entity sounds and all etched playing sounds for the client side.
 *
 * @author Ocelot
 * @since 2.0.0
 */
public class SoundTracker {

    private static final Int2ObjectArrayMap<SoundInstance> ENTITY_PLAYING_SOUNDS = new Int2ObjectArrayMap<>();
    // The extra speaker sounds playing a jukebox's record, beyond the one that drives playback.
    private static final Map<BlockPos, List<SoundInstance>> SPEAKER_COMPANIONS = new HashMap<>();
    private static final Set<String> FAILED_URLS = new HashSet<>();
    // The volume vanilla plays records at, before per-speaker and master scaling.
    private static final float RECORD_VOLUME = 4.0F;
    private static final Component RADIO = Component.translatable("sound_source." + Etched.MOD_ID + ".radio");

    static {
        //MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingOut>addListener(event -> FAILED_URLS.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            FAILED_URLS.clear();
            SPEAKER_COMPANIONS.clear();
            gg.moonflower.etched.client.sound.SharedAudioBuffer.clear();
        });
    }

    private static synchronized void setRecordPlayingNearby(Level level, BlockPos pos, boolean playing) {
        BlockState state = level.getBlockState(pos);
        if (state.is(EtchedTags.AUDIO_PROVIDER) || state.is(Blocks.JUKEBOX)) {
            for (LivingEntity livingEntity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(3.0D))) {
                livingEntity.setRecordPlayingNearby(pos, playing);
            }
        }
    }

    /**
     * Retrieves the sound instance for the specified entity id.
     *
     * @param entity The id of the entity to get a sound for
     * @return The sound for that entity
     */
    @Nullable
    public static SoundInstance getEntitySound(int entity) {
        return ENTITY_PLAYING_SOUNDS.get(entity);
    }

    /**
     * Sets the playing sound for the specified entity.
     *
     * @param entity   The id of the entity to play a sound for
     * @param instance The new sound to play or <code>null</code> to stop
     */
    public static void setEntitySound(int entity, @Nullable SoundInstance instance) {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if (instance == null) {
            SoundInstance old = ENTITY_PLAYING_SOUNDS.remove(entity);
            if (old != null) {
                if (old instanceof StopListeningSound) {
                    ((StopListeningSound) old).stopListening();
                }
                soundManager.stop(old);
            }
        } else {
            ENTITY_PLAYING_SOUNDS.put(entity, instance);
            soundManager.play(instance);
        }
    }

    /**
     * Creates an online sound for the specified entity.
     *
     * @param url                 The url to play
     * @param title               The title of the record
     * @param entity              The entity to play for
     * @param attenuationDistance The attenuation distance of the sound
     * @param stream              Whether to play a stream or regular file
     * @return A new sound instance
     */
    public static AbstractOnlineSoundInstance getEtchedRecord(String url, Component title, Entity entity, int attenuationDistance, boolean stream) {
        return new OnlineRecordSoundInstance(url, entity, attenuationDistance, new MusicDownloadListener(title, entity::getX, entity::getY, entity::getZ) {
            @Override
            public void onSuccess() {
                if (!entity.isAlive() || !ENTITY_PLAYING_SOUNDS.containsKey(entity.getId())) {
                    this.clearComponent();
                } else {
                    if (PlayableRecord.canShowMessage(entity.getX(), entity.getY(), entity.getZ())) {
                        Minecraft.getInstance().gui.setNowPlaying(title);
                    }
                }
            }

            @Override
            public void onFail() {
                Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("record." + Etched.MOD_ID + ".downloadFail", title), true);
                FAILED_URLS.add(url);
            }
        }, stream ? AudioSource.AudioFileType.STREAM : AudioSource.AudioFileType.FILE);
    }

    public static AbstractOnlineSoundInstance getEtchedRecord(String url, Component title, Entity entity, boolean stream) {
        return SoundTracker.getEtchedRecord(url, title, entity, 16, stream);
    }

    /**
     * Creates an online sound for the specified position.
     *
     * @param url                 The url to play
     * @param title               The title of the record
     * @param level               The level to play the record in
     * @param pos                 The position of the record
     * @param attenuationDistance The attenuation distance of the sound
     * @param type                The type of audio to accept
     * @return A new sound instance
     */
    public static AbstractOnlineSoundInstance getEtchedRecord(String url, Component title, ClientLevel level, BlockPos pos, int attenuationDistance, AudioSource.AudioFileType type) {
        BlockState aboveState = level.getBlockState(pos.above());
        boolean muffled = aboveState.is(BlockTags.WOOL);
        boolean hidden = !aboveState.isAir();

        Map<BlockPos, SoundInstance> playingRecords = ((LevelRendererAccessor)Minecraft.getInstance().levelRenderer).getPlayingRecords();
        // Speakers take over: recompute the source each tick so the sound follows speakers as they
        // connect/disconnect (and the player moves between them), without restarting the disc.
        return new OnlineRecordSoundInstance(url, () -> recordSoundSource(level, pos), muffled ? 2.0F : 4.0F, muffled ? attenuationDistance / 2 : attenuationDistance, new MusicDownloadListener(title, () -> pos.getX() + 0.5, () -> pos.getY() + 0.5, () -> pos.getZ() + 0.5) {
            @Override
            public void onSuccess() {
                if (!playingRecords.containsKey(pos)) {
                    this.clearComponent();
                } else {
                    if (!hidden && PlayableRecord.canShowMessage(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                        Minecraft.getInstance().gui.setNowPlaying(title);
                    }
                    setRecordPlayingNearby(level, pos, true);
                }
            }

            @Override
            public void onFail() {
                Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("record." + Etched.MOD_ID + ".downloadFail", title), true);
                FAILED_URLS.add(url);
            }
        }, type);
    }

    public static AbstractOnlineSoundInstance getEtchedRecord(String url, Component title, ClientLevel level, BlockPos pos, AudioSource.AudioFileType type) {
        return getEtchedRecord(url, title, level, pos, 16, type);
    }

    // The speakers a jukebox plays through. A stereo on top adds the wireless speakers paired to it;
    // without one, only speakers touching the jukebox are used. Deterministic order so the "first"
    // speaker is stable across ticks.
    private static java.util.List<BlockPos> connectedSpeakers(ClientLevel level, BlockPos jukeboxPos) {
        gg.moonflower.etched.common.blockentity.StereoBlockEntity stereo =
                gg.moonflower.etched.common.block.StereoBlock.getStereoFor(level, jukeboxPos);
        if (stereo != null) {
            java.util.List<BlockPos> active = stereo.getActiveSpeakers(level);
            // In nearest mode only one sound plays, and recordSoundSource keeps it on the speaker
            // closest to the listener as they move.
            if (stereo.getMode() == gg.moonflower.etched.common.blockentity.StereoBlockEntity.MODE_NEAREST && active.size() > 1) {
                return java.util.List.of(nearestTo(active, Minecraft.getInstance().player));
            }
            return active;
        }

        java.util.List<BlockPos> speakers = new java.util.ArrayList<>();
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            BlockPos p = jukeboxPos.relative(dir);
            if (level.getBlockState(p).getBlock() instanceof gg.moonflower.etched.common.block.SpeakerBlock) {
                speakers.add(p.immutable());
            }
        }
        return speakers;
    }

    // The sound position for a jukebox record: the first connected speaker (so a connected speaker
    // takes over the audio), or the jukebox itself when none is connected. Recomputed each tick by
    // the caller's supplier, so it follows speakers being placed/broken. True multi-point ("from every
    // speaker at once") needs a shared decoded buffer + synchronized start; that's a later rework.
    private static net.minecraft.world.phys.Vec3 recordSoundSource(ClientLevel level, BlockPos pos) {
        java.util.List<BlockPos> speakers = connectedSpeakers(level, pos);
        BlockPos src = speakers.isEmpty() ? pos : speakers.get(0);
        return new net.minecraft.world.phys.Vec3(src.getX() + 0.5, src.getY() + 0.5, src.getZ() + 0.5);
    }

    private static void playRecord(BlockPos pos, SoundInstance sound) {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        Map<BlockPos, SoundInstance> playingRecords =((LevelRendererAccessor)Minecraft.getInstance().levelRenderer).getPlayingRecords();
        playingRecords.put(pos, sound);
        soundManager.play(sound);
    }

    private static void playNextRecord(ClientLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof AlbumJukeboxBlockEntity jukebox)) {
            return;
        }

        jukebox.next();
        playAlbum((AlbumJukeboxBlockEntity) blockEntity, blockEntity.getBlockState(), level, pos, true);
    }

    public static void playBlockRecord(BlockPos pos, TrackData[] tracks, int track) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        if (track >= tracks.length) {
            setRecordPlayingNearby(level, pos, false);
            return;
        }

        TrackData trackData = tracks[track];
        String url = trackData.url();
        if (!TrackData.isValidURL(url) || FAILED_URLS.contains(url)) {
            playBlockRecord(pos, tracks, track + 1);
            return;
        }
        playBlockAudio(level, pos, url, trackData.getDisplayName(), () -> Minecraft.getInstance().tell(() -> {
            if (!(((LevelRendererAccessor)Minecraft.getInstance().levelRenderer).getPlayingRecords().containsKey(pos))) {
                return;
            }
            playBlockRecord(pos, tracks, track + 1);
        }));
    }

    /**
     * Starts a downloaded record for a block, playing it from every connected speaker when more than
     * one is attached. The speakers all read from one decoded buffer and are handed their streams by
     * the same future, so they start together and stay in sync.
     *
     * @param level      The level the block is in
     * @param pos        The block playing the record
     * @param url        The track to play
     * @param title      The name to announce
     * @param onFinished Run when the track ends, to advance to the next one
     */
    private static void playBlockAudio(ClientLevel level, BlockPos pos, String url, Component title, SoundStopListener onFinished) {
        stopCompanions(pos);

        List<BlockPos> speakers = connectedSpeakers(level, pos);
        // Sounds bundled with the game are loaded from resource packs rather than downloaded, so they
        // can't be shared through the buffer; those keep playing from a single speaker.
        if (speakers.size() <= 1 || TrackData.isLocalSound(url)) {
            playRecord(pos, StopListeningSound.create(getEtchedRecord(url, title, level, pos, AudioSource.AudioFileType.FILE), onFinished));
            return;
        }

        Map<BlockPos, SoundInstance> playingRecords = ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getPlayingRecords();
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();

        // The first speaker's sound is the one tracked for the block: it advances playback and stops
        // when the disc is removed. It can't simply be stopped when its speaker is broken (that would
        // skip to the next track), so instead it goes silent, and moves back to the jukebox once no
        // speakers are left at all. Register it before the rest so they see the record as playing.
        BlockPos primaryPos = speakers.get(0);
        SpeakerSoundInstance primary = new SpeakerSoundInstance(url,
                () -> isSpeaker(level, primaryPos) || !connectedSpeakers(level, pos).isEmpty()
                        ? net.minecraft.world.phys.Vec3.atCenterOf(primaryPos)
                        : net.minecraft.world.phys.Vec3.atCenterOf(pos),
                RECORD_VOLUME, speakerAttenuation(speakerVolume(level, pos, primaryPos)), AudioSource.AudioFileType.FILE);
        primary.withVolume(() -> {
            if (isSpeaker(level, primaryPos)) {
                return speakerGain(speakerVolume(level, pos, primaryPos));
            }
            // Silent while other speakers carry the record; audible again from the jukebox once they
            // are all gone.
            return connectedSpeakers(level, pos).isEmpty() ? RECORD_VOLUME : 0.0;
        });
        playRecord(pos, StopListeningSound.create(primary, onFinished));

        List<SoundInstance> companions = new ArrayList<>();
        for (int i = 1; i < speakers.size(); i++) {
            BlockPos speakerPos = speakers.get(i);
            SpeakerSoundInstance companion = speakerSound(url, speakerPos, speakerAttenuation(speakerVolume(level, pos, speakerPos)));
            companion.withVolume(() -> speakerGain(speakerVolume(level, pos, speakerPos)));
            // Stop with the record, or as soon as this speaker is broken, so no audio is left playing
            // from a block that is no longer there.
            companion.stopWhen(() -> !playingRecords.containsKey(pos) || !isSpeaker(level, speakerPos));
            soundManager.play(companion);
            companions.add(companion);
        }
        SPEAKER_COMPANIONS.put(pos.immutable(), companions);

        // The listener that would normally announce the track is bypassed by the shared buffer, so
        // report it here once the audio is actually ready.
        gg.moonflower.etched.client.sound.SharedAudioBuffer.get(url, AudioSource.AudioFileType.FILE).thenRunAsync(() -> {
            if (!playingRecords.containsKey(pos)) {
                return;
            }
            if (PlayableRecord.canShowMessage(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                Minecraft.getInstance().gui.setNowPlaying(title);
            }
            setRecordPlayingNearby(level, pos, true);
        }, Minecraft.getInstance()).exceptionally(e -> null);
    }

    private static BlockPos nearestTo(java.util.List<BlockPos> positions, @Nullable Entity listener) {
        if (listener == null) {
            return positions.get(0);
        }
        BlockPos best = positions.get(0);
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : positions) {
            double dist = pos.distToCenterSqr(listener.getX(), listener.getY(), listener.getZ());
            if (dist < bestDist) {
                bestDist = dist;
                best = pos;
            }
        }
        return best;
    }

    private static SpeakerSoundInstance speakerSound(String url, BlockPos speaker, int attenuation) {
        return new SpeakerSoundInstance(url, speaker.getX() + 0.5, speaker.getY() + 0.5, speaker.getZ() + 0.5, RECORD_VOLUME, attenuation, AudioSource.AudioFileType.FILE);
    }

    private static boolean isSpeaker(ClientLevel level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof gg.moonflower.etched.common.block.SpeakerBlock;
    }

    // A speaker's own volume, scaled by the master volume of the stereo driving the jukebox (0-1).
    private static double speakerVolume(ClientLevel level, BlockPos jukeboxPos, BlockPos speakerPos) {
        return gg.moonflower.etched.common.blockentity.SpeakerBlockEntity.volumeAt(level, speakerPos)
                * gg.moonflower.etched.common.blockentity.StereoBlockEntity.masterVolumeAt(level, jukeboxPos);
    }

    // The gain a speaker plays at for a given 0-1 volume. Hearing is roughly logarithmic, so a raw
    // linear volume makes the slider feel dead until the very bottom; squaring spreads the audible
    // change across the whole travel.
    private static double speakerGain(double volume) {
        // Map straight into 0..1 (squared for a perceptual taper). Multiplying by RECORD_VOLUME (4.0)
        // saturated the game's near-source volume clamp, so the top half of the slider did nothing.
        return volume * volume;
    }

    // How far a speaker carries at a given 0-1 volume: quieter speakers are heard less far. Floored so
    // a speaker is never silent at its own block. Baked when the sound starts (a mid-track slider move
    // changes loudness immediately but its range only on the next track).
    private static int speakerAttenuation(double volume) {
        return Math.max(2, (int) Math.round(16 * volume));
    }

    private static void stopCompanions(BlockPos pos) {
        List<SoundInstance> companions = SPEAKER_COMPANIONS.remove(pos);
        if (companions != null) {
            SoundManager soundManager = Minecraft.getInstance().getSoundManager();
            companions.forEach(soundManager::stop);
        }
    }

    // ---- vanilla-disc speaker routing (driven from LevelRendererMixin, which owns the version-specific
    // sound construction) ----

    /**
     * @return The speakers connected to a jukebox, so the vanilla-disc hook can route through them.
     */
    public static List<BlockPos> getConnectedSpeakers(ClientLevel level, BlockPos jukeboxPos) {
        return connectedSpeakers(level, jukeboxPos);
    }

    /**
     * Plays and tracks the extra speaker sounds for a vanilla disc. The caller builds the instances
     * (their construction differs by version); this plays them and keys them to the jukebox so they
     * stop with it.
     */
    public static void playSpeakerCompanions(BlockPos jukeboxPos, List<SoundInstance> companions) {
        stopCompanions(jukeboxPos);
        if (companions.isEmpty()) {
            return;
        }
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        companions.forEach(soundManager::play);
        SPEAKER_COMPANIONS.put(jukeboxPos.immutable(), companions);
    }

    public static void stopSpeakers(BlockPos jukeboxPos) {
        stopCompanions(jukeboxPos);
    }

    /**
     * Plays a record stack for an entity.
     *
     * @param record              The record to play
     * @param entityId            The id of the entity to play the record at
     * @param track               The track to play
     * @param attenuationDistance The attenuation distance of the sound
     * @param loop                Whether to loop
     */
    public static void playEntityRecord(ItemStack record, int entityId, int track, int attenuationDistance, boolean loop) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        Entity entity = level.getEntity(entityId);
        if (entity == null) {
            return;
        }

        Optional<? extends SoundInstance> sound = ((PlayableRecord) record.getItem()).createEntitySound(record, entity, track, attenuationDistance);
        if (sound.isEmpty()) {
            if (loop && track != 0) {
                playEntityRecord(record, entityId, 0, attenuationDistance, true);
            }
            return;
        }

        SoundInstance entitySound = ENTITY_PLAYING_SOUNDS.remove(entity.getId());
        if (entitySound != null) {
            if (entitySound instanceof StopListeningSound) {
                ((StopListeningSound) entitySound).stopListening();
            }
            Minecraft.getInstance().getSoundManager().stop(entitySound);
        }

        entitySound = StopListeningSound.create(sound.get(), () -> Minecraft.getInstance().tell(() -> {
            ENTITY_PLAYING_SOUNDS.remove(entityId);
            playEntityRecord(record, entityId, track + 1, attenuationDistance, loop);
        }));

        ENTITY_PLAYING_SOUNDS.put(entityId, entitySound);
        Minecraft.getInstance().getSoundManager().play(entitySound);
    }

    public static void playEntityRecord(ItemStack record, int entityId, int track, boolean loop) {
        SoundTracker.playEntityRecord(record, entityId, track, 16, loop);
    }

    /**
     * Plays a record stack for an entity with a boombox.
     *
     * @param entityId The id of the entity to play the record at
     * @param record   The record to play
     */
    public static void playBoombox(int entityId, ItemStack record) {
        setEntitySound(entityId, null);
        if (!record.isEmpty()) {
            playEntityRecord(record, entityId, 0, 8, true);
        }
    }

    /**
     * Plays the records on an album jukebox in order.
     *
     * @param url   The URL of the stream
     * @param state The block state of the radio
     * @param level The level to play records in
     * @param pos   The position of the jukebox
     */
    public static void playRadio(@Nullable String url, BlockState state, ClientLevel level, BlockPos pos) {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        Map<BlockPos, SoundInstance> playingRecords = ((LevelRendererAccessor)Minecraft.getInstance().levelRenderer).getPlayingRecords();

        SoundInstance soundInstance = playingRecords.get(pos);
        if (soundInstance != null) {
            if (soundInstance instanceof StopListeningSound) {
                ((StopListeningSound) soundInstance).stopListening();
            }
            soundManager.stop(soundInstance);
            playingRecords.remove(pos);
            setRecordPlayingNearby(level, pos, false);
        }

        if (FAILED_URLS.contains(url)) {
            return;
        }
        if (!state.hasProperty(RadioBlock.POWERED) || state.getValue(RadioBlock.POWERED)) { // Something must already be playing since it would otherwise be -1 and a change would occur
            return;
        }

        if (TrackData.isValidURL(url)) {
            AbstractOnlineSoundInstance record = getEtchedRecord(url, RADIO, level, pos, 8, AudioSource.AudioFileType.BOTH);
            record.setLoop(true); // If the sound is a file, then just continue looping that specific track
            playRecord(pos, record); // Get the new block state
        }
    }

    /**
     * Plays the records on an album jukebox in order.
     *
     * @param jukebox The jukebox to play records
     * @param level   The level to play records in
     * @param pos     The position of the jukebox
     * @param force   Whether to force the jukebox to play
     */
    public static void playAlbum(AlbumJukeboxBlockEntity jukebox, BlockState state, ClientLevel level, BlockPos pos, boolean force) {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        Map<BlockPos, SoundInstance> playingRecords = ((LevelRendererAccessor)Minecraft.getInstance().levelRenderer).getPlayingRecords();

        if (!state.hasProperty(AlbumJukeboxBlock.POWERED) || !state.getValue(AlbumJukeboxBlock.POWERED) && !force && !jukebox.recalculatePlayingIndex(false)) {// Something must already be playing since it would otherwise be -1 and a change would occur
            return;
        }

        SoundInstance soundInstance = playingRecords.get(pos);
        if (soundInstance != null) {
            if (soundInstance instanceof StopListeningSound) {
                ((StopListeningSound) soundInstance).stopListening();
            }
            soundManager.stop(soundInstance);
            playingRecords.remove(pos);
            setRecordPlayingNearby(level, pos, false);
        }

        if (state.getValue(AlbumJukeboxBlock.POWERED)) {
            jukebox.stopPlaying();
        }

        if (jukebox.getPlayingIndex() < 0) {// Nothing can be played inside the jukebox
            return;
        }

        ItemStack disc = jukebox.getItem(jukebox.getPlayingIndex());
        String playUrl = null;
        Component playTitle = null;
        //? if >=1.21 {
        /*if (disc.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
            Optional<net.minecraft.core.Holder<net.minecraft.world.item.JukeboxSong>> etched$song = net.minecraft.world.item.JukeboxSong.fromStack(level.registryAccess(), disc);
            if (etched$song.isPresent()) {
                playUrl = etched$song.get().value().soundEvent().value().getLocation().toString();
                playTitle = disc.getHoverName();
            }
        } else if (disc.getItem() instanceof PlayableRecord) {
        *///?} else {
        if (disc.getItem() instanceof RecordItem) {
            playUrl = ((RecordItem) disc.getItem()).getSound().getLocation().toString();
            playTitle = ((RecordItem) disc.getItem()).getDisplayName();
        } else if (disc.getItem() instanceof PlayableRecord) {
        //?}
            Optional<TrackData[]> optional = PlayableRecord.getStackMusic(disc);
            if (optional.isPresent() && optional.get().length > 0) {
                TrackData[] tracks = optional.get();
                TrackData track = jukebox.getTrack() < 0 || jukebox.getTrack() >= tracks.length ? tracks[0] : tracks[jukebox.getTrack()];
                String url = track.url();
                if (TrackData.isValidURL(url) && !FAILED_URLS.contains(url)) {
                    playUrl = url;
                    playTitle = track.getDisplayName();
                }
            }
        }

        if (playUrl == null) {
            return;
        }

        playBlockAudio(level, pos, playUrl, playTitle, () -> Minecraft.getInstance().tell(() -> playNextRecord(level, pos)));
        setRecordPlayingNearby(level, pos, true);
    }

    private static class DownloadTextComponent implements Component {

        private ComponentContents contents;
        private FormattedCharSequence visualOrderText;
        private Language decomposedWith;

        public DownloadTextComponent() {
            //? if >=1.21 {
            /*this.contents = net.minecraft.network.chat.contents.PlainTextContents.EMPTY;
            *///?} else {
            this.contents = ComponentContents.EMPTY;
            //?}
            this.visualOrderText = FormattedCharSequence.EMPTY;
            this.decomposedWith = null;
        }

        @Override
        public ComponentContents getContents() {
            return this.contents;
        }

        @Override
        public List<Component> getSiblings() {
            return Collections.emptyList();
        }

        @Override
        public Style getStyle() {
            return Style.EMPTY;
        }

        @Override
        public FormattedCharSequence getVisualOrderText() {
            Language language = Language.getInstance();
            if (this.decomposedWith != language) {
                this.visualOrderText = language.getVisualOrder(this);
                this.decomposedWith = language;
            }

            return this.visualOrderText;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            DownloadTextComponent that = (DownloadTextComponent) o;
            return this.contents.equals(that.contents);
        }

        @Override
        public int hashCode() {
            return this.contents.hashCode();
        }

        @Override
        public String toString() {
            return this.contents.toString();
        }

        public void setText(String text) {
            this.contents = new LiteralContents(text);
            this.decomposedWith = null;
        }
    }

    private static abstract class MusicDownloadListener implements DownloadProgressListener {

        private final Component title;
        private final DoubleSupplier x;
        private final DoubleSupplier y;
        private final DoubleSupplier z;
        private final BlockPos.MutableBlockPos pos;
        private float size;
        private Component requesting;
        private DownloadTextComponent component;

        protected MusicDownloadListener(Component title, DoubleSupplier x, DoubleSupplier y, DoubleSupplier z) {
            this.title = title;
            this.x = x;
            this.y = y;
            this.z = z;
            this.pos = new BlockPos.MutableBlockPos();
        }

        private BlockPos.MutableBlockPos getPos() {
            return this.pos.set(this.x.getAsDouble(), this.y.getAsDouble(), this.z.getAsDouble());
        }

        private void setComponent(Component text) {
            if (this.component == null && (Minecraft.getInstance().level == null || !Minecraft.getInstance().level.getBlockState(this.getPos().move(Direction.UP)).isAir() || !PlayableRecord.canShowMessage(this.x.getAsDouble(), this.y.getAsDouble(), this.z.getAsDouble()))) {
                return;
            }

            if (this.component == null) {
                this.component = new DownloadTextComponent();
                Minecraft.getInstance().gui.setOverlayMessage(this.component, true);
                ((GuiAccessor)Minecraft.getInstance().gui).setOverlayMessageTime(Short.MAX_VALUE);
            }
            this.component.setText(text.getString());
        }

        protected void clearComponent() {
            if (Minecraft.getInstance().gui.overlayMessageString == this.component) {
                ((GuiAccessor)Minecraft.getInstance().gui).setOverlayMessageTime(60);
                this.component = null;
            }
        }

        @Override
        public void progressStartRequest(Component component) {
            this.requesting = component;
            this.setComponent(component);
        }

        @Override
        public void progressStartDownload(float size) {
            this.size = size;
            this.requesting = null;
            this.progressStagePercentage(0);
        }

        @Override
        public void progressStagePercentage(int percentage) {
            if (this.requesting != null) {
                this.setComponent(this.requesting.copy().append(" " + percentage + "%"));
            } else if (this.size != 0) {
                this.setComponent(Component.translatable("record." + Etched.MOD_ID + ".downloadProgress", String.format(Locale.ROOT, "%.2f", percentage / 100.0F * this.size), String.format(Locale.ROOT, "%.2f", this.size), this.title));
            }
        }

        @Override
        public void progressStartLoading() {
            this.requesting = null;
            this.setComponent(Component.translatable("record." + Etched.MOD_ID + ".loading", this.title));
        }

        @Override
        public void onFail() {
            Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("record." + Etched.MOD_ID + ".downloadFail", this.title), true);
        }
    }
}
