package com.training.demo.controller;

import com.training.demo.application.usecase.product.CreateProductUseCase;
import com.training.demo.application.usecase.product.GetProductUseCase;
import com.training.demo.application.usecase.product.UpdateProductUseCase;
import com.training.demo.dto.request.Product.ProductCreateRequest;
import com.training.demo.dto.request.Product.ProductRequest;
import com.training.demo.dto.response.Product.ProductResponse;
import com.training.demo.dto.response.System.BaseResponse;
import com.training.demo.dto.response.System.PageResponse;
import com.training.demo.service.ProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;
    
    // Use Cases
    private final GetProductUseCase getProductUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;

    /**
     * Create a new product
     * @param request product request
     * @return created product response
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping(value = "/create")
    public ResponseEntity<?> createProduct(@RequestBody ProductCreateRequest request) {
        log.info("[ProductController] Creating new product: {}", request.getName());
        return ResponseEntity.ok(BaseResponse.success(createProductUseCase.execute(request)));
    }

    /**
     * Update an existing product
     * @param id product id
     * @param request product request
     * @return updated product response
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        log.info("[ProductController] Updating product with id: {}", id);
        return ResponseEntity.ok(BaseResponse.success(updateProductUseCase.execute(id, request)));
    }

    /**
     * Delete product by id
     * @param productId product id
     * @return ResponseEntity indicating the result of the delete operation
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteProductById(@PathVariable Long productId) {
        log.info("[Product] Delete product with id: {}", productId);
        productService.deleteProductById(productId);
        return ResponseEntity.ok(BaseResponse.success());
    }

    /**
     * Get all products with pagination
     * @param pageNumber page number (min: 0)
     * @param pageSize page size (min: 1, max: 100)
     * @return paginated product responses
     */
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be >= 0") int pageNumber,
            @RequestParam(defaultValue = "12") @Min(value = 1, message = "Page size must be >= 1") @Max(value = 100, message = "Page size must be <= 100") int pageSize) {
        log.info("[Product] Get all products - page: {}, size: {}", pageNumber, pageSize);
        return ResponseEntity.ok(BaseResponse.success(productService.getAllProducts(pageNumber, pageSize)));
    }

    /**
     * Search products with filters and pagination
     * @param q search keyword
     * @param categoryId category ID filter
     * @param categoryIds list of category IDs filter
     * @param minPrice minimum price filter
     * @param maxPrice maximum price filter
     * @param inStock stock availability filter
     * @param pageable pagination information
     * @return paginated product responses matching the search criteria
     */
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false) String q,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) List<Long> categoryIds,
                                    @RequestParam(required = false) BigDecimal minPrice,
                                    @RequestParam(required = false) BigDecimal maxPrice,
                                    @RequestParam(required = false) Boolean inStock,
                                    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("[Product] Search products with filters");
        return ResponseEntity.ok(BaseResponse.success(
                productService.search(q, categoryId, categoryIds, minPrice, maxPrice, inStock, pageable)
        ));
    }


    /**
     * Get product by id
     * @param productId product id
     * @return product response
     */
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId) {
        log.info("[ProductController] Getting product by id: {}", productId);
        return ResponseEntity.ok(BaseResponse.success(getProductUseCase.execute(productId)));
    }

    /**
     * Count total products
     * @return total number of products
     */
    @GetMapping("/count")
    public ResponseEntity<?> countProducts() {
        log.info("[Product] Counting total products");
        return ResponseEntity.ok(BaseResponse.success(productService.countProducts()));
    }

    /**
     * Search products by names
     * @param name product names
     * @return list of product responses matching the names
     */
    @PostMapping("/search")
    public ResponseEntity<?> searchByNames(@RequestParam String name) {
        log.info("[Product] Search products by names");
        return ResponseEntity.ok(BaseResponse.success(productService.findByName(name)));
    }

    /**
     * Search products by category name with pagination
     * @param name category name
     * @param pageNumber page number
     * @param pageSize page size
     * @return paginated product responses matching the category name
     */
    @PostMapping("/search-category")
    public ResponseEntity<?> searchByCategory(@RequestParam String name,
                                              @RequestParam(defaultValue = "0") int pageNumber,
                                              @RequestParam(defaultValue = "12") int pageSize) {
        log.info("[Product] Search products by category");
        PageResponse<ProductResponse> response = productService.findByCategory(name, pageNumber, pageSize);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * Count products by category ID
     * @param categoryId category ID
     * @return number of products in the given category
     */
    @GetMapping("/count-by-category/{categoryId}")
    public ResponseEntity<?> countProductsByCategoryId(@PathVariable Long categoryId) {
        log.info("[Product] Counting products by category id: {}", categoryId);
        return ResponseEntity.ok(BaseResponse.success(productService.countProductsByCategoryId(categoryId)));
    }
}
