# Etched Refabricated

A Fabric continuation of [Etched](https://github.com/jacksonhardaway/etched), the music mod that lets you turn any audio link into a playable music disc.

Etch a URL (direct audio files, SoundCloud, Bandcamp, and more) onto a blank disc at the Etching Table, give it a custom label and album cover, and play it anywhere vanilla music discs work. Plus a few places they don't. The original mod is barely maintained on modern Fabric, so this fork picks it back up, fixes long-standing bugs, and adds new features.

## Features

- Etch online audio onto custom music discs at the Etching Table
- Streams OGG, WAV, MP3, and now AAC / `.m4a`
- Boombox you can carry and play in-hand
- Album Jukebox for multi-track playback, plus a Radio for continuous streams
- Custom disc labels and album cover art
- Minecart Jukebox, note block support, and a wandering Bard villager

## What's different in this fork

- Fixed the bug where non-OGG audio (many MP3 and streamed links) failed to play. Format detection no longer relies on a tiny mark/reset buffer, so larger streams load reliably.
- Added AAC / `.m4a` playback.

## Supported versions

- Minecraft 1.20.1, Fabric

Support for newer Minecraft versions is planned.

## Building

Requires JDK 21 (to run Gradle; Minecraft 1.20.1 itself still compiles to and runs on Java 17).

The project uses [Stonecutter](https://stonecutter.kikugie.dev/) to build one source tree across multiple Minecraft versions. To build the active version:

```
./gradlew build
```

To run the development client for a specific version:

```
./gradlew :1.20.1:runClient
```

Built jars land in `versions/<version>/build/libs/`.

## License and credits

Etched Refabricated is a modified fork of Etched and is licensed under the same terms:

- Code is licensed under GPL-3.0.
- The art, textures, and sounds in `src/main/resources/` are All Rights Reserved by Moonflower Studio and are not covered by the GPL.

Original mod by Moonflower Studio (Jackson, Ocelot, Farcr, Echolite, AstraZoey). Fabric port lineage by Aikoyori and VanderCat. See [LICENSE](LICENSE) for full terms.
