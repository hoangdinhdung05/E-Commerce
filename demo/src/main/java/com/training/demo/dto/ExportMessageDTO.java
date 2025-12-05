package com.training.demo.dto;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportMessageDTO implements Serializable {
    private Long userId;
    private String exportType;  // PDF, EXCEL, CSV
    private String fileName;
    private Map<String, Object> parameters;
    private LocalDateTime requestedAt;
}