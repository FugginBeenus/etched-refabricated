package gg.moonflower.etched.client.render.item;

import com.mojang.blaze3d.platform.NativeImage;
import gg.moonflower.etched.common.item.CoverArt;

/**
 * Procedurally-generated album-cover patterns (no texture assets). Each pattern is drawn onto a
 * cover-sized canvas in a chosen color, over a base color, then multiplied by the cover overlay so it
 * matches the styling of image covers. Pattern indices are stored in {@link CoverArt} and are stable.
 */
public final class CoverPatterns {

    public static final String[] NAMES = {
            "Solid", "Border", "H-Stripes", "V-Stripes", "Cross",
            "Diagonal", "Circle", "Checker", "Corners", "Half"
    };
    public static final int COUNT = NAMES.length;

    private CoverPatterns() {
    }

    // A clean swatch for GUI previews: the pattern in `color` over a white base, no sleeve overlay.
    public static NativeImage preview(int pattern, int color, int size) {
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, size, size, false);
        fill(image, size, size, 0xFFFFFF);
        draw(image, size, size, pattern, color);
        return image;
    }

    public static NativeImage compose(CoverArt.PatternDesign design, NativeImage overlay) {
        int width = overlay.getWidth();
        int height = overlay.getHeight();
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);
        fill(image, width, height, design.baseColor());
        for (CoverArt.Layer layer : design.layers()) {
            draw(image, width, height, layer.pattern(), layer.color());
        }
        multiplyOverlay(image, overlay, width, height);
        return image;
    }

    private static void draw(NativeImage image, int w, int h, int pattern, int color) {
        switch (pattern) {
            case 0 -> fill(image, w, h, color);
            case 1 -> { // border
                int t = Math.max(1, Math.min(w, h) / 8);
                forEach(image, w, h, color, (x, y) -> x < t || y < t || x >= w - t || y >= h - t);
            }
            case 2 -> forEach(image, w, h, color, (x, y) -> (y * 8 / h) % 2 == 0); // horizontal stripes
            case 3 -> forEach(image, w, h, color, (x, y) -> (x * 8 / w) % 2 == 0); // vertical stripes
            case 4 -> { // cross
                int t = Math.max(1, Math.min(w, h) / 5);
                forEach(image, w, h, color, (x, y) -> Math.abs(x - w / 2) < t || Math.abs(y - h / 2) < t);
            }
            case 5 -> { // diagonal band
                int t = Math.max(1, Math.min(w, h) / 3);
                forEach(image, w, h, color, (x, y) -> Math.abs((x * h - y * w)) < (long) t * Math.max(w, h));
            }
            case 6 -> { // filled circle
                double r = Math.min(w, h) * 0.36;
                double cx = w / 2.0, cy = h / 2.0;
                forEach(image, w, h, color, (x, y) -> {
                    double dx = x + 0.5 - cx, dy = y + 0.5 - cy;
                    return dx * dx + dy * dy <= r * r;
                });
            }
            case 7 -> forEach(image, w, h, color, (x, y) -> ((x * 4 / w) + (y * 4 / h)) % 2 == 0); // checker
            case 8 -> { // corners
                int cw = Math.max(1, w / 3), ch = Math.max(1, h / 3);
                forEach(image, w, h, color, (x, y) -> (x < cw || x >= w - cw) && (y < ch || y >= h - ch));
            }
            case 9 -> forEach(image, w, h, color, (x, y) -> y < h / 2); // top half
            default -> {
            }
        }
    }

    private static void fill(NativeImage image, int w, int h, int rgb) {
        int abgr = abgr(rgb);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                image.setPixelRGBA(x, y, abgr);
            }
        }
    }

    private interface Mask {
        boolean on(int x, int y);
    }

    private static void forEach(NativeImage image, int w, int h, int rgb, Mask mask) {
        int abgr = abgr(rgb);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (mask.on(x, y)) {
                    image.setPixelRGBA(x, y, abgr);
                }
            }
        }
    }

    // Multiplies each pixel by the overlay's brightness so patterns get the same sleeve shading as
    // image covers. The overlay alpha is preserved so transparent overlay areas stay transparent.
    private static void multiplyOverlay(NativeImage image, NativeImage overlay, int w, int h) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int base = image.getPixelRGBA(x, y);
                int over = overlay.getPixelRGBA(x, y);
                int a = (over >>> 24) & 0xFF;
                int r = mul(base & 0xFF, over & 0xFF);
                int g = mul((base >> 8) & 0xFF, (over >> 8) & 0xFF);
                int b = mul((base >> 16) & 0xFF, (over >> 16) & 0xFF);
                image.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
    }

    private static int mul(int a, int b) {
        return (a * b) / 255;
    }

    // 0xRRGGBB -> NativeImage's opaque 0xAABBGGRR.
    private static int abgr(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return 0xFF000000 | (b << 16) | (g << 8) | r;
    }
}
