package com.training.demo.application.usecase.product;

import com.training.demo.dto.response.Product.ProductResponse;
import com.training.demo.entity.Product;
import com.training.demo.exception.NotFoundException;
import com.training.demo.mapper.ProductMapper;
import com.training.demo.repository.ProductRepository;
import com.training.demo.utils.constants.ApiConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Get Product by ID
 * Business logic: Retrieve product details and map to response
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GetProductUseCase {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ProductResponse execute(Long productId) {
        log.info("[GetProductUseCase] Getting product by id: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                    String.format(ApiConstants.Messages.PRODUCT_NOT_FOUND, productId)
                ));

        return ProductMapper.toProductResponse(product);
    }
}
