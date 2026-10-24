package com.civicpulse.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class ContentNormalizer {

    private ContentNormalizer() {}

    /**
     * Normalizes text by lowercasing, stripping all punctuation/symbols, and collapsing whitespace.
     */
    public static String normalize(String text) {
        if (text == null) return "";
        // Replace all non-alphanumeric characters with single space
        String stripped = text.replaceAll("[^a-zA-Z0-9]", " ");
        // Lowercase and trim multiple whitespaces
        return stripped.toLowerCase().replaceAll("\\s+", " ").trim();
    }

    /**
     * Normalizes title and description and returns a 64-char hex SHA-256 hash.
     */
    public static String computeHash(String title, String description) {
        String normalizedTitle = normalize(title);
        String normalizedDesc = normalize(description);
        String combined = normalizedTitle + "|||" + normalizedDesc;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Hashes normalized text into 64-char hex SHA-256 hash.
     */
    public static String hash(String text) {
        if (text == null) text = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(normalize(text).getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
