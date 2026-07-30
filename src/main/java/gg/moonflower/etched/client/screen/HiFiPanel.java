package gg.moonflower.etched.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class HiFiPanel {

    // Panel chrome: vanilla's structure, warmed very slightly.
    public static final int BG = 0xFFCDC6BA;
    public static final int HI = 0xFFEAE4D9;
    public static final int LO = 0xFFA79D90;
    public static final int EDGE = 0xFF4A4239;
    public static final int FACE_BTN = 0xFFB8B0A4;

    // Slots.
    public static final int SLOT = 0xFF9C948A;
    public static final int SLOT_SH = 0xFF5E564C;

    // Recessed display glass.
    public static final int GLASS = 0xFF332E27;
    public static final int GRID = 0xFF554C42;

    // Line art.
    public static final int INK = 0xFF5A5147;
    public static final int SOFT = 0xFF8F857A;

    // The drawn unit's own surfaces, lit lighter than the panel so it reads as an object sitting on it
    // rather than an outline scratched into it.
    public static final int UNIT_FACE = 0xFFE2DBCF;
    public static final int UNIT_TOP = 0xFFC0B8AA;
    public static final int UNIT_BACK = 0xFFA9A093;

    // Text.
    public static final int TEXT = 0xFF453C31;
    public static final int DIM = 0xFF7A6F62;

    // Amber, used only where it carries meaning: signal, level, "installed", "in range". Matches the
    // copper knobs on the stereo block.
    public static final int AM = 0xFFC4661F;
    public static final int AM_HI = 0xFFE9963A;
    public static final int AM_GLOW = 0xFFFFE0B0;

    // The teal of the stereo block's own VU window, so the drawn unit reads as the block the player placed.
    public static final int DISPLAY = 0xFF43B0A6;

    // The most speakers the field plots individually, which is the ceiling two preamps allow.
    private static final int MAX_SPEAKER_GLYPHS = 6;

    private HiFiPanel() {
    }

    // ---- primitives ----

    private static void px(GuiGraphics g, int x, int y, int color) {
        g.fill(x, y, x + 1, y + 1, color);
    }

    public static void line(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            px(g, x0, y0, color);
            if (x0 == x1 && y0 == y1) {
                return;
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    public static void arc(GuiGraphics g, int cx, int cy, int r, double a0, double a1, int color) {
        double step = Math.max(0.004, 0.55 / Math.max(1, r));
        for (double t = a0; t <= a1; t += step) {
            px(g, cx + (int) Math.round(r * Math.cos(t)), cy + (int) Math.round(r * Math.sin(t)), color);
        }
    }

    public static void circle(GuiGraphics g, int cx, int cy, int r, int color) {
        arc(g, cx, cy, r, 0.0, Math.PI * 2.0, color);
    }

    public static void box(GuiGraphics g, int x, int y, int x2, int y2, int color) {
        line(g, x, y, x2, y, color);
        line(g, x, y2, x2, y2, color);
        line(g, x, y, x, y2, color);
        line(g, x2, y, x2, y2, color);
    }

    // ---- chrome ----

    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        int x2 = x + w;
        int y2 = y + h;
        g.fill(x, y, x2, y2, EDGE);
        g.fill(x + 1, y + 1, x2 - 1, y2 - 1, BG);
        g.fill(x + 1, y + 1, x2 - 2, y + 2, HI);
        g.fill(x + 1, y + 1, x + 2, y2 - 2, HI);
        g.fill(x2 - 2, y + 2, x2 - 1, y2 - 1, LO);
        g.fill(x + 2, y2 - 2, x2 - 1, y2 - 1, LO);
    }

    public static void slot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, HI);
        g.fill(x - 1, y - 1, x + 16, y + 16, SLOT_SH);
        g.fill(x, y, x + 16, y + 16, SLOT);
    }

    public static void glass(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, SLOT_SH);
        g.fill(x + 1, y + 1, x + w, y + h, HI);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, GLASS);
    }

    public static void slider(GuiGraphics g, int x, int y, int w, int h, double value) {
        double v = clamp01(value);
        g.fill(x, y, x + w, y + h, SLOT_SH);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF7E766B);

        // Segmented rather than one solid bar, so a full slider reads as a level meter instead of a slab
        // of colour.
        int fillW = (int) Math.round((w - 4) * v);
        if (fillW > 0) {
            g.fill(x + 2, y + 2, x + 2 + fillW, y + h - 2, AM);
            g.fill(x + 2, y + 2, x + 2 + fillW, y + 3, AM_HI);
            for (int i = 4; i < fillW; i += 4) {
                g.fill(x + 2 + i, y + 2, x + 3 + i, y + h - 2, 0xFF9E4E14);
            }
        }

        int knobW = 5;
        int kx = x + 1 + (int) Math.round((w - 2 - knobW) * v);
        g.fill(kx, y, kx + knobW, y + h, EDGE);
        g.fill(kx + 1, y + 1, kx + knobW - 1, y + h - 1, HI);
        g.fill(kx + 1, y + h - 2, kx + knobW - 1, y + h - 1, LO);
    }

    public static void button(GuiGraphics g, Font font, int x, int y, int w, int h, Component label, boolean hovered, boolean active) {
        g.fill(x, y, x + w, y + h, EDGE);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, hovered && active ? HI : FACE_BTN);
        g.fill(x + 1, y + 1, x + w - 2, y + 2, HI);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, LO);
        // Drawn without a shadow: dark text on a light key is crisper without one, and the shadow was
        // muddying short labels.
        int labelW = font.width(label);
        g.drawString(font, label, x + (w - labelW) / 2, y + (h - 8) / 2, active ? TEXT : DIM, false);
    }

    // ---- instrumentation ----

    public static void waveform(GuiGraphics g, int x, int y, int w, int h, double level) {
        double v = clamp01(level);
        glass(g, x, y, w, h);

        int cy = y + h / 2;
        int maxH = h / 2 - 3;
        g.fill(x + 2, cy, x + w - 2, cy + 1, GRID);
        for (int i = 8; i < w - 4; i += 8) {
            px(g, x + 2 + i, cy - 1, GRID);
            px(g, x + 2 + i, cy + 1, GRID);
        }

        for (int i = 0; i < w - 6; i += 3) {
            double t = (double) i / (w - 6);
            double env = 0.30 + 0.70 * Math.abs(Math.sin(t * 11.0) * 0.6 + Math.sin(t * 23.0) * 0.4);
            int hh = Math.max(1, (int) Math.round(maxH * v * env));
            int bx = x + 3 + i;
            g.fill(bx, cy - hh, bx + 2, cy + hh + 1, AM);
            g.fill(bx, cy - hh, bx + 2, cy - hh + 1, AM_HI);
            g.fill(bx, cy + hh, bx + 2, cy + hh + 1, AM_HI);
        }
    }

    public static void ghostChip(GuiGraphics g, int x, int y) {
        box(g, x + 3, y + 6, x + 13, y + 11, SOFT);
        for (int i = 0; i < 3; i++) {
            px(g, x + 5 + i * 3, y + 4, SOFT);
            px(g, x + 5 + i * 3, y + 13, SOFT);
        }
    }

    public static Anchors stereoUnit(GuiGraphics g, int ox, int oy, int preamps, int transmitters) {
        int depth = 8;
        int fx = ox;
        int fy = oy + 8;
        int fx2 = ox + 52;
        int fy2 = oy + 30;
        int midY = fy + 11;

        // Solid faces first so the unit reads as an object against the panel, then the outline over them.
        // The top face and back panel are slanted, so they fill by scanline.
        for (int sy = oy; sy < fy; sy++) {
            int off = depth - (sy - oy);
            g.fill(fx + off, sy, fx2 + off, sy + 1, UNIT_TOP);
        }
        for (int sx = fx2; sx < fx2 + depth; sx++) {
            int off = sx - fx2;
            g.fill(sx, fy - off, sx + 1, fy2 - off, UNIT_BACK);
        }
        g.fill(fx, fy, fx2, fy2, UNIT_FACE);

        // Front face, top face and back panel, in three-quarter view so the places upgrades install into
        // are both visible.
        box(g, fx, fy, fx2, fy2, INK);
        line(g, fx, fy, fx + depth, oy, INK);
        line(g, fx + depth, oy, fx2 + depth, oy, INK);
        line(g, fx2, fy, fx2 + depth, oy, INK);
        line(g, fx2 + depth, oy, fx2 + depth, fy2 - depth, INK);
        line(g, fx2, fy2, fx2 + depth, fy2 - depth, INK);

        // The block's own face: a VU window between two copper knobs, with corner screws.
        int dx1 = fx + 18;
        int dx2 = fx2 - 18;
        g.fill(dx1, midY - 6, dx2, midY + 6, GLASS);
        box(g, dx1 - 1, midY - 7, dx2, midY + 6, SOFT);
        int[] bars = {3, 5, 2, 4};
        for (int i = 0; i < bars.length; i++) {
            int bx = dx1 + 2 + i * 3;
            g.fill(bx, midY + 4 - bars[i], bx + 2, midY + 4, DISPLAY);
        }
        for (int i = 0; i < 2; i++) {
            int kx = i == 0 ? fx + 10 : fx2 - 10;
            circle(g, kx, midY, 5, SOFT);
            g.fill(kx - 1, midY - 1, kx + 2, midY + 2, AM);
            line(g, kx, midY, kx, midY - 4, INK);
        }
        px(g, fx + 3, fy + 3, SOFT);
        px(g, fx2 - 3, fy + 3, SOFT);

        // Two preamp bays on the top face, aligned under the slots above them. An empty bay is drawn as a
        // recessed socket so it's clear something goes there.
        for (int i = 0; i < 2; i++) {
            int bx = fx + 8 + i * 22;
            int by = oy + 2;
            if (i < preamps) {
                box(g, bx, by, bx + 16, by + 4, INK);
                g.fill(bx + 1, by + 1, bx + 16, by + 4, AM);
                px(g, bx + 14, by + 2, AM_GLOW);
            } else {
                g.fill(bx + 1, by + 1, bx + 16, by + 4, UNIT_BACK);
                box(g, bx, by, bx + 16, by + 4, SOFT);
            }
        }

        // Two transmitter ports in the back panel, dongles plugged in connector-first when installed.
        for (int i = 0; i < 2; i++) {
            int dx = fx2 + depth;
            int dy = fy - 2 + i * 11;
            if (i < transmitters) {
                g.fill(dx, dy, dx + 3, dy + 3, SOFT);
                line(g, dx + 3, dy - 1, dx + 12, dy - 1, INK);
                line(g, dx + 3, dy + 4, dx + 12, dy + 4, INK);
                line(g, dx + 12, dy - 1, dx + 12, dy + 4, INK);
                g.fill(dx + 4, dy, dx + 12, dy + 4, AM);
                px(g, dx + 9, dy + 1, AM_GLOW);
            } else {
                g.fill(dx - 4, dy, dx - 1, dy + 3, GLASS);
                box(g, dx - 5, dy - 1, dx - 1, dy + 3, SOFT);
            }
        }

        return new Anchors(fx, midY, fx2 + depth);
    }

    public static void wirelessField(GuiGraphics g, int cx, int cy, int transmitters, int paired, int maxSpeakers) {
        double from = 2.55;
        double to = 3.75;
        int[] radii = {11, 19, 27, 35};
        int rings = 1 + Math.min(transmitters, 3);
        for (int i = 0; i < rings; i++) {
            arc(g, cx, cy, radii[i], from, to, SOFT);
        }

        // Speakers spread evenly across the whole sweep and alternate between two radii, so neighbours are
        // separated both along the arc and across it. Pinning them all to one radius made a full set
        // bunch together and overlap.
        int shown = Math.min(paired, MAX_SPEAKER_GLYPHS);
        for (int i = 0; i < shown; i++) {
            double a = from + (i + 0.5) * (to - from) / shown;
            int r = i % 2 == 0 ? 34 : 24;
            int sx = cx + (int) Math.round(r * Math.cos(a));
            int sy = cy + (int) Math.round(r * Math.sin(a));
            boolean driven = i < maxSpeakers;
            box(g, sx - 3, sy - 3, sx + 3, sy + 3, driven ? INK : SOFT);
            if (driven) {
                g.fill(sx - 2, sy - 2, sx + 3, sy + 3, AM);
                px(g, sx, sy, AM_GLOW);
            }
        }
    }

    private static double clamp01(double v) {
        return v < 0.0 ? 0.0 : Math.min(v, 1.0);
    }

    public record Anchors(int leftX, int midY, int backX) {
    }
}
