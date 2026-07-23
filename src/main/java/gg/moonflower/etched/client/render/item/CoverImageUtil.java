package gg.moonflower.etched.client.render.item;

import com.mojang.blaze3d.platform.NativeImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Conversions between Minecraft's {@link NativeImage} and AWT/PNG, for the Album Printer image path.
 * NativeImage stores pixels as RGBA (little-endian int {@code 0xAABBGGRR}); AWT uses {@code 0xAARRGGBB},
 * so red and blue are swapped in both directions.
 */
public final class CoverImageUtil {

    private CoverImageUtil() {
    }

    public static NativeImage toNativeImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                nativeImage.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
        return nativeImage;
    }

    public static BufferedImage toBufferedImage(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int abgr = image.getPixelRGBA(x, y);
                int a = (abgr >>> 24) & 0xFF;
                int b = (abgr >> 16) & 0xFF;
                int g = (abgr >> 8) & 0xFF;
                int r = abgr & 0xFF;
                bufferedImage.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return bufferedImage;
    }

    public static byte[] encodePng(NativeImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(toBufferedImage(image), "png", out);
        return out.toByteArray();
    }

    public static NativeImage decodePng(byte[] bytes) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
        if (bufferedImage == null) {
            throw new IOException("Could not decode cover image");
        }
        return toNativeImage(bufferedImage);
    }

    public static NativeImage readFile(Path path) throws IOException {
        try (InputStream is = new FileInputStream(path.toFile())) {
            BufferedImage bufferedImage = ImageIO.read(is);
            if (bufferedImage == null) {
                throw new IOException("Unsupported image format: " + path);
            }
            return toNativeImage(bufferedImage);
        }
    }
}
