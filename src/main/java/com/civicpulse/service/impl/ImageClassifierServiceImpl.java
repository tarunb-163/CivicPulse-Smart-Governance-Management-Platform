package com.civicpulse.service.impl;

import com.civicpulse.dto.response.CategorySuggestion;
import com.civicpulse.dto.response.ImageClassificationResult;
import com.civicpulse.entity.Category;
import com.civicpulse.exception.BadRequestException;
import com.civicpulse.repository.CategoryRepository;
import com.civicpulse.service.ImageClassifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageClassifierServiceImpl implements ImageClassifierService {

    private final CategoryRepository categoryRepository;

    @Value("${app.vision.provider:}")
    private String visionProvider;

    @Value("${app.vision.api-key:}")
    private String visionApiKey;

    @Value("${app.vision.model:gemini-1.5-flash}")
    private String visionModel;

    @Value("${app.vision.timeout-ms:8000}")
    private int timeoutMs;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    @Override
    public ImageClassificationResult classifyImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Image exceeds 5 MB size limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Invalid file type: " + contentType + ". Allowed: jpg, png, webp");
        }

        // Check if provider and key are configured
        if (!StringUtils.hasText(visionProvider) || !StringUtils.hasText(visionApiKey)) {
            log.info("Vision provider or API key not configured; returning available:false gracefully");
            return ImageClassificationResult.unavailable("Vision classification not configured in this environment");
        }

        try {
            List<Category> allCategories = categoryRepository.findByActiveTrue();
            if (allCategories.isEmpty()) {
                return ImageClassificationResult.unavailable("No categories available");
            }

            // Real HTTP call to vision provider endpoint (supports custom mock server or provider URL)
            RestTemplate restTemplate = new RestTemplate();
            // Custom vision endpoint can be passed via provider URL or standard provider
            String endpointUrl = visionProvider.startsWith("http")
                    ? visionProvider
                    : "https://generativelanguage.googleapis.com/v1beta/models/" + visionModel + ":generateContent?key=" + visionApiKey;

            byte[] imageBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String categoryNamesList = allCategories.stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(", "));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(
                    Map.of("parts", Arrays.asList(
                            Map.of("text", "Classify this civic issue photo into exactly one of these categories: " + categoryNamesList + ". Respond only with the category name."),
                            Map.of("inlineData", Map.of(
                                    "mimeType", contentType,
                                    "data", base64Image
                            ))
                    ))
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(endpointUrl, HttpMethod.POST, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String responseText = response.getBody().toString();
                for (Category category : allCategories) {
                    if (responseText.toLowerCase().contains(category.getName().toLowerCase())) {
                        return ImageClassificationResult.builder()
                                .available(true)
                                .suggestedCategoryId(category.getId())
                                .suggestedCategoryName(category.getName())
                                .departmentId(category.getDepartment().getId())
                                .departmentName(category.getDepartment().getName())
                                .confidence(0.85)
                                .alternatives(Collections.emptyList())
                                .message("Category suggested by image analysis")
                                .build();
                    }
                }
            }

            return ImageClassificationResult.unavailable("Could not match category from image");

        } catch (Exception e) {
            log.warn("Image classification provider call failed or timed out: {}", e.getMessage());
            return ImageClassificationResult.unavailable("Image classification service temporarily unavailable");
        }
    }
}
