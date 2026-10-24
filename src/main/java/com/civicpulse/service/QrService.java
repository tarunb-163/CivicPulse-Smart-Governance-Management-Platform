package com.civicpulse.service;

public interface QrService {
    byte[] generateQrPng(String text, int width, int height);
    byte[] generateComplaintQrPng(String publicCode);
    String decodeQr(byte[] imageBytes);
}
