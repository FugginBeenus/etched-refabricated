# Art drop-in guide

Everything below is already wired up and building. The files that ship today are **placeholders**
(flat colour, diagonal hatch, a letter in the corner). Overwrite a file and it appears in game — no
JSON edits needed, no code changes.

All textures are **16x16 PNG** with transparency, matching the rest of the mod.

## Textures

Drop into `src/main/resources/assets/etched/textures/`.

| File | Used for |
| --- | --- |
| `block/speaker_front.png` | Speaker — the face the cone is on |
| `block/speaker_side.png` | Speaker — left, right, back |
| `block/speaker_top.png` | Speaker — top and bottom |
| `block/stereo_front.png` | Stereo — the face discs eject from |
| `block/stereo_side.png` | Stereo — left, right, back |
| `block/stereo_top.png` | Stereo — top (the face you look down on) |
| `block/stereo_bottom.png` | Stereo — underside, against the jukebox lid |
| `block/album_printer_front.png` | Album Printer — front |
| `block/album_printer_side.png` | Album Printer — left, right, back |
| `block/album_printer_top.png` | Album Printer — top |
| `block/album_printer_bottom.png` | Album Printer — bottom |
| `item/preamp.png` | Preamp item |
| `item/transmitter.png` | Transmitter item |

Note on the **Stereo**: it is a 6-pixel-tall topper, not a full block. Its side textures are sampled
from the **bottom 6 pixels** of the image (v 10-16), so put the detail down there and leave the top
10 rows unused, or design the whole 16x16 and accept that only the lower strip shows on the sides.
The top face uses the full 16x16.

## Models

Drop into `src/main/resources/assets/etched/models/block/`.

If you have Blockbench models, replace these outright — the blockstates already point at them:

- `speaker.json` — currently `minecraft:block/orientable` (front / side / top)
- `album_printer.json` — currently `minecraft:block/orientable_with_bottom`
- `stereo.json` — a hand-written 6px-tall element, since no vanilla parent fits

Keep the same filenames. If your model declares its own `textures` block, the names above stop
mattering — use whatever paths your model references, just make sure the PNGs exist.

The item models (`models/item/speaker.json` etc.) simply inherit the block model, so the inventory
icon follows automatically. `preamp` / `transmitter` are flat `item/generated` sprites.

## Rotation

The three blocks are directional and the blockstates already handle it: the model is authored
**facing north** and rotated with `y` 90/180/270. Author front-facing north and rotation takes care
of itself.

**`uvlock` is off** on all three (it was removed). Leave it off for custom Blockbench models: uvlock
counter-rotates the texture on each face to stay "upright" as the block turns, which is meant for
plain full-cube vanilla blocks and *garbles* a model with hand-authored UVs (the texture looks
scrambled on the east/south/west variants only). With uvlock off the whole model — geometry and UVs
together — rotates rigidly, which is what you want.

## Blockbench gotchas (these will crash the model, showing magenta)

Vanilla Java models are stricter than Blockbench's editor. Two things it will happily let you do in
the editor but can't export to a working model:

1. **Element rotation is limited to -45°..+45° in 22.5° steps** (per element/cube). If you rotate a
   cube 90° (a knob, a dial, a leg), the export can't represent it and the *whole model* fails to
   load — everything goes magenta, not just that piece. The log says
   `Missing axis, expected to find a string`. Fix in Blockbench by modeling the piece in its final
   orientation instead of rotating it, or keep rotations within ±45°. (This happened with the
   stereo's two knobs; they were baked into fixed geometry by hand to recover it — but re-exporting
   from the same Blockbench file will bring the 90° back.)
2. **Face texture references must match a defined key.** Each face has `"texture": "#0"`; the
   `textures` block must define that exact key (`"0": "etched:block/..."`). If a face says `#0` but
   the key is named something else, that face is magenta. Blockbench usually gets this right; only an
   issue if the JSON was hand-edited.

The whole-model failure also shows in the log as
`Failed to load model etched:models/block/<name>.json` followed by a `JsonSyntaxException`. Always
check `run/logs/latest.log` when something is magenta — it names the exact file and reason.

## Checking your work

Validate that every texture a model references actually exists:

```bash
cd src/main/resources/assets/etched && python3 - <<'EOF'
import json, os, glob
missing = []
for p in glob.glob('models/**/*.json', recursive=True):
    for k, t in (json.load(open(p)).get('textures') or {}).items():
        if isinstance(t, str) and t.startswith('etched:'):
            f = 'textures/' + t.split(':', 1)[1] + '.png'
            if not os.path.exists(f):
                missing.append(f'{p} [{k}] -> {f}')
print('\n'.join(missing) if missing else 'all textures resolve')
EOF
```

Then see them in game:

```bash
./gradlew :1.21.1:runClient
```

## Still drawn in code

These are **not** textures yet — the screens are painted procedurally in Java:

- Album Printer screen (`client/screen/AlbumPrinterScreen.java`)
- Stereo screen (`client/screen/StereoScreen.java`)
- Speaker volume screen (`client/screen/SpeakerScreen.java`)

If you want real GUI backgrounds, they need a small code change to blit a texture instead of drawing
panels and slots. Ask and it can be swapped over to `textures/gui/container/*.png`.
