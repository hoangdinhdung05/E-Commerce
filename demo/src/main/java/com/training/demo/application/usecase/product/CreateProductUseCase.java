package com.training.demo.application.usecase.product;

import com.training.demo.config.CacheConfig;
import com.training.demo.dto.request.Product.ProductCreateRequest;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Create Product
 * Business logic: Validate, upload image, create product with category
 * Invalidates product list cache after creation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;
    private final ProductMapperMS productMapper;

    @CacheEvict(value = CacheConfig.PRODUCT_LIST_CACHE, allEntries = true)
    @Transactional
    public ProductResponse execute(ProductCreateRequest request) {
        log.info("[CreateProductUseCase] Creating product: {}", request.getName());

        // 1. Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(
                    String.format(ApiConstants.Messages.CATEGORY_NOT_FOUND, request.getCategoryId())
                ));

        // 2. Validate product name uniqueness
        if (productRepository.existsByName(request.getName())) {
            throw new BadRequestException(
                String.format("Product with name '%s' already exists", request.getName())
            );
        }

        // 3. Create product entity
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .productImageUrl(request.getImageUrl())
                .category(category)
                .build();

        // 4. Save product
        Product savedProduct = productRepository.save(product);

        log.info("[CreateProductUseCase] Product created successfully with id: {}", savedProduct.getId());

        return productMapper.toProductResponse(savedProduct);
    }
}
