package com.civicpulse.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

@Slf4j
public final class ImageHashUtil {

    private ImageHashUtil() {}

    /**
     * Computes the SHA-256 hex string for byte array.
     */
    public static String computeSha256(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to compute SHA-256: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Computes a 64-bit Difference Hash (dHash) hex string (16 chars).
     * Returns null if bytes cannot be decoded as an image.
     */
    public static String computePhash(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            BufferedImage original = ImageIO.read(is);
            if (original == null) {
                return null; // Not an image (e.g. PDF)
            }

            // Resize to 9 width x 8 height grayscale
            BufferedImage small = new BufferedImage(9, 8, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = small.createGraphics();
            g.drawImage(original.getScaledInstance(9, 8, Image.SCALE_SMOOTH), 0, 0, null);
            g.dispose();

            long hash = 0;
            int bitIndex = 0;
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    int left = small.getRaster().getSample(x, y, 0);
                    int right = small.getRaster().getSample(x + 1, y, 0);
                    if (left > right) {
                        hash |= (1L << bitIndex);
                    }
                    bitIndex++;
                }
            }
            return String.format("%016x", hash);
        } catch (Exception e) {
            log.warn("Failed to compute perceptual hash: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Computes Hamming distance between two 16-hex-character perceptual hashes.
     * Returns number of differing bits (0 to 64).
     */
    public static int hammingDistance(String phash1, String phash2) {
        if (phash1 == null || phash2 == null || phash1.length() != 16 || phash2.length() != 16) {
            return 64;
        }
        try {
            long h1 = Long.parseUnsignedLong(phash1, 16);
            long h2 = Long.parseUnsignedLong(phash2, 16);
            return Long.bitCount(h1 ^ h2);
        } catch (NumberFormatException e) {
            return 64;
        }
    }

    /**
     * Threshold for considering two images visually similar (covers resizing, compression).
     */
    public static boolean isVisualDuplicate(String phash1, String phash2) {
        if (phash1 == null || phash2 == null) return false;
        return hammingDistance(phash1, phash2) <= 10;
    }
}
