package gg.moonflower.etched.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class HiFiPanel {

    // Panel chrome: vanilla's own container values, warmed by a hair so the mod keeps a tone of its
    // own. Neutral greys and a hard bevel are what let these screens sit beside a chest or a furnace.
    public static final int BG = 0xFFC6C4C0;
    public static final int HI = 0xFFFFFFFF;
    public static final int LO = 0xFF57534D;
    public static final int EDGE = 0xFF1A1A1A;
    public static final int FACE_BTN = 0xFFC6C4C0;

    // Slots, at vanilla's contrast.
    public static final int SLOT = 0xFF8B8987;
    public static final int SLOT_SH = 0xFF373737;

    // Recessed display glass.
    public static final int GLASS = 0xFF332E27;
    public static final int GRID = 0xFF554C42;

    // Line art.
    public static final int INK = 0xFF4A4A48;
    public static final int SOFT = 0xFF8A8886;
    // Dim enough to read as a hint on a slot instead of vanishing into it.
    public static final int GHOST = 0xFF6E6C6A;

    // Text.
    public static final int TEXT = 0xFF404040;
    public static final int DIM = 0xFF707070;

    // Amber, used only where it carries meaning: signal, level, "installed", "in range". Matches the
    // copper knobs on the stereo block.
    public static final int AM = 0xFFC4661F;
    public static final int AM_HI = 0xFFE9963A;
    public static final int AM_GLOW = 0xFFFFE0B0;
    public static final int AM_LO = 0xFF8A430F;

    // The teal of the stereo block's own VU window, so the drawn unit reads as the block the player placed.
    public static final int DISPLAY = 0xFF43B0A6;

    // Sampled from the stereo block's texture so its drawing is recognisably that block.
    public static final int BODY = 0xFF1D1B19;
    public static final int BODY_HI = 0xFF42403F;
    public static final int BODY_LO = 0xFF0D0C0B;
    public static final int TEAL_BG = 0xFF101C1E;
    public static final int TEAL_SCAN = 0xFF16262A;
    public static final int TEAL = 0xFF1B757D;
    public static final int TEAL_HI = 0xFFB4D4E1;
    public static final int SOCKET = 0xFF2E2A25;

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

    /**
     * Midpoint circle. The angular stepping in {@link #arc} doubles some pixels and drops others, which
     * shows badly at the small radii the coverage display works at.
     */
    public static void ring(GuiGraphics g, int cx, int cy, int r, int color) {
        int x = r;
        int y = 0;
        int err = 1 - r;
        while (x >= y) {
            px(g, cx + x, cy + y, color);
            px(g, cx + y, cy + x, color);
            px(g, cx - x, cy + y, color);
            px(g, cx - y, cy + x, color);
            px(g, cx + x, cy - y, color);
            px(g, cx + y, cy - x, color);
            px(g, cx - x, cy - y, color);
            px(g, cx - y, cy - x, color);
            y++;
            if (err < 0) {
                err += 2 * y + 1;
            } else {
                x--;
                err += 2 * (y - x) + 1;
            }
        }
    }

    /** Draws a hand-authored pixel glyph, so small art stays exact instead of being computed. */
    public static void stamp(GuiGraphics g, int x, int y, String[] rows, int color) {
        for (int j = 0; j < rows.length; j++) {
            String row = rows[j];
            for (int i = 0; i < row.length(); i++) {
                if (row.charAt(i) == '#') {
                    px(g, x + i, y + j, color);
                }
            }
        }
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

    // Hand-authored so they stay crisp at 1:1. Stepped bars read as gain, stacked arcs as a transmitter.
    public static final String[] GAIN = {
            "........##",
            "........##",
            "....##..##",
            "....##..##",
            "##..##..##",
            "##..##..##",
            "##..##..##",
            "##..##..##",
    };
    public static final String[] ANTENNA = {
            "..######..",
            ".#......#.",
            "#........#",
            "..........",
            "...####...",
            "..#....#..",
            "..........",
            "....##....",
    };
    private static final String[] SPEAKER_ON = {"#####", "#####", "##.##", "#####", "#####"};
    private static final String[] SPEAKER_OFF = {"#####", "#...#", "#...#", "#...#", "#####"};

    // The drawn chassis, straight on rather than in projection, and symmetric about the panel's centre.
    // Each bay sits directly opposite the slot that fills it, which is what lets the screen do without
    // the leader lines the old three-quarter drawing needed.
    public static final int CHASSIS_W = 108;
    public static final int CHASSIS_H = 50;
    public static final int WIN_DX = 30;
    public static final int WIN_DY = 5;
    public static final int WIN_W = 48;
    public static final int WIN_H = 40;
    private static final int BAY_W = 20;
    private static final int BAY_H = 16;
    private static final int PREAMP_BAY_DX = 6;
    private static final int TRANSMITTER_BAY_DX = 82;
    private static final int[] BAY_DY = {6, 28};

    public static void stereoChassis(GuiGraphics g, int ox, int oy, int preamps, int transmitters,
                                     int paired, int maxSpeakers) {
        g.fill(ox, oy, ox + CHASSIS_W, oy + CHASSIS_H, BODY);
        g.fill(ox + 1, oy + 1, ox + CHASSIS_W - 1, oy + 2, BODY_HI);
        g.fill(ox + 1, oy + CHASSIS_H - 2, ox + CHASSIS_W - 1, oy + CHASSIS_H - 1, BODY_LO);
        box(g, ox, oy, ox + CHASSIS_W - 1, oy + CHASSIS_H - 1, EDGE);

        for (int i = 0; i < BAY_DY.length; i++) {
            bay(g, ox + PREAMP_BAY_DX, oy + BAY_DY[i], i < preamps, GAIN);
            bay(g, ox + TRANSMITTER_BAY_DX, oy + BAY_DY[i], i < transmitters, ANTENNA);
        }
        coverage(g, ox + WIN_DX, oy + WIN_DY, transmitters, paired, maxSpeakers);
    }

    // An empty bay still shows the part it takes, faintly, so which slot wants what is visible before
    // the player tries to put something in it.
    private static void bay(GuiGraphics g, int x, int y, boolean installed, String[] glyph) {
        int gx = x + (BAY_W - 10) / 2;
        int gy = y + (BAY_H - 8) / 2;
        if (installed) {
            g.fill(x, y, x + BAY_W, y + BAY_H, AM);
            g.fill(x, y, x + BAY_W, y + 1, AM_HI);
            g.fill(x, y + BAY_H - 1, x + BAY_W, y + BAY_H, AM_LO);
            stamp(g, gx, gy, glyph, AM_LO);
            box(g, x, y, x + BAY_W - 1, y + BAY_H - 1, BODY_LO);
        } else {
            g.fill(x, y, x + BAY_W, y + BAY_H, SOCKET);
            stamp(g, gx, gy, glyph, SOFT);
            box(g, x, y, x + BAY_W - 1, y + BAY_H - 1, BODY_HI);
        }
    }

    // One picture for the whole wireless state: a ring per transmitter for reach, a glyph per paired
    // speaker, hollow once the count runs past what the preamps can actually drive.
    private static void coverage(GuiGraphics g, int x, int y, int transmitters, int paired, int maxSpeakers) {
        g.fill(x, y, x + WIN_W, y + WIN_H, TEAL_BG);
        for (int sy = y + 2; sy < y + WIN_H - 1; sy += 3) {
            g.fill(x + 2, sy, x + WIN_W - 2, sy + 1, TEAL_SCAN);
        }
        box(g, x, y, x + WIN_W - 1, y + WIN_H - 1, BODY_LO);

        int cx = x + WIN_W / 2;
        int cy = y + WIN_H / 2;
        int[] radii = {6, 11, 16};
        for (int i = 0; i < 1 + Math.min(transmitters, radii.length - 1); i++) {
            ring(g, cx, cy, radii[i], TEAL);
        }

        int shown = Math.min(paired, MAX_SPEAKER_GLYPHS);
        for (int i = 0; i < shown; i++) {
            double a = -Math.PI / 2.0 + i * (Math.PI * 2.0 / shown);
            int sx = cx + (int) Math.round(13 * Math.cos(a));
            int sy = cy + (int) Math.round(13 * Math.sin(a));
            if (i < maxSpeakers) {
                stamp(g, sx - 2, sy - 2, SPEAKER_ON, TEAL_HI);
                px(g, sx, sy, TEAL_BG);
            } else {
                stamp(g, sx - 2, sy - 2, SPEAKER_OFF, TEAL);
            }
        }

        // Drawn last, so the unit always sits on top of its own field.
        g.fill(cx - 2, cy - 2, cx + 3, cy + 3, AM);
        px(g, cx, cy, AM_GLOW);
    }

    private static double clamp01(double v) {
        return v < 0.0 ? 0.0 : Math.min(v, 1.0);
    }
}
