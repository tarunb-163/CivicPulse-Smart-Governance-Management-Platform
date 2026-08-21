package com.civicpulse.controller;

import com.civicpulse.entites.Citizen;
import com.civicpulse.entites.Complaint;
import com.civicpulse.entites.ComplaintAttachment;
import com.civicpulse.repositories.CitizenRepository;
import com.civicpulse.services.ComplaintAttachmentService;
import com.civicpulse.services.ComplaintService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Controller
@RequestMapping("/citizen")
public class CitizenComplaintController {

    private final ComplaintService complaintService;
    private final ComplaintAttachmentService attachmentService;
    private final CitizenRepository citizenRepository;

    private static final String UPLOAD_DIR = "uploads/";

    public CitizenComplaintController(
            ComplaintService complaintService,
            ComplaintAttachmentService attachmentService,
            CitizenRepository citizenRepository) {

        this.complaintService = complaintService;
        this.attachmentService = attachmentService;
        this.citizenRepository = citizenRepository;
    }

    /*
     * =========================================================
     * SHOW COMPLAINT FORM
     * =========================================================
     */

    @GetMapping("/complaint-form")
    public String complaintForm(Model model) {

        model.addAttribute("complaint", new Complaint());

        return "citizen/complaint-form";
    }

    /*
     * =========================================================
     * SUBMIT COMPLAINT
     * =========================================================
     */

    @PostMapping(
            value = "/complaint-form",
            consumes = "multipart/form-data"
    )
    public String submitComplaint(

            @RequestParam("category")
            String category,

            @RequestParam("title")
            String title,

            @RequestParam("description")
            String description,

            @RequestParam("location")
            String location,

            @RequestParam(value = "attachment", required = false)
            MultipartFile attachment,

            Authentication authentication,

            RedirectAttributes redirectAttributes) {

        try {

            /*
             * =================================================
             * GET LOGGED-IN CITIZEN
             * =================================================
             */

            String username = authentication.getName();

            Citizen citizen = citizenRepository
                    .findByUsername(username)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Citizen not found: " + username
                            )
                    );

            /*
             * =================================================
             * CREATE COMPLAINT
             * =================================================
             */

            Complaint complaint = new Complaint();

            /*
             * Generate complaint number
             */

            String complaintNumber =
                    generateComplaintNumber();

            complaint.setComplaintNumber(
                    complaintNumber
            );

            complaint.setTitle(title);

            complaint.setDescription(description);

            complaint.setCategory(category);

            complaint.setLocation(location);

            /*
             * =================================================
             * DEFAULT VALUES
             * =================================================
             */

            complaint.setStatus("PENDING");

            complaint.setPriority(
                    determinePriority(category)
            );

            complaint.setDepartment(
                    determineDepartment(category)
            );

            /*
             * =================================================
             * CITIZEN INFORMATION
             * =================================================
             */

            complaint.setCitizenName(
                    citizen.getFullName()
            );

            complaint.setCitizenContact(
                    citizen.getPhone()
            );

            complaint.setCitizenEmail(
                    citizen.getEmail()
            );

            /*
             * =================================================
             * SAVE COMPLAINT
             * =================================================
             */

            Complaint savedComplaint =
                    complaintService.save(complaint);

            /*
             * =================================================
             * SAVE ATTACHMENT
             * =================================================
             */

            if (attachment != null
                    && !attachment.isEmpty()) {

                saveAttachment(
                        attachment,
                        savedComplaint
                );
            }

            /*
             * =================================================
             * SUCCESS MESSAGE
             * =================================================
             */

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Complaint registered successfully."
            );

            redirectAttributes.addFlashAttribute(
                    "complaintNumber",
                    savedComplaint.getComplaintNumber()
            );

            return "redirect:/citizen/complaints";

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to register complaint. Please try again."
            );

            return "redirect:/citizen/complaint-form";
        }
    }

    /*
     * =========================================================
     * SAVE ATTACHMENT
     * =========================================================
     */

    private void saveAttachment(
            MultipartFile file,
            Complaint complaint) throws IOException {

        /*
         * Create upload directory
         */

        Path uploadPath =
                Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {

            Files.createDirectories(
                    uploadPath
            );
        }

        /*
         * Original filename
         */

        String originalFileName =
                file.getOriginalFilename();

        if (originalFileName == null
                || originalFileName.isBlank()) {

            originalFileName = "attachment";
        }

        /*
         * Get extension
         */

        String extension = "";

        int dotIndex =
                originalFileName.lastIndexOf(".");

        if (dotIndex >= 0) {

            extension =
                    originalFileName.substring(
                            dotIndex
                    );
        }

        /*
         * Generate unique filename
         */

        String storedFileName =
                UUID.randomUUID()
                        .toString()
                        + extension;

        /*
         * Complete file path
         */

        Path filePath =
                uploadPath.resolve(
                        storedFileName
                );

        /*
         * Save physical file
         */

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        /*
         * =================================================
         * CREATE ATTACHMENT ENTITY
         * =================================================
         */

        ComplaintAttachment attachment =
                new ComplaintAttachment();

        attachment.setComplaint(
                complaint
        );

        attachment.setFileName(
                originalFileName
        );

        attachment.setStoredFileName(
                storedFileName
        );

        attachment.setFileType(
                file.getContentType()
        );

        attachment.setFileSize(
                file.getSize()
        );

        attachment.setFilePath(
                filePath.toString()
        );

        /*
         * Save attachment
         */

        attachmentService.save(
                attachment
        );
    }

    /*
     * =========================================================
     * GENERATE COMPLAINT NUMBER
     * =========================================================
     */

    private String generateComplaintNumber() {

        String date =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMdd"
                                )
                        );

        String random =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 6)
                        .toUpperCase();

        return "CP-" + date + "-" + random;
    }

    /*
     * =========================================================
     * DETERMINE PRIORITY
     * =========================================================
     */

    private String determinePriority(
            String category) {

        if (category == null) {
            return "MEDIUM";
        }

        String value =
                category.toLowerCase();

        if (value.contains("water")
                || value.contains("electricity")
                || value.contains("sanitation")) {

            return "HIGH";
        }

        return "MEDIUM";
    }

    /*
     * =========================================================
     * DETERMINE DEPARTMENT
     * =========================================================
     */

    private String determineDepartment(
            String category) {

        if (category == null) {
            return "GENERAL";
        }

        switch (category) {

            case "Road Damage":
                return "ROADS";

            case "Water Supply":
                return "WATER";

            case "Street Light":
                return "ELECTRICITY";

            case "Waste Management":
                return "WASTE MANAGEMENT";

            case "Sanitation":
                return "SANITATION";

            case "Electricity":
                return "ELECTRICITY";

            default:
                return "GENERAL";
        }
    }
}

