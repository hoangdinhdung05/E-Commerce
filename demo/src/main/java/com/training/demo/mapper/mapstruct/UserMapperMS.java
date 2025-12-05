package com.training.demo.mapper.mapstruct;

import com.training.demo.dto.response.User.UserResponse;
import com.training.demo.entity.User;
import org.mapstruct.*;

/**
 * MapStruct mapper for User entity to DTO conversions
 * Automatically generates implementation at compile time
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapperMS {

    /**
     * Map User entity to UserResponse DTO
     * @param user User entity
     * @return UserResponse DTO
     */
    UserResponse toUserResponse(User user);

    /**
     * Update existing UserResponse with User data
     * @param user Source user entity
     * @param userResponse Target DTO to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserResponse(User user, @MappingTarget UserResponse userResponse);
}
