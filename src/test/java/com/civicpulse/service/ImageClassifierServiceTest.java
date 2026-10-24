package com.civicpulse.service;

import com.civicpulse.dto.response.ImageClassificationResult;
import com.civicpulse.exception.BadRequestException;
import com.civicpulse.repository.CategoryRepository;
import com.civicpulse.service.impl.ImageClassifierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

public class ImageClassifierServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ImageClassifierServiceImpl imageClassifierService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Returns available:false when API key is not configured (IMG-4)")
    void testGracefulFallbackWhenNoKey() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "pothole.jpg", "image/jpeg", "dummy image content".getBytes()
        );

        ImageClassificationResult result = imageClassifierService.classifyImage(file);
        assertNotNull(result);
        assertFalse(result.isAvailable());
    }

    @Test
    @DisplayName("Rejects unsupported file type (e.g. text/plain or executable)")
    void testRejectsInvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "doc", "script.sh", "text/plain", "echo hello".getBytes()
        );

        assertThrows(BadRequestException.class, () -> imageClassifierService.classifyImage(file));
    }

    @Test
    @DisplayName("Rejects file exceeding 5MB size limit")
    void testRejectsOversizedFile() {
        byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6 MB
        MockMultipartFile file = new MockMultipartFile(
                "image", "huge.png", "image/png", largeBytes
        );

        assertThrows(BadRequestException.class, () -> imageClassifierService.classifyImage(file));
    }
}
