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

### Album Jukebox (1)

- [ ] `textures/block/album_jukebox.png` (32x32, one sheet for the whole block)

### Etching Table (4)

- [ ] `models/block/etching_table.json` (3D model)
- [ ] `textures/block/etching_table.png` (32x32, one sheet for the whole block)
- [ ] `textures/item/empty_etching_table_slot_music_disc.png` (16x16)
- [ ] `textures/item/empty_etching_table_slot_music_label.png` (16x16)

### Radio (6)

- [ ] `models/block/radio.json` (3D model)
- [ ] `models/block/radio_225.json` (3D model)
- [ ] `models/block/radio_45.json` (3D model)
- [ ] `models/block/radio_675.json` (3D model)
- [ ] `textures/block/radio.png` (32x32)
- [ ] `textures/item/radio.png` (16x16)

### Portal Radio (2)

- [ ] `textures/block/portal_radio.png` (32x32)
- [ ] `textures/item/portal_radio.png` (16x16)

### Boombox (3)

- [ ] `models/item/boombox_in_hand.json` (3D model)
- [ ] `textures/item/boombox.png` (16x16)
- [ ] `textures/item/boombox_model.png` (32x32)

### Album Cover (4)

- [ ] `textures/item/album_cover_overlay.png` (16x16)
- [ ] `textures/item/blank_album_cover.png` (16x16)
- [ ] `textures/item/default_album_cover.png` (16x16)
- [ ] `textures/item/vanilla_album_cover.png` (16x16)

### Music Discs (2)

- [ ] `textures/item/blank_music_disc.png` (16x16)
- [ ] `textures/item/etched_music_disc.png` (16x16)

### Music Label (4)

- [ ] `textures/item/flat_label_bottom.png` (16x16)
- [ ] `textures/item/flat_label_top.png` (16x16)
- [ ] `textures/item/music_label_bottom.png` (16x16)
- [ ] `textures/item/music_label_top.png` (16x16)

### Jukebox Minecart (1)

- [ ] `textures/item/jukebox_minecart.png` (16x16)

---

## 2. Optional: already ours

These were made for this fork, so there is no need to touch them. Listed only in case you
want them redone to match the new look.

### Radio Station (3)

- [ ] `textures/block/radio_station_front.png` (16x16)
- [ ] `textures/block/radio_station_side.png` (16x16)
- [ ] `textures/block/radio_station_top.png` (16x16)

### Album Crate (5)

- [ ] `models/block/album_crate.json` (3D model)
- [ ] `models/block/album_crate_22.json` (3D model)
- [ ] `models/block/album_crate_45.json` (3D model)
- [ ] `models/block/album_crate_n22.json` (3D model)
- [ ] `textures/item/album_crate.png` (16x16)

### Album Display (5)

- [ ] `models/block/album_display_stand.json` (3D model)
- [ ] `models/block/album_display_stand_22.json` (3D model)
- [ ] `models/block/album_display_stand_45.json` (3D model)
- [ ] `models/block/album_display_stand_n22.json` (3D model)
- [ ] `textures/item/album_display.png` (16x16)

### Album Printer (4)

- [ ] `models/block/album_printer.json` (3D model)
- [ ] `textures/block/album_printer.png` (128x128)
- [ ] `textures/block/album_printer_particle.png` (64x64)
- [ ] `textures/item/album_printer.png` (16x16)

### Speaker (4)

- [ ] `models/block/speaker.json` (3D model)
- [ ] `textures/block/speaker.png` (128x128)
- [ ] `textures/block/speaker_particle.png` (32x32)
- [ ] `textures/item/speaker.png` (16x16)

### Stereo (4)

- [ ] `models/block/stereo.json` (3D model)
- [ ] `textures/block/stereo.png` (64x64)
- [ ] `textures/block/stereo_particle.png` (32x32)
- [ ] `textures/item/stereo.png` (16x16)

### Preamp (1)

- [ ] `textures/item/preamp.png` (16x16)

### Transmitter (1)

- [ ] `textures/item/transmitter.png` (16x16)

---

## 3. Not blocks or items, still must be redone

Also Moonflower Studio's, so these cannot ship either. Listed apart because they are not
an item or block artist's job.

### GUI textures (5)

- [ ] `textures/gui/container/boombox.png` (256x256)
- [ ] `textures/gui/container/etching_table.png` (256x256)
- [ ] `textures/gui/edit_music_label.png` (256x256)
- [ ] `textures/gui/label.png` (256x256)
- [ ] `textures/gui/radio.png` (256x256)

### Bard / villager (4)

- [ ] `textures/entity/villager/profession/bard.png` (64x64)
- [ ] `textures/entity/villager/profession/bard.png.mcmeta` (animation data)
- [ ] `textures/entity/zombie_villager/profession/bard.png` (64x64)
- [ ] `textures/entity/zombie_villager/profession/bard.png.mcmeta` (animation data)

### Sounds (2)

- [ ] `sounds/ui/etching_table/take_result1.ogg` (sound)
- [ ] `sounds/ui/etching_table/take_result2.ogg` (sound)

---

**Must be redone: 27 blocks and items, plus 11 others.**
**Optional: 27.**
