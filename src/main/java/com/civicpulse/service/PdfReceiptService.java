package com.civicpulse.service;

import com.civicpulse.entity.Complaint;

public interface PdfReceiptService {
    byte[] generateReceiptPdf(Complaint complaint);
}
