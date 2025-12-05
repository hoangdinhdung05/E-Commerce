package com.training.demo.dto.response.System;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadResponse {
    private String publicUrl;
    private String storagePath;
    private String fileName;
    private long size;
    private String contentType;
}
