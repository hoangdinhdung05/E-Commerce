package com.training.demo.service.impl;

import com.training.demo.dto.request.Auth.RegisterRequest;
import com.training.demo.dto.request.User.AdminCreateUserRequest;
import com.training.demo.dto.request.User.ChangePasswordRequest;
import com.training.demo.dto.request.User.UpdateUserRequest;
import com.training.demo.dto.response.System.PageResponse;
import com.training.demo.dto.response.User.UserDetailsResponse;
import com.training.demo.dto.response.User.UserResponse;
import com.training.demo.entity.Role;
import com.training.demo.entity.User;
import com.training.demo.entity.UserHasRole;
import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.helpers.FilterParser;
import com.training.demo.helpers.GenericSpecificationsBuilder;
import com.training.demo.mapper.UserMapper;
import com.training.demo.repository.RoleRepository;
import com.training.demo.repository.UserRepository;
import com.training.demo.security.SecurityUtils;
import com.training.demo.service.FileService;
import com.training.demo.service.UserService;
import com.training.demo.utils.enums.RoleType;
import com.training.demo.utils.enums.UploadKind;
import com.training.demo.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.training.demo.mapper.UserMapper.toUserDetailsResponse;
import static com.training.demo.mapper.UserMapper.toUserResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final FileService fileService;

    /**
     * Register a new user account.
     *
     * @param request registration details (e.g., email, password, name, etc.)
     */
    @Transactional
    @Override
    public void register(RegisterRequest request) {
        log.info("Registering new user account");

        validateFieldUser(request.getUsername(), request.getEmail());

        Role role = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new NotFoundException("Role user not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .verifyEmail(false)
                .build();

        UserHasRole userHasRole = UserHasRole.builder()
                .user(user)
                .role(role)
                .build();

        user.getUserHasRoles().add(userHasRole);

        userRepository.save(user);
        log.info("Registered new account successfully with username: {}", request.getUsername());
    }

    /**
     * Admin tạo ra account mới
     *
     * @param request thông tin cơ bản account
     */
    @Override
    public void adminCreate(AdminCreateUserRequest request) {
        log.info("[UserService] Admin create new user account");

        validateFieldUser(request.getUsername(), request.getEmail());

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new NotFoundException("Role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .verifyEmail(true)
                .build();

        UserHasRole userHasRole = UserHasRole.builder()
                .user(user)
                .role(role)
                .build();

        user.getUserHasRoles().add(userHasRole);

        userRepository.save(user);
        log.info("Admin created new account successfully with username: {}", request.getUsername());
    }

    /**
     * Xem thông tin cơ bản của user hiện tại
     *
     * @return Các thông tin cơ bản
     */
    @Override
    public UserDetailsResponse getCurrentUserDetails() {
        log.info("[UserService] Get current user details");
        User user = SecurityUtils.getCurrentUserDetails().getUser();
        return toUserDetailsResponse(user);
    }

    /**
     * Xem thông tin cơ bản user theo id
     *
     * @param id id user cần xem
     * @return Các thông tin cơ bản
     */
    @Override
    public UserResponse getUser(Long id) {
        log.info("[UserService] Get user by userId: {}", id);
        User user = getUserIfAuthorized(id);
        return toUserResponse(user);
    }

    /**
     * Xem thông tin chi tiết user theo id
     *
     * @param id id user cần xem
     * @return Các thông tin chi tiết
     */
    @Override
    public UserDetailsResponse getUserDetails(Long id) {
        log.info("[UserService] Get user details by userId: {}", id);
        User user = getUserIfAuthorized(id);
        return toUserDetailsResponse(user);
    }

    /**
     * Lấy danh sách user có phân trang
     *
     * @param pageNumber trang hiện tại
     * @param pageSize   kích thước
     * @return trả về danh sách
     */
    @Override
    public PageResponse<?> getAll(int pageNumber, int pageSize) {
        log.info("[UserService] Get list user");

        if (pageNumber < 0 || pageSize < 0) {
            throw new BadRequestException("Invalid PageNumber and PageSize");
        }

        Page<UserResponse> userPage = userRepository.findAll(PageRequest.of(pageNumber, pageSize))
                .map(UserMapper::toUserResponse);
        return PageResponse.of(userPage);
    }

    /**
     * User thay đổi mật khẩu của mình
     *
     * @param request Thông tin mật khẩu cũ và mới
     */
    @Override
    public void changePassword(ChangePasswordRequest request) {
        log.info("[UserService] Change password running");

        User user = userRepository.findByUsername(SecurityUtils.getCurrentUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        validatePassword(request, user);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Admin xóa tài khoản user
     *
     * @param id userId cần xóa
     */
    @Override
    public void deleteUser(Long id) {
        log.info("[UserService] Delete user by userId: {}", id);

        User currentUser = SecurityUtils.getCurrentUserDetails().getUser(); // Lấy user đang đăng nhập
        if (currentUser.getId().equals(id)) {
            throw new BadRequestException("You cannot delete your own account");
        }

        User user = getUserById(id);

        if (user.getStatus().equals(UserStatus.INACTIVE)) {
            throw new BadRequestException("User has been locked");
        } else {
            user.setStatus(UserStatus.INACTIVE);
            userRepository.save(user);
        }
    }

    /**
     * Admin tìm kiếm user với nhiều bộ lọc khác nhau
     *
     * @param filters  các bộ lọc
     * @param pageable thông tin phân trang
     * @return danh sách user
     */
    public PageResponse<UserResponse> searchUsersForAdmin(Map<String, String> filters, Pageable pageable) {
        log.info("[UserService] Search users for admin with filters: {}", filters);

        GenericSpecificationsBuilder<User> builder = new GenericSpecificationsBuilder<>();

        FilterParser.parse(filters).forEach(builder::with);

        Specification<User> spec = builder.build();

        Page<User> result = userRepository.findAll(spec, pageable);
        // map entity -> DTO
        Page<UserResponse> mapped = result.map(UserMapper::toUserResponse);

        return PageResponse.of(mapped);
    }

    /**
     * Cập nhật thông tin user
     *
     * @param id      userId cần cập nhật
     * @param request thông tin mới
     */
    @Transactional
    @Override
    public void updateUser(Long id, UpdateUserRequest request) {
        log.info("[UserService] Update user by userId: {}", id);

        User user = getUserIfAuthorized(id);

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }

        if (SecurityUtils.hasRole(RoleType.ADMIN.name())) {
            if (request.getStatus() != null) {
                user.setStatus(request.getStatus());
            }

            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                user.getUserHasRoles().clear();
                for (RoleType roleType : request.getRoles()) {
                    Role role = roleRepository.findByName(roleType)
                            .orElseThrow(() -> new NotFoundException("Role not found"));
                    UserHasRole userHasRole = UserHasRole.builder()
                            .user(user)
                            .role(role)
                            .build();
                    user.getUserHasRoles().add(userHasRole);
                }
            }
            user.setVerifyEmail(request.isVerifyEmail());
        }

        userRepository.save(user);
    }

    /**
     * Lấy tổng số user trong hệ thống
     *
     * @return tổng số user
     */
    @Override
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * Upload avatar
     *
     * @param userId userId
     * @param file   ảnh
     * @throws IOException error
     */
    @Override
    public void uploadAvatar(Long userId, MultipartFile file) throws IOException {
        log.info("[UserService] Upload avatar by userId: {}", userId);

        User user = getUserIfAuthorized(userId);

        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        try {

            var uploadResult = fileService.upload(UploadKind.AVATAR, file, "avatars/");
            String oldAvatarUrl = user.getAvatarUrl();
            user.setAvatarUrl(uploadResult.getPublicUrl());
            userRepository.save(user);

        } catch (Exception e) {
            log.error("Error uploading avatar: {}", e.getMessage());
            throw new org.apache.coyote.BadRequestException("Could not upload file. Please try again!");
        }
    }

    //========== PRIVATE METHOD ==========//
    private void validatePassword(ChangePasswordRequest request, User user) {
        final String oldRaw = request.getOldPassword();
        final String newRaw = request.getNewPassword();
        final String confirmRaw = request.getConfirmPassword();
        final String currentEncoded = user.getPassword();

        // 1) Xác thực oldPassword khớp với mật khẩu hiện tại
        if (!passwordEncoder.matches(oldRaw, currentEncoded)) {
            throw new BadRequestException("Old password is incorrect.");
        }

        // 2) new != old (so sánh raw string)
        if (newRaw.equals(oldRaw)) {
            throw new BadRequestException("New password must be different from the current password.");
        }

        // 3) new khớp confirm
        if (!newRaw.equals(confirmRaw)) {
            throw new BadRequestException("New password and confirm password do not match.");
        }

        // 4) (Khuyến nghị) Cấm đặt lại đúng mật khẩu hiện tại (trường hợp ai đó gửi cùng chuỗi nhưng muốn chắc ăn)
        if (passwordEncoder.matches(newRaw, currentEncoded)) {
            throw new BadRequestException("New password must be different from the current password.");
        }

        // 5) (Nếu có) kiểm tra policy qua @ValidPassword đã chạy ở tầng controller (@Valid)
        // Nếu bạn validate ở service, cần @Validated trên class và @Valid ở tham số method.
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateFieldUser(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }
    }

    private User getUserIfAuthorized(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!Objects.equals(currentUserId, id) && !SecurityUtils.hasRole(RoleType.ADMIN.name())) {
            throw new BadRequestException("You can only access your own user information");
        }
        return getUserById(id);
    }
}
