package com.training.demo.service.impl;

import com.training.demo.dto.request.Product.ProductCreateRequest;
import com.training.demo.dto.request.Product.ProductRequest;
import com.training.demo.dto.response.Product.ProductResponse;
import com.training.demo.dto.response.System.PageResponse;
import com.training.demo.entity.Category;
import com.training.demo.entity.Product;
import com.training.demo.exception.BadRequestException;
import com.training.demo.mapper.ProductMapper;
import com.training.demo.repository.CategoryRepository;
import com.training.demo.repository.ProductRepository;
import com.training.demo.repository.specification.ProductSpecs;
import com.training.demo.service.FileService;
import com.training.demo.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import static com.training.demo.mapper.ProductMapper.toProductResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;

    /**
     * Get product by id
     *
     * @param productId product id
     * @return product response
     */
    @Override
    public ProductResponse getProductById(Long productId) {
        log.info("[ProductService] Getting product by id: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException("Product with id " + productId + " does not exist"));

        return toProductResponse(product);
    }

    /**
     * Create a new product
     *
     * @param request product request
     * @return created product response
     */
    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("[ProductService] Creating product with name: {}", request.getName());
        validateForCreate(request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category with id " + request.getCategoryId() + " does not exist"));

        if (productRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("Product with name " + request.getName() + " already exists");
        }

        Product product = Product.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .productImageUrl(request.getImageUrl()) // gán URL FE truyền vào
                .category(category)
                .build();
        product = productRepository.save(product);

        log.info("[ProductService] Created product id={}", product.getId());
        return toProductResponse(product);
    }

    /**
     * Update an existing product
     *
     * @param request product request
     * @return updated product response
     */
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("[ProductService] Updating product id={} with name: {}", id, request.getName());

        validateForUpdate(request);

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Product with id " + id + " does not exist"));

        // Check duplicate name (if name changed)
        if (request.getName() != null
                && !request.getName().trim().equalsIgnoreCase(existing.getName())
                && productRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("Product with name " + request.getName() + " already exists");
        }

        // Validate category (if changed / provided)
        Category category = existing.getCategory();
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BadRequestException("Category with id " + request.getCategoryId() + " does not exist"));
        }

        // Apply changes
        if (request.getName() != null) {
            existing.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            existing.setPrice(request.getPrice());
        }
        if (request.getQuantity() != null) {
            existing.setQuantity(request.getQuantity());
        }
        existing.setCategory(category);

        // Update image if new URL provided
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            // Delete old image if exists
            String oldImageUrl = existing.getProductImageUrl();
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                try {
                    fileService.deleteByPublicUrl(oldImageUrl);
                } catch (Exception e) {
                    log.error("[ProductService] Failed to delete old product image at URL: {}", oldImageUrl);
                }
            }
            // Set new image URL
            existing.setProductImageUrl(request.getImageUrl());
        }

        Product saved = productRepository.save(existing);
        log.info("[ProductService] Updated product id={}", saved.getId());
        return toProductResponse(saved);
    }

    /**
     * Delete product by id
     *
     * @param productId product id
     */
    @Override
    @Transactional
    public void deleteProductById(Long productId) {
        log.info("[ProductService] Deleting product id={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException("Product with id " + productId + " does not exist"));

        String publicUrl = product.getProductImageUrl();

        try {
            fileService.deleteByPublicUrl(publicUrl);
        } catch (Exception e) {
            log.error("[ProductService] Failed to delete product image at URL: {}", publicUrl);
        }



        productRepository.deleteById(productId);
        log.info("[ProductService] Deleted product id={}", productId);
    }


    /**
     * Get all products with pagination
     *
     * @param pageNumber page number
     * @param pageSize page size
     * @return paginated product responses
     */
    @Override
    public PageResponse<?> getAllProducts(int pageNumber, int pageSize) {
        if (pageNumber < 0) throw new BadRequestException("Page index must be >= 0");
        if (pageSize <= 0 || pageSize > 200) throw new BadRequestException("Page size must be in range [1, 200]");

        Page<ProductResponse> productResponses = productRepository.findAll(
                PageRequest.of(pageNumber, pageSize, 
                    Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by("id")))
        ).map(ProductMapper::toProductResponse);
        return PageResponse.of(productResponses);
    }

    /**
     * Search products with filters and pagination
     *
     * @param q           search keyword
     * @param categoryId  category ID filter
     * @param categoryIds list of category IDs filter
     * @param minPrice    minimum price filter
     * @param maxPrice    maximum price filter
     * @param inStock     stock availability filter
     * @param pageable    pagination information
     * @return paginated product responses matching the search criteria
     */
    @Override
    public PageResponse<ProductResponse> search(String q,
                                                Long categoryId,
                                                List<Long> categoryIds,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                Boolean inStock,
                                                Pageable pageable) {
        log.info("[ProductService] Searching products with filters: q={}, categoryId={}, categoryIds={}, minPrice={}, maxPrice={}, inStock={}",
                q, categoryId, categoryIds, minPrice, maxPrice, inStock);

        // Validate khoảng giá
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("minPrice must be <= maxPrice");
        }

        Specification<Product> spec = Specification
                .where(ProductSpecs.keyword(q))
                .and(ProductSpecs.categoryId(categoryId))
                .and(ProductSpecs.categoryIds(categoryIds))
                .and(ProductSpecs.minPrice(minPrice))
                .and(ProductSpecs.maxPrice(maxPrice))
                .and(ProductSpecs.priceBetween(minPrice, maxPrice))
                .and(ProductSpecs.inStock(inStock));

        // dùng @EntityGraph của repository để tránh N+1
        Page<Product> page = productRepository.findAll(spec, pageable);

        var items = page.getContent()
                .stream()
                .map(com.training.demo.mapper.ProductMapper::toProductResponse)
                .toList();

        return PageResponse.<ProductResponse>builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .content(items)
                .build();
    }


    /**
     * Count total products
     *
     * @return total number of products
     */
    @Override
    public long countProducts() {
        log.info("[ProductService] Counting total products");
        return productRepository.count();
    }

    /**
     * Count products by category ID
     *
     * @param categoryId category ID
     * @return number of products in the given category
     */
    @Override
    public long countProductsByCategoryId(Long categoryId) {
        log.info("[ProductService] Counting products by category id={}", categoryId);
        return productRepository.countByCategoryId(categoryId);
    }

    /**
     * Check if any product exists by category ID
     *
     * @param categoryId category ID
     * @return true if any product exists with the given category ID, false otherwise
     */
    @Override
    public boolean existsByCategoryId(Long categoryId) {
        log.info("[ProductService] Checking existence of products with category id={}", categoryId);
        return productRepository.existsByCategoryId(categoryId);
    }

    /**
     * Find products by name (case-insensitive, partial match)
     * @param name product name
     * @return list of product responses matching the name
     */
    @Override
    public List<ProductResponse> findByName(String name) {
        log.info("[ProductService] Finding products by name: {}", name);
        return productRepository.searchByName(name);
    }

    /**
     * Find products by category name with pagination
     * @param category category name
     * @param pageSize number of items per page
     * @param pageNumber page number
     * @return paginated product responses matching the category
     */
    @Override
    public PageResponse<ProductResponse> findByCategory(String category, int pageSize, int pageNumber) {
        log.info("[ProductService] Finding products by category: {}", category);

        if (pageNumber < 0) throw new BadRequestException("Page index must be >= 0");
        Pageable pageable = PageRequest.of(pageSize, pageNumber);
        Page<ProductResponse> productResponses = productRepository.searchByCategory(category, pageable);

        return PageResponse.of(productResponses);
    }

    //========== PRIVATE METHOD ==========//
    private void validateForCreate(ProductCreateRequest req) {
        if (req == null) throw new BadRequestException("Request body must not be null");
        if (req.getName() == null || req.getName().isBlank())
            throw new BadRequestException("Product name must not be blank");
        if (req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new BadRequestException("Price must be >= 0");
        if (req.getQuantity() == null || req.getQuantity() < 0)
            throw new BadRequestException("Quantity must be >= 0");
        if (req.getCategoryId() == null)
            throw new BadRequestException("Category id must not be null");
        if (req.getImageUrl() == null || req.getImageUrl().isEmpty())
            throw new BadRequestException("Product image file must not be null");
    }

    private void validateForUpdate(ProductRequest req) {
        if (req == null) throw new BadRequestException("Request body must not be null");
        if (req.getName() != null && req.getName().isBlank())
            throw new BadRequestException("Product name must not be blank");
        if (req.getPrice() != null && req.getPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new BadRequestException("Price must be >= 0");
        if (req.getQuantity() != null && req.getQuantity() < 0)
            throw new BadRequestException("Quantity must be >= 0");
    }
}
