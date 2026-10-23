# Art checklist: blocks and items

**Keep the exact path and the exact pixel size.** Drop the new file over the old one and
nothing in the code needs changing. All paths are relative to
`src/main/resources/assets/etched/`.

**One sheet per block.** A block marked "one sheet" reads every face from a single 32x32
picture, split into four 16x16 squares, the way Blockbench exports a model. Keep each face
inside its square, as the current sheet shows.

---

## 1. Must be redone

These are Moonflower Studio's work. Permission to use them was refused, so none of them
can ship. Every one has to be replaced with original art before release.

---

## 2. Optional: already ours

These were made for this fork, so there is no need to touch them. Listed only in case you
want them redone to match the new look.

### Album Jukebox (3)

- [ ] `models/block/album_jukebox.json` (3D model)
- [ ] `models/block/album_jukebox_full.json` (3D model)
- [ ] `textures/block/album_jukebox.png` (64x32)

### Etching Table (4)

- [ ] `models/block/etching_table.json` (3D model)
- [ ] `textures/block/etching_table.png` (32x32)
- [ ] `textures/item/empty_etching_table_slot_music_disc.png` (16x16)
- [ ] `textures/item/empty_etching_table_slot_music_label.png` (16x16)

### Radio Station (10)

- [ ] `models/block/radio_station.json` (3D model)
- [ ] `models/block/radio_station_on.json` (3D model)
- [ ] `textures/block/radio_station_bottom.png` (16x16)
- [ ] `textures/block/radio_station_front.png` (16x16)
- [ ] `textures/block/radio_station_front_on.png` (16x128)
- [ ] `textures/block/radio_station_front_on.png.mcmeta` (animation data)
- [ ] `textures/block/radio_station_side.png` (16x16)
- [ ] `textures/block/radio_station_side_on.png` (16x16)
- [ ] `textures/block/radio_station_top.png` (16x16)
- [ ] `textures/block/radio_station_top_on.png` (16x16)

### Radio (6)

- [ ] `models/block/radio.json` (3D model)
- [ ] `models/block/radio_225.json` (3D model)
- [ ] `models/block/radio_45.json` (3D model)
- [ ] `models/block/radio_675.json` (3D model)
- [ ] `textures/block/radio.png` (32x32)
- [ ] `textures/item/radio.png` (16x16)

### Portal Radio (6)

- [ ] `models/block/portal_radio.json` (3D model)
- [ ] `models/block/portal_radio_225.json` (3D model)
- [ ] `models/block/portal_radio_45.json` (3D model)
- [ ] `models/block/portal_radio_675.json` (3D model)
- [ ] `textures/block/portal_radio.png` (32x32)
- [ ] `textures/item/portal_radio.png` (16x16)

### Boombox (5)

- [ ] `models/item/boombox_in_hand.json` (3D model)
- [ ] `textures/item/boombox.png` (16x16)
- [ ] `textures/item/boombox_model.png` (32x32)
- [ ] `textures/item/boombox_playing.png` (32x256)
- [ ] `textures/item/boombox_playing.png.mcmeta` (animation data)

### Album Cover (4)

- [ ] `textures/item/album_cover_overlay.png` (16x16)
- [ ] `textures/item/blank_album_cover.png` (16x16)
- [ ] `textures/item/default_album_cover.png` (16x16)
- [ ] `textures/item/vanilla_album_cover.png` (16x16)

### Album Crate (6)

- [ ] `models/block/album_crate.json` (3D model)
- [ ] `models/block/album_crate_22.json` (3D model)
- [ ] `models/block/album_crate_45.json` (3D model)
- [ ] `models/block/album_crate_n22.json` (3D model)
- [ ] `textures/block/album_crate_trim.png` (16x16)
- [ ] `textures/block/album_crate_wood.png` (16x16)

### Album Display (5)

- [ ] `models/block/album_display_stand.json` (3D model)
- [ ] `models/block/album_display_stand_22.json` (3D model)
- [ ] `models/block/album_display_stand_45.json` (3D model)
- [ ] `models/block/album_display_stand_n22.json` (3D model)
- [ ] `textures/item/album_display.png` (16x16)

### Album Printer (2)

- [ ] `models/block/album_printer.json` (3D model)
- [ ] `textures/block/album_printer.png` (64x32)

### Speaker (4)

- [ ] `models/block/speaker.json` (3D model)
- [ ] `models/block/speaker_left.json` (3D model)
- [ ] `models/block/speaker_right.json` (3D model)
- [ ] `textures/block/speaker.png` (64x32)

### Stereo (6)

- [ ] `models/block/stereo.json` (3D model)
- [ ] `textures/block/stereo.png` (64x64)
- [ ] `textures/block/stereo_particle.png` (32x32)
- [ ] `textures/block/stereo_screen.png` (16x256)
- [ ] `textures/block/stereo_screen.png.mcmeta` (animation data)
- [ ] `textures/item/stereo.png` (16x16)

### Preamp (1)

- [ ] `textures/item/preamp.png` (16x16)

### Transmitter (1)

- [ ] `textures/item/transmitter.png` (16x16)

### Music Discs (2)

- [ ] `textures/item/blank_music_disc.png` (16x16)
- [ ] `textures/item/etched_music_disc.png` (16x16)

### Music Label (5)

- [ ] `textures/item/flat_label_bottom.png` (16x16)
- [ ] `textures/item/flat_label_top.png` (16x16)
- [ ] `textures/item/music_label_bottom.png` (16x16)
- [ ] `textures/item/music_label_paper.png` (16x16)
- [ ] `textures/item/music_label_top.png` (16x16)

### Jukebox Minecart (1)

- [ ] `textures/item/jukebox_minecart.png` (16x16)

### Other (1)

- [ ] `models/item/etched_album_cover/default.json` (3D model)

---

## 3. Not blocks or items, still must be redone

Also Moonflower Studio's, so these cannot ship either. Listed apart because they are not
an item or block artist's job.

None left.

---

## 4. Moved out, waiting for new versions

Moonflower's last pieces were moved out of the mod into `old-art/` at the repo root. That
folder stays on this machine only: it never ships and git ignores it.

- [ ] **Etching table sounds.** The composer is remaking them. Until then the table plays
  vanilla's cartography table sound. Drop the new files in as
  `sounds/ui/etching_table/take_result1.ogg` and `take_result2.ogg`, then point
  `ui.etching_table.take_result` in `sounds.json` back at them.
- [ ] **Bard village houses.** Villages get none until new ones ship. Each goes in
  `data/etched/structures/village/<village>/houses/<village>_bard_house_<n>.nbt` for plains,
  desert, savanna, snowy and taiga, numbered from 1. The game picks them up by name.
- [x] **Mod icon.** Redrawn: our etched disc with a gold label on light grey, after the gold variant.
- [x] **Logo and the old villager model files.** Nothing used them, so they are gone.

---

**Must be redone: 0 blocks and items, plus 0 others.**
**Optional: 72.**
