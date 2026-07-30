# 1.2.0

## The bard is back

A bard villager works at an etching table, buys music discs, and sells labels, blank discs,
album covers, note blocks and jukeboxes. Bard houses generate in plains, desert, savanna,
snowy and taiga villages, each with an etching table sitting on the note block the bard
plays while working.

Houses are added to villages at runtime rather than through a datapack, so villages keep
everything other mods have put in them. A village overhaul that owns its own layout simply
does not receive the house, and there is a config toggle to switch the houses off entirely.

## Rebuilt screens

The speaker, stereo and album printer screens are drawn fresh. A speaker's volume opens a
waveform behind glass, so the control shows its own level. The stereo is drawn as the block
you placed, and the upgrades you install appear on it as hardware: preamps seat in bays on
the top face, transmitters plug into the back panel, and the wireless field widens with each
transmitter. Paired speakers ride that field and stay hollow past what the preamps can
drive, so pairing more than you can power is visible at a glance.

Stereo upgrade bays are now typed and hold one part each, which makes the speaker and range
ceilings real: a stack of preamps in one bay used to count as several.

## Fixes

- Vanilla discs now play through speakers on 1.20.1, matching 1.21.1. They were previously
  played twice over, which also made the volume sliders appear to do nothing.
- Vanilla disc playback follows the per-speaker and master volume sliders, and quieter
  speakers carry less far.
- Volume sliders can be dragged, not only clicked.
- Speaker link mode can be left again, by clicking the stereo or sneak-clicking a speaker.
  Previously the only way out was destroying the stereo, which locked you out of the
  per-speaker volume screen.
- The stereo's paired speaker count no longer climbs forever as speakers are broken and
  replaced.

## Known gaps

- Music discs from this mod cannot be used in Sophisticated Backpacks' Jukebox Upgrade. Its
  Fabric port predates the disc handler API that would make this possible.
