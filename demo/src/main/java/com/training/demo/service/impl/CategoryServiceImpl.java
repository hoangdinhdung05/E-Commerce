package com.training.demo.service.impl;

import com.training.demo.dto.request.Category.CategoryRequest;
import com.training.demo.dto.response.Category.CategoryResponse;
import com.training.demo.dto.response.System.PageResponse;
import com.training.demo.entity.Category;
import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.mapper.CategoryMapper;
import com.training.demo.repository.CategoryRepository;
import com.training.demo.service.CategoryService;
import com.training.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    /**
     * Add a new category
     *
     * @param categoryRequest the category request object
     * @return the created category response object
     */
    @Override
    @Transactional
    public CategoryResponse addCategory(CategoryRequest categoryRequest) {
        log.info("[CategoryService] Adding new category: {}", categoryRequest.getName());

        if (categoryRequest.getName() == null || categoryRequest.getName().isBlank()) {
            throw new BadRequestException("Category name must not be blank");
        }

        // check uniqueness by name
        boolean exists = categoryRepository.findAll().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(categoryRequest.getName()));
        if (exists) {
            throw new BadRequestException("Category name already exists");
        }

        Category category = Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryMapper.categoryResponse(saved);
    }

    /**
     * Update an existing category
     *
     * @param categoryRequest the category request object
     * @return the updated category response object
     */
    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        log.info("[CategoryService] Updating category id={}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (categoryRequest.getName() != null && !categoryRequest.getName().isBlank()) {
            // check uniqueness
            boolean exists = categoryRepository.findAll().stream()
                    .anyMatch(c -> !c.getId().equals(id) && c.getName().equalsIgnoreCase(categoryRequest.getName()));
            if (exists) {
                throw new BadRequestException("Category name already exists");
            }
            category.setName(categoryRequest.getName());
        }

        if (categoryRequest.getDescription() != null) {
            category.setDescription(categoryRequest.getDescription());
        }

        Category saved = categoryRepository.save(category);
        return CategoryMapper.categoryResponse(saved);
    }

    /**
     * Delete a category by its ID
     *
     * @param categoryId the ID of the category to be deleted
     */
    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("[CategoryService] Deleting category id={}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        // check if any product exists with this category
        boolean exists = productService.existsByCategoryId(categoryId);
        if (exists) {
            throw new BadRequestException("Cannot delete category with associated products");
        }

        categoryRepository.delete(category);
    }

    /**
     * Get all categories with pagination
     *
     * @param page the page number
     * @param size the size of the page
     * @return a paginated response of category responses
     */
    @Override
    public PageResponse<?> getAllCategories(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Invalid PageNumber or PageSize");
        }

        Page<CategoryResponse> result = categoryRepository.findAll(PageRequest.of(page, size))
                .map(CategoryMapper::categoryResponse);

        return PageResponse.of(result);
    }

    /**
     * Count total categories
     *
     * @return total number of categories
     */
    @Override
    public long countCategories() {
        log.info("[CategoryService] Counting total categories");
        return categoryRepository.count();
    }
}
