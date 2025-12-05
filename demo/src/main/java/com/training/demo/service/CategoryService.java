package com.training.demo.service;

import com.training.demo.dto.request.Category.CategoryRequest;
import com.training.demo.dto.response.Category.CategoryResponse;
import com.training.demo.dto.response.System.PageResponse;

public interface CategoryService {

    /**
     * Add a new category
     * @param categoryRequest the category request object
     * @return the created category response object
     */
    CategoryResponse addCategory(CategoryRequest categoryRequest);

    /**
     * Update an existing category
     * @param categoryRequest the category request object
     * @return the updated category response object
     */
    CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest);

    /**
     * Delete a category by its ID
     * @param categoryId the ID of the category to be deleted
     */
    void deleteCategory(Long categoryId);

    /**
     * Get all categories with pagination
     * @param page the page number
     * @param size the size of the page
     * @return a paginated response of category responses
     */
    PageResponse<?> getAllCategories(int page, int size);

    /**
     * Count total categories
     * @return total number of categories
     */
    long countCategories();
}
