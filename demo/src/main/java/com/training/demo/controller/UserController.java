package com.training.demo.controller;

import com.training.demo.dto.request.User.AdminCreateUserRequest;
import com.training.demo.dto.request.User.ChangePasswordRequest;
import com.training.demo.dto.request.User.UpdateUserRequest;
import com.training.demo.dto.response.System.BaseResponse;
import com.training.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    /**
     * Get user information by ID
     * @param id user ID
     * @return basic user information
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        log.info("[UserController] Getting user by id: {}", id);
        return ResponseEntity.ok(BaseResponse.success(userService.getUser(id)));
    }

    /**
     * Get detailed user information by ID
     * @param id user ID
     * @return detailed user information
     */
    @GetMapping("/details/{id}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        log.info("[User] Get user details by userId: {}", id);
        return ResponseEntity.ok(BaseResponse.success(userService.getUserDetails(id)));
    }

    /**
     * Get paginated list of users
     * @param pageNumber current page number
     * @param pageSize page size
     * @return paginated user list
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(name = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        log.info("[User] Get all users - pageNumber: {}, pageSize: {}", pageNumber, pageSize);
        return ResponseEntity.ok(BaseResponse.success(userService.getAll(pageNumber, pageSize)));
    }

    /**
     * Change user password
     * @param request old and new password information
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        log.info("[UserController] Changing password for current user");
        userService.changePassword(request);
        return ResponseEntity.ok(BaseResponse.success("Password changed successfully"));
    }

    /**
     * Admin deletes user account
     * @param id user ID to delete
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        log.info("[User] Delete user by userId: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(BaseResponse.success());
    }

    /**
     * Admin creates new user account
     * @param request basic account information
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> adminCreateUser(@RequestBody @Valid AdminCreateUserRequest request) {
        log.info("[User] Admin create new user with username: {}", request.getUsername());
        userService.adminCreate(request);
        return ResponseEntity.ok(BaseResponse.success());
    }

    /**
     * Get current authenticated user information
     * @return current user basic information
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        log.info("[User] Get current user");
        return ResponseEntity.ok(BaseResponse.success(userService.getCurrentUserDetails()));
    }

    /**
     * Admin searches users with multiple filters
     *
     * @param filters search filters
     * @param pageable pagination information
     * @return filtered user list
     */
    @GetMapping("/filter")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getUsers(
            @RequestParam Map<String, String> filters,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        log.info("[User] Admin search users with filters: {}, pageable: {}", filters, pageable);

        Map<String, String> realFilters = filters.entrySet().stream()
                .filter(entry -> !List.of("page", "size", "sort").contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return ResponseEntity.ok(BaseResponse.success(userService.searchUsersForAdmin(realFilters, pageable)));
    }

    /**
     * User updates their personal information
     * @param id user ID to update
     * @param request new information
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        log.info("[User] Update user info for userId: {}", id);
        userService.updateUser(id, request);
        return ResponseEntity.ok(BaseResponse.success());
    }

    /**
     * Get total number of users
     * @return total count
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @GetMapping("/count")
    public ResponseEntity<?> count() {
        log.info("[User] Count total users");
        return ResponseEntity.ok(BaseResponse.success(userService.countUsers()));
    }

    /**
     * Upload user avatar
     * @param id user ID
     * @param file avatar image file
     * @return success response
     * @throws IOException if upload fails
     */
    @PostMapping("/{id}/avatar")
    public ResponseEntity<?> uploadAvatar(@PathVariable Long id,
                                          @RequestParam("file") MultipartFile file) throws IOException {
        log.info("[User] Upload avatar for user: {}", id);
        userService.uploadAvatar(id, file);
        return ResponseEntity.ok(BaseResponse.success());
    }
}
