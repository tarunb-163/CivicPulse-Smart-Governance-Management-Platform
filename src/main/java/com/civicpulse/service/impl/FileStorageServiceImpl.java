package com.civicpulse.service.impl;

import com.civicpulse.entity.Attachment;
import com.civicpulse.entity.Complaint;
import com.civicpulse.enums.AttachmentKind;
import com.civicpulse.exception.BadRequestException;
import com.civicpulse.exception.ResourceNotFoundException;
import com.civicpulse.repository.AttachmentRepository;
import com.civicpulse.service.FileStorageService;
import com.civicpulse.util.ImageHashUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private Path rootLocation;
    private final AttachmentRepository attachmentRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    @PostConstruct
    @Override
    public void init() {
        try {
            this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.rootLocation);
            log.info("Initialized upload directory at: {}", this.rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize upload storage location", e);
            throw new RuntimeException("Could not initialize upload storage folder", e);
        }
    }

    @Override
    public Attachment store(MultipartFile file, Complaint complaint, AttachmentKind kind) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File " + file.getOriginalFilename() + " exceeds 5 MB size limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Invalid file type: " + contentType + ". Allowed: jpg, png, webp, pdf");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new BadRequestException("Invalid file path in filename: " + originalFilename);
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedName = UUID.randomUUID() + extension;

        try {
            byte[] bytes = file.getBytes();
            Path destinationFile = this.rootLocation.resolve(storedName).normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new BadRequestException("Cannot store file outside upload directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            String sha256 = ImageHashUtil.computeSha256(bytes);
            String phash = ImageHashUtil.computePhash(bytes);

            Attachment attachment = Attachment.builder()
                    .complaint(complaint)
                    .fileName(originalFilename)
                    .storedName(storedName)
                    .contentType(contentType)
                    .size(file.getSize())
                    .sha256(sha256)
                    .phash(phash)
                    .kind(kind)
                    .build();

            return attachmentRepository.save(attachment);
        } catch (IOException e) {
            log.error("Failed to store file {}", originalFilename, e);
            throw new RuntimeException("Failed to store file " + originalFilename, e);
        }
    }

    @Override
    public Resource loadAsResource(String storedName) {
        try {
            Path file = this.rootLocation.resolve(storedName).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Could not read file: " + storedName);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Could not read file: " + storedName);
        }
    }

    @Override
    public void delete(String storedName) {
        try {
            Path file = this.rootLocation.resolve(storedName).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Could not delete stored file: {}", storedName);
        }
    }
}
