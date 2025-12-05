package com.training.demo.application.usecase.product;

import com.training.demo.dto.request.Product.ProductCreateRequest;
import com.training.demo.dto.response.Product.ProductResponse;
import com.training.demo.entity.Category;
import com.training.demo.entity.Product;
import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.mapper.ProductMapper;
import com.training.demo.repository.CategoryRepository;
import com.training.demo.repository.ProductRepository;
import com.training.demo.service.FileService;
import com.training.demo.utils.constants.ApiConstants;
import com.training.demo.utils.enums.UploadKind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Create Product
 * Business logic: Validate, upload image, create product with category
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;

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

        // 3. Upload product image if provided
        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                var uploadResult = fileService.upload(
                    UploadKind.PRODUCT, 
                    request.getImage(), 
                    "products/"
                );
                imageUrl = uploadResult.getPublicUrl();
            } catch (Exception e) {
                log.error("[CreateProductUseCase] Error uploading image: {}", e.getMessage());
                throw new BadRequestException("Could not upload product image. Please try again!");
            }
        }

        // 4. Create product entity
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .image(imageUrl)
                .category(category)
                .build();

        // 5. Save product
        Product savedProduct = productRepository.save(product);

        log.info("[CreateProductUseCase] Product created successfully with id: {}", savedProduct.getId());

        return ProductMapper.toProductResponse(savedProduct);
    }
}
