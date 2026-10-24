package com.civicpulse.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ImageHashTest {

    @Test
    @DisplayName("Compute SHA-256 for identical and differing bytes")
    void testSha256() {
        byte[] b1 = "CivicPulse Image Byte Stream".getBytes();
        byte[] b2 = "CivicPulse Image Byte Stream".getBytes();
        byte[] b3 = "Different Image Stream".getBytes();

        String hash1 = ImageHashUtil.computeSha256(b1);
        String hash2 = ImageHashUtil.computeSha256(b2);
        String hash3 = ImageHashUtil.computeSha256(b3);

        assertNotNull(hash1);
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
    }

    @Test
    @DisplayName("Compute perceptual dHash and Hamming distance between identical and slightly modified images")
    void testPhashAndHammingDistance() throws IOException {
        // Create an image with a diagonal pattern
        BufferedImage img1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g1 = img1.createGraphics();
        g1.setColor(Color.WHITE);
        g1.fillRect(0, 0, 100, 100);
        g1.setColor(Color.BLACK);
        g1.fillRect(10, 10, 80, 80);
        g1.dispose();

        // Create an identical image but slightly scaled/colored in corner
        BufferedImage img2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img2.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, 100, 100);
        g2.setColor(Color.BLACK);
        g2.fillRect(10, 10, 80, 80);
        g2.setColor(Color.GRAY);
        g2.fillRect(11, 11, 2, 2); // tiny difference
        g2.dispose();

        // Completely different image (all black)
        BufferedImage img3 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g3 = img3.createGraphics();
        g3.setColor(Color.BLACK);
        g3.fillRect(0, 0, 100, 100);
        g3.dispose();

        byte[] bytes1 = toPngBytes(img1);
        byte[] bytes2 = toPngBytes(img2);
        byte[] bytes3 = toPngBytes(img3);

        String phash1 = ImageHashUtil.computePhash(bytes1);
        String phash2 = ImageHashUtil.computePhash(bytes2);
        String phash3 = ImageHashUtil.computePhash(bytes3);

        assertNotNull(phash1);
        assertNotNull(phash2);
        assertNotNull(phash3);
        assertEquals(16, phash1.length());

        int distSame = ImageHashUtil.hammingDistance(phash1, phash1);
        assertEquals(0, distSame);

        int distSimilar = ImageHashUtil.hammingDistance(phash1, phash2);
        assertTrue(distSimilar <= 10, "Similar images should have Hamming distance <= 10, was: " + distSimilar);
        assertTrue(ImageHashUtil.isVisualDuplicate(phash1, phash2));
    }

    private byte[] toPngBytes(BufferedImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}
