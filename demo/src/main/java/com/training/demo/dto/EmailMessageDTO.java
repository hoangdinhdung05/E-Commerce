package com.training.demo.dto;

import com.training.demo.utils.enums.EmailType;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessageDTO implements Serializable {
    
    private String[] to;
    private String subject;
    private String templateName;
    private Map<String, Object> variables;
    private EmailType emailType;
    private String content; // For simple text email
    private LocalDateTime requestedAt;
    private Integer retryCount;
}
