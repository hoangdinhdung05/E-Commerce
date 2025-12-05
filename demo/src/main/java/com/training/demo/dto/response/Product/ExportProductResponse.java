package com.training.demo.dto.response.Product;

import java.time.LocalDateTime;

public interface ExportProductResponse {
    long getId();
    String getName();
    String getDescription();
    double getPrice();
    int getQuantity();
    String getCategoryName();
    LocalDateTime getCreatedAt();
}
