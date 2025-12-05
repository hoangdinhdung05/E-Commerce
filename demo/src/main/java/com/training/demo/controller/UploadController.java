package com.training.demo.controller;

import com.training.demo.dto.response.System.UploadResponse;
import com.training.demo.service.FileService;
import com.training.demo.utils.enums.UploadKind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final FileService fileService;

    @PostMapping(value = "/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadProductImage(
            @RequestPart("file") MultipartFile file) {
        try {
            UploadResponse response = fileService.upload(UploadKind.PRODUCT_IMAGE, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Upload failed", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
