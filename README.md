# Etched Refabricated

A Fabric continuation of [Etched](https://github.com/jacksonhardaway/etched), the music mod that lets you turn any audio link into a playable music disc.

Etch a URL (direct audio files, SoundCloud, Bandcamp, and more) onto a blank disc at the Etching Table, give it a custom label and album cover, and play it anywhere vanilla music discs work. Plus a few places they don't. The original mod is barely maintained on modern Fabric, so this fork picks it back up, fixes long-standing bugs, and adds new features.

## Features

- Etch online audio onto custom music discs at the Etching Table
- Streams OGG, WAV, FLAC, MP3, and AAC / `.m4a`
- Speakers that take over a jukebox's audio, wired by placing them alongside it or wireless by pairing them to a Stereo
- Per speaker volume and a master volume, with quieter speakers carrying a shorter distance
- Boombox you can carry and play in-hand
- Album Jukebox for multi-track playback, plus a Radio for continuous streams
- Custom disc labels and album cover art, including your own uploaded images
- Minecart Jukebox, note block support, and a Bard villager who works at an Etching Table

## Speakers and the Stereo

Place a Speaker next to a jukebox and the record plays from the speaker instead of the block. Put a Stereo on top of a jukebox to drive speakers wirelessly: click Link, then click the speakers you want to pair. Click the Stereo again, or sneak-click a speaker, to finish.

The Stereo takes two kinds of upgrade. Preamps raise how many speakers it can drive, transmitters extend how far it reaches. Both show up on the unit drawn in the Stereo screen as you install them, so you can see what the machine is actually running.

Vanilla music discs play through speakers too, not only etched ones.

## Requirements

- Fabric Loader and Fabric API
- [YetAnotherConfigLib](https://modrinth.com/mod/yacl)

## Supported versions

- Minecraft 1.20.1 (Fabric)
- Minecraft 1.21.1 (Fabric)

## What's different in this fork

- Fixed the bug where non-OGG audio (many MP3 and streamed links) failed to play. Format detection no longer relies on a tiny mark/reset buffer, so larger streams load reliably.
- Added AAC / `.m4a` and FLAC playback.
- Added the Speaker and Stereo system, with wireless pairing, volume control, and hearing distance that follows volume.
- Vanilla discs now route through speakers on both supported Minecraft versions.
- Rebuilt the Speaker, Stereo, and Album Printer screens.
- Brought back the Bard villager, who works at an Etching Table and lives in bard houses that generate in plains, desert, savanna, snowy, and taiga villages. Houses are added to villages at runtime rather than through a datapack, so villages keep whatever other mods have added to them, and a config option turns them off.
- Fixed volume sliders that could be clicked but not dragged, speaker link mode that could not be exited, and a paired speaker count that only ever climbed.

## Known gaps

Music discs from this mod cannot be used in Sophisticated Backpacks' Jukebox Upgrade. Its Fabric port predates the disc handler API that would make this possible.

## Building

Requires JDK 21 to run Gradle. Minecraft 1.20.1 itself still compiles to and runs on Java 17.

The project uses [Stonecutter](https://stonecutter.kikugie.dev/) to build one source tree across multiple Minecraft versions. Building from the root produces jars for every supported version:

```
./gradlew build
```

To run the development client for a specific version:

```
./gradlew :1.20.1:runClient
./gradlew :1.21.1:runClient
```

Built jars land in `versions/<version>/build/libs/`.

Source files use Stonecutter comment directives (`//? if >=1.21 {`) to hold both versions in one tree. The commented-out branch is live code for the other version, so leave anything marked `//?` alone.

## License and credits

Etched Refabricated is a modified fork of Etched and is licensed under the same terms:

- Code is licensed under GPL-3.0.
- The art, textures, and sounds in `src/main/resources/` are All Rights Reserved by Moonflower Studio and are not covered by the GPL.

Original mod by Moonflower Studio (Jackson, Ocelot, Farcr, Echolite, AstraZoey). Fabric port lineage by Aikoyori and VanderCat. See [LICENSE](LICENSE) for full terms.
