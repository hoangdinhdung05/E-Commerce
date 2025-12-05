package com.training.demo.controller;

import com.training.demo.dto.request.Category.CategoryRequest;
import com.training.demo.dto.response.System.BaseResponse;
import com.training.demo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@Slf4j
@RequiredArgsConstructor

public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Add a new category
     * @param categoryRequest request body chứa thông tin category cần thêm
     * @return ResponseEntity chứa thông tin category vừa thêm
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody CategoryRequest categoryRequest) {
        log.info("[CATEGORY] Adding new category: {}", categoryRequest.getName());
        return ResponseEntity.ok(BaseResponse.success(categoryService.addCategory(categoryRequest)));
    }

    /**
     * Update an existing category
     * @param id the ID of the category to be updated
     * @param categoryRequest request body chứa thông tin category cần cập nhật
     * @return ResponseEntity chứa thông tin category vừa cập nhật
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CategoryRequest categoryRequest) {
        log.info("[CATEGORY] Updating category with id: {}", id);
        return ResponseEntity.ok(BaseResponse.success(categoryService.updateCategory(id, categoryRequest)));
    }

    /**
     * Delete a category by its ID
     * @param id the ID of the category to be deleted
     * @return ResponseEntity indicating the result of the delete operation
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("[CATEGORY] Deleting category with id: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(BaseResponse.success());
    }

    /**
     * Get all categories with pagination
     * @param page trang
     * @param size kích thước trang
     * @return ResponseEntity chứa danh sách category có phân trang
     */
    @GetMapping
    public ResponseEntity<?> getAllCategories(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        log.info("[CATEGORY] Fetching all categories - page: {}, size: {}", page, size);
        return ResponseEntity.ok(BaseResponse.success(categoryService.getAllCategories(page, size)));
    }

    /**
     * Count total categories
     * @return ResponseEntity chứa tổng số category
     */
    @GetMapping("/count")
    public ResponseEntity<?> countCategories() {
        log.info("[CATEGORY] Counting total categories");
        return ResponseEntity.ok(BaseResponse.success(categoryService.countCategories()));
    }
}
