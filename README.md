# Etched Refabricated

An unofficial Fabric continuation of [Etched](https://github.com/jacksonhardaway/etched), the music mod that turns any audio link into a playable music disc.

**This is an unofficial fork.** It is not made, endorsed or supported by Moonflower Studio, the team behind Etched. Please report problems with it [here](https://github.com/FugginBeenus/etched-refabricated/issues), not to them.

Etch a link onto a blank disc at the Etching Table, give it a label and an album cover, and play it anywhere vanilla discs play, plus a few places they don't. The original mod is barely maintained on modern Fabric, so this fork picks it back up, fixes long-standing bugs, adds new features, and ships a full set of new art.

## Features

- Etch online audio onto custom music discs at the Etching Table
- Streams OGG, WAV, FLAC, MP3 and AAC (`.m4a`)
- Speakers that take over a jukebox's sound, wired by placing them beside it or wireless by pairing them to a Stereo
- Per speaker volume and a master volume, with quieter speakers carrying a shorter distance
- A Boombox you carry and play in your hand, and rest on your shoulder while it plays
- An Album Jukebox that plays up to nine discs or albums in order
- A Radio that plays a live stream from any link, or tunes in to a Radio Station. A redstone signal switches it off
- A Radio Station that holds up to 18 discs and puts them on air. Every radio tuned to it plays the same track at the same moment, even for a listener who tunes in late
- Custom disc labels and album covers, including your own images
- A Minecart with Jukebox, note block support, and a Bard villager who runs a music stall in villages

## Speakers and the Stereo

Place a Speaker next to a jukebox and the record plays from the speaker instead of the block. Put a Stereo on top of a jukebox to drive speakers wirelessly: click Link, then click the speakers you want to pair. Click the Stereo again, or sneak-click a speaker, to finish.

The Stereo takes two kinds of upgrade. Preamps raise how many speakers it can drive, and transmitters extend how far it reaches. Both show up on the drawing of the unit in the Stereo screen as you install them.

Vanilla music discs play through speakers too, not only etched ones.

## Requirements

- Fabric Loader and Fabric API
- [YetAnotherConfigLib](https://modrinth.com/mod/yacl)

## Supported versions

- Minecraft 1.20.1 (Fabric)
- Minecraft 1.21.1 (Fabric)

## What's different in this fork

- New art throughout. Every block, item, screen and the Bard were redrawn for this fork, so none of the original mod's art ships any more. See [How this fork was made](#how-this-fork-was-made).
- Non-OGG audio (many MP3 and streamed links) plays reliably. Format detection no longer relies on a tiny mark and reset buffer.
- AAC (`.m4a`) and FLAC playback.
- The Speaker and Stereo system, with wireless pairing, volume control, and hearing distance that follows volume.
- Vanilla discs play through speakers on both supported Minecraft versions.
- Rebuilt Speaker, Stereo and Album Printer screens.
- The Bard villager is back. Plains, desert, savanna, snowy and taiga villages can have a bard's music stall, and any villager who takes up an Etching Table becomes a Bard.
- Fixed volume sliders that could be clicked but not dragged, speaker link mode that could not be exited, and a paired speaker count that only ever climbed.

## Known gaps

Discs from this mod don't work in Sophisticated Backpacks' Jukebox Upgrade. Its Fabric port predates the disc handler API that would make this possible.

## Building

Gradle needs JDK 21. Minecraft 1.20.1 itself still compiles to and runs on Java 17.

The project uses [Stonecutter](https://stonecutter.kikugie.dev/) to build one source tree for several Minecraft versions. Building from the root makes a jar for every supported version:

```
./gradlew build
```

To run the development client for one version:

```
./gradlew :1.20.1:runClient
./gradlew :1.21.1:runClient
```

Jars land in `versions/<version>/build/libs/`.

Source files use Stonecutter comments (`//? if >=1.21 {`) to hold both versions in one tree. The commented-out branch is live code for the other version, so leave anything marked `//?` alone.

## How this fork was made

This fork was built with help from an AI assistant (Anthropic's Claude). It wrote much of the fork's new code, and it made the new textures, models, screen art and mod icon from the maintainer's art direction. The maintainer reviewed every change, tested it in game, and edited several models and textures by hand. The full breakdown is in [CONTENT_DISCLOSURE.md](docs/CONTENT_DISCLOSURE.md).

## Credits and licensing

Etched Refabricated is an unofficial fork of Etched, by Moonflower Studio (Jackson, Ocelot, Farcr, Echolite and AstraZoey). The Fabric port work it continues is by Aikoyori and VanderCat.

- The code and this fork's own art are licensed under GPL-3.0, the same license as the original code.
- Some data and text files in `src/main/resources/` (translations, recipes, loot tables, advancements, tags, item model files and a few config files) come from the original mod and stay All Rights Reserved by Moonflower Studio. LICENSE lists each one.

See [LICENSE](LICENSE) for the full terms.
