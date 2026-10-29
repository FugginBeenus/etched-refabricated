# 2.0.1

- The record in the label editor is simpler.

# 2.0.0

## New art throughout

Etched's original art belongs to Moonflower Studio, who did not give permission to use it here,
so all of it has been replaced. Every block, item and screen is redrawn, the Bard is a travelling
minstrel with a feather in his hat and a lute on his back, and the mod has a new icon. None of the
original art ships any more.

A few things changed on the way:

- The Album Jukebox is a turntable, with a window that shows what is playing.
- The Radio Station shows when it is on air, and the Stereo's screen moves with the music.
- A Speaker placed beside a jukebox or another music player slides over to sit snug against it.
- The Album Printer and Album Crate show their 3D models in the inventory. The Boombox has a flat
  icon there and stays 3D in your hand.

For now the Etching Table plays vanilla's cartography table sound. Its own sounds are on the way.

## Radio stations

A Radio Station holds up to 18 discs and puts one track on air. Radios tune in from a dropdown on
their screen, and every radio tuned in plays the same track at the same moment. A listener who
tunes in late joins part way through, like a real broadcast. A station can be named, plays
vanilla discs too, and keeps to its schedule with nobody listening.

Crouch and use a radio to switch it off and on. It keeps its link or station either way.

## The bard's stall

Bards set up a music stall in plains, desert, savanna, snowy and taiga villages, under a green and
red awning. The Etching Table sits on a note block the bard plays while he works, beside a jukebox
and a speaker, and a chest holds paper, labels, records and now and then a part for Etched's
machines. The stalls replace the old bard houses, which were part of the original art and never
built on 1.21.1 anyway.

## Album jukebox and redstone

The Album Jukebox has a redstone mode, set in its screen, for what a pulse does: restart the
record, skip to the next track, or go back one. Restart is the default, so old contraptions keep
working.

## Copying discs

An etched disc and a blank disc in a crafting grid give a copy, the way vanilla copies a map. The
original comes back, and only the blank is used.

## Advancements

Five of the mod's own: Record Scratch for etching your first disc, Audiophile for linking a speaker
to a stereo, Goes up to 11 for driving six speakers from one, Toss a Coin for finding a bard, and
Broadcast Radio for putting a station on air.

## For servers

Servers can list which sites music may come from. An empty list allows everything, so nothing
changes until a list is set.

## The boombox sits on your shoulder again

Carrying a playing boombox held it aloft in an odd raised pose. The mod has always shipped a
separate model for a boombox that is playing, drawn to rest on the shoulder, but the item
property that switches to it was never registered on this port. The game had no way to know a
boombox was playing, so it always fell back to the plain held model and that model was dead
content. It is wired up on both Minecraft versions now.

The holding arm also drifted while it was up there. The pose was being applied before vanilla
layers on its idle sway, so the sway won. It is applied after now, and the free arm keeps its
normal motion.

## Changes

- Disc labels come in one dyeable pattern. The other five never showed on the disc itself, so the
  pattern picker is gone from the Etching Table.
- This is an unofficial fork, and Mod Menu now says so. The original team is credited as
  contributors.

## Fixes

- Etched's recipes show up in the recipe book. On 1.21.1 none ever did, and on 1.20.1 several
  unlocked on any item at all.
- The Etching Table and Preamp could not be crafted on 1.20.1.
- The Radio Station broke into nothing. It drops now, and takes an axe.
- Closing a radio's screen no longer cuts off the station it is tuned to.
- Stereo upgrades could be stacked into one bay. Each bay takes one part now.
- Middle clicking a Minecart with Jukebox gave an empty hand. It hands over the item now, in
  survival as well as creative.
- Middle clicking a portal radio on 1.21.1 gave a plain radio. Only 1.20.1 had the check.
- Album crates lost their custom name when broken. A crate renamed on an anvil keeps that name,
  so a shop can label its bins and still rearrange them.

# 1.3.2

## Fixes

- The bard's four vanilla disc trades ran backwards. He bought discs off you for 8 emeralds
  each instead of selling them to you at that price. All four now sell.

# 1.3.1

## Cover art from the file itself

A disc etched from a self hosted link, on Google Drive or Dropbox or anywhere else, now takes
its cover from the artwork embedded in the audio file's own tags. ID3 in MP3 and the picture
block in FLAC are both read. Only the opening of the file is fetched, so this does not pull the
whole track down to look for a picture that may not be there.

## Item sprites

The Speaker, Stereo, Album Printer, Album Crate and Album Display were showing their block
models in the inventory. They have drawn sprites now, in the style of the ones the mod already
had for the radio and boombox.

# 1.3.0

## Show off your records

Two new blocks for anyone building a record shop.

The **Album Display** is a small stand that shows a single album's artwork. Right-click with a
record or album cover to set it, right-click again to take it back. Stands place at any of
sixteen angles rather than snapping to the four walls, so they can be arranged however a room
wants them.

The **Album Crate** is a shop bin on legs holding twelve albums. They stand upright inside,
filling from the back forward and riding a stepped floor so each sleeve clears the one in front
and the whole row reads at a glance. Anvil-rename the crate item and the placed crate takes that
name, so a shop can label its crates A to C and so on.

## The portal radio is back

Feed a radio a cake, or carry one into the Nether, and it turns into a portal radio. It has been
in the files for a long time without ever being reachable.

## FLAC

Discs can now be etched from FLAC links, alongside OGG, WAV, MP3 and AAC. Speakers get it too,
since they decode the same way.

## Recipes and the bard

Every block has a recipe now, including the two new ones. The speaker gains a pair of oak logs
for its cabinet, the stereo trades two of its six iron for copper, and the etching table costs
noticeably more than it did, so buying one from a bard is the easier route rather than a worse
deal. Preamps were repriced to sit alongside transmitters, which they had always undercut by a
wide margin despite being just as strong an upgrade.

The bard used to run a block shop, selling clay, hay, wool, bone, packed ice and gold. That is
gone. It now deals in the mod's own blocks, upgrades and materials: a listening room at level
three, the bigger machines at four, and the stereo with its preamps and transmitters at five.

## Fixes

- Album covers were invisible on 1.20.1. The renderer that draws them was only ever registered
  on 1.21, so covers, and the boombox held in hand, rendered as nothing.
- Bard houses never appeared in modpacks with village overhauls. They were only added to
  vanilla's own house pools, and an overhaul normally ships its own. The pools are searched for
  now, so an overhaul's naming is picked up too.

## Known gaps

- Music discs from this mod cannot be used in Sophisticated Backpacks' Jukebox Upgrade. Its
  Fabric port predates the disc handler API that would make this possible.
