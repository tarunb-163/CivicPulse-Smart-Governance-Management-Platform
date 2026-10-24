package com.civicpulse.service.impl;

import com.civicpulse.entity.Complaint;
import com.civicpulse.service.PdfReceiptService;
import com.civicpulse.service.QrService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfReceiptServiceImpl implements PdfReceiptService {

    private final QrService qrService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
            .withZone(ZoneId.of("Asia/Kolkata"));

    @Override
    public byte[] generateReceiptPdf(Complaint complaint) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Brand Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(37, 99, 235));
            Paragraph header = new Paragraph("CivicPulse Grievance Receipt", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(4);
            document.add(header);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY);
            Paragraph sub = new Paragraph("Smart Governance Platform for Citizen Service Assistance", subtitleFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(24);
            document.add(sub);

            // Table with details and QR Code
            PdfPTable mainTable = new PdfPTable(2);
            mainTable.setWidthPercentage(100);
            mainTable.setWidths(new float[]{65f, 35f});

            // Details Cell
            PdfPCell detailsCell = new PdfPCell();
            detailsCell.setBorder(Rectangle.NO_BORDER);

            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(71, 85, 105));
            Font valFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(15, 23, 42));
            Font codeFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 15, new Color(29, 78, 216));

            detailsCell.addElement(new Paragraph("COMPLAINT TRACKING CODE", labelFont));
            detailsCell.addElement(new Paragraph(complaint.getPublicCode(), codeFont));
            detailsCell.addElement(new Paragraph(" ", labelFont));

            detailsCell.addElement(new Paragraph("TITLE", labelFont));
            detailsCell.addElement(new Paragraph(complaint.getTitle(), valFont));
            detailsCell.addElement(new Paragraph(" ", labelFont));

            detailsCell.addElement(new Paragraph("CATEGORY & DEPARTMENT", labelFont));
            String catDept = (complaint.getCategory() != null ? complaint.getCategory().getName() : "General")
                    + " (" + (complaint.getDepartment() != null ? complaint.getDepartment().getName() : "General") + ")";
            detailsCell.addElement(new Paragraph(catDept, valFont));
            detailsCell.addElement(new Paragraph(" ", labelFont));

            detailsCell.addElement(new Paragraph("URGENCY & CURRENT STATUS", labelFont));
            detailsCell.addElement(new Paragraph(complaint.getUrgency().name() + " | " + complaint.getStatus().name(), valFont));
            detailsCell.addElement(new Paragraph(" ", labelFont));

            detailsCell.addElement(new Paragraph("LOCATION / ADDRESS", labelFont));
            detailsCell.addElement(new Paragraph(complaint.getAddress() != null ? complaint.getAddress() : "Location coordinates registered", valFont));
            detailsCell.addElement(new Paragraph(" ", labelFont));

            detailsCell.addElement(new Paragraph("DATE LODGED", labelFont));
            detailsCell.addElement(new Paragraph(DATE_FORMATTER.format(complaint.getCreatedAt()), valFont));

            mainTable.addCell(detailsCell);

            // QR Code Cell
            PdfPCell qrCell = new PdfPCell();
            qrCell.setBorder(Rectangle.NO_BORDER);
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            byte[] qrBytes = qrService.generateComplaintQrPng(complaint.getPublicCode());
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.scaleToFit(140, 140);
            qrImage.setAlignment(Element.ALIGN_CENTER);

            qrCell.addElement(qrImage);
            Paragraph qrCaption = new Paragraph("Scan with camera to track status online", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.DARK_GRAY));
            qrCaption.setAlignment(Element.ALIGN_CENTER);
            qrCaption.setSpacingBefore(6);
            qrCell.addElement(qrCaption);

            mainTable.addCell(qrCell);
            document.add(mainTable);

            // Description Box
            Paragraph descHeader = new Paragraph("Detailed Description:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK));
            descHeader.setSpacingBefore(20);
            descHeader.setSpacingAfter(4);
            document.add(descHeader);

            PdfPTable descTable = new PdfPTable(1);
            descTable.setWidthPercentage(100);
            PdfPCell descCell = new PdfPCell(new Phrase(complaint.getDescription(), FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY)));
            descCell.setBackgroundColor(new Color(248, 250, 252));
            descCell.setPadding(12);
            descCell.setBorderColor(new Color(226, 232, 240));
            descTable.addCell(descCell);
            document.add(descTable);

            // Footer note
            Paragraph footer = new Paragraph("Official Grievance Acknowledgment — CivicPulse Administrative Portal\nFor inquiries contact the respective nodal department.", FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(40);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF receipt for complaint {}: {}", complaint.getPublicCode(), e.getMessage());
            throw new RuntimeException("Failed to generate PDF receipt", e);
        }
    }
}
