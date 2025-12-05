package com.training.demo.application.usecase.product;

import com.training.demo.config.CacheConfig;
import com.training.demo.dto.request.Product.ProductRequest;
import com.training.demo.dto.response.Product.ProductResponse;
import com.training.demo.entity.Category;
import com.training.demo.entity.Product;
import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.mapper.mapstruct.ProductMapperMS;
import com.training.demo.repository.CategoryRepository;
import com.training.demo.repository.ProductRepository;
import com.training.demo.service.FileService;
import com.training.demo.utils.constants.ApiConstants;
import com.training.demo.utils.enums.UploadKind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Update Product
 * Business logic: Validate, update fields, handle image upload
 * Invalidates specific product cache and product list cache
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;
    private final ProductMapperMS productMapper;

    @Caching(evict = {
        @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
        @CacheEvict(value = CacheConfig.PRODUCT_LIST_CACHE, allEntries = true)
    })
    @Transactional
    public ProductResponse execute(Long productId, ProductRequest request) {
        log.info("[UpdateProductUseCase] Updating product id: {}", productId);

        // 1. Find existing product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                    String.format(ApiConstants.Messages.PRODUCT_NOT_FOUND, productId)
                ));

        // 2. Update basic fields
        if (request.getName() != null && !request.getName().isBlank()) {
            // Check name uniqueness (exclude current product)
            if (productRepository.existsByName(request.getName()) && 
                !product.getName().equals(request.getName())) {
                throw new BadRequestException(
                    String.format("Product with name '%s' already exists", request.getName())
                );
            }
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        }

        // 3. Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException(
                        String.format(ApiConstants.Messages.CATEGORY_NOT_FOUND, request.getCategoryId())
                    ));
            product.setCategory(category);
        }

        // 4. Update image URL if provided
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            product.setProductImageUrl(request.getImageUrl());
        }

        // 5. Save updated product
        Product updatedProduct = productRepository.save(product);

        log.info("[UpdateProductUseCase] Product updated successfully: {}", updatedProduct.getId());

        return productMapper.toProductResponse(updatedProduct);
    }
}
