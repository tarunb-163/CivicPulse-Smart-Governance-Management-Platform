package com.civicpulse.service;

import com.civicpulse.service.impl.QrServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QrServiceTest {

    private QrService qrService;

    @BeforeEach
    void setUp() {
        qrService = new QrServiceImpl();
    }

    @Test
    @DisplayName("Generate QR code PNG and verify it decodes back to original URL")
    void testQrEncodeAndDecode() {
        String testUrl = "http://localhost:8080/track.html?code=CP-2026-000123";
        byte[] pngBytes = qrService.generateQrPng(testUrl, 250, 250);

        assertNotNull(pngBytes);
        assertTrue(pngBytes.length > 100);

        // Verify PNG magic bytes (0x89, 'P', 'N', 'G')
        assertEquals((byte) 0x89, pngBytes[0]);
        assertEquals((byte) 'P', pngBytes[1]);
        assertEquals((byte) 'N', pngBytes[2]);
        assertEquals((byte) 'G', pngBytes[3]);

        // Decode back
        String decoded = qrService.decodeQr(pngBytes);
        assertEquals(testUrl, decoded);
    }
}
