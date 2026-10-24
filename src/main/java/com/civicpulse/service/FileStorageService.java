package com.civicpulse.service;

import com.civicpulse.entity.Attachment;
import com.civicpulse.entity.Complaint;
import com.civicpulse.enums.AttachmentKind;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    void init();
    Attachment store(MultipartFile file, Complaint complaint, AttachmentKind kind);
    Resource loadAsResource(String storedName);
    void delete(String storedName);
}
