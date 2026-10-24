package com.civicpulse.service;

import com.civicpulse.dto.response.ImageClassificationResult;
import org.springframework.web.multipart.MultipartFile;

public interface ImageClassifierService {
    ImageClassificationResult classifyImage(MultipartFile file);
}
