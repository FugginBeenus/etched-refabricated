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
