package com.training.demo.application.usecase.product;

import com.training.demo.config.CacheConfig;
import com.training.demo.entity.Product;
import com.training.demo.exception.NotFoundException;
import com.training.demo.repository.ProductRepository;
import com.training.demo.utils.constants.ApiConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Delete Product
 * Business logic: Validate existence and delete product
 * Invalidates specific product cache and product list cache
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteProductUseCase {

    private final ProductRepository productRepository;

    @Caching(evict = {
        @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#productId"),
        @CacheEvict(value = CacheConfig.PRODUCT_LIST_CACHE, allEntries = true)
    })
    @Transactional
    public void execute(Long productId) {
        log.info("[DeleteProductUseCase] Deleting product id: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                    String.format(ApiConstants.Messages.PRODUCT_NOT_FOUND, productId)
                ));

        productRepository.delete(product);

        log.info("[DeleteProductUseCase] Product deleted successfully: {}", productId);
    }
}
