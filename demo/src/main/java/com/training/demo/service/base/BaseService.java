package com.training.demo.service.base;

import com.training.demo.exception.BadRequestException;
import com.training.demo.exception.NotFoundException;
import com.training.demo.utils.constants.ValidationConstants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.function.Supplier;

/**
 * Base service providing common operations for service implementations
 * Eliminates duplicate code and enforces consistent patterns
 * 
 * @param <T> Entity type
 * @param <ID> Entity ID type
 */
public abstract class BaseService<T, ID> {
    
    /**
     * Get the repository instance
     * @return Repository for this entity
     */
    protected abstract JpaRepository<T, ID> getRepository();
    
    /**
     * Get the entity name for error messages
     * @return Entity name (e.g., "User", "Product")
     */
    protected abstract String getEntityName();
    
    /**
     * Find entity by ID or throw NotFoundException
     * 
     * @param id Entity ID
     * @return Entity if found
     * @throws NotFoundException if entity not found
     */
    protected T findByIdOrThrow(ID id) {
        return getRepository().findById(id)
            .orElseThrow(() -> new NotFoundException(
                String.format("%s not found with id: %s", getEntityName(), id)
            ));
    }
    
    /**
     * Find entity by ID with custom exception
     * 
     * @param id Entity ID
     * @param exceptionSupplier Custom exception supplier
     * @return Entity if found
     */
    protected T findByIdOrThrow(ID id, Supplier<RuntimeException> exceptionSupplier) {
        return getRepository().findById(id)
            .orElseThrow(exceptionSupplier);
    }
    
    /**
     * Check if entity exists by ID
     * 
     * @param id Entity ID
     * @return true if exists, false otherwise
     */
    protected boolean existsById(ID id) {
        return getRepository().existsById(id);
    }
    
    /**
     * Delete entity by ID with existence check
     * 
     * @param id Entity ID
     * @throws NotFoundException if entity not found
     */
    protected void deleteByIdOrThrow(ID id) {
        if (!existsById(id)) {
            throw new NotFoundException(
                String.format("%s not found with id: %s", getEntityName(), id)
            );
        }
        getRepository().deleteById(id);
    }
    
    /**
     * Validate pagination parameters
     * 
     * @param pageNumber Page number (must be >= 0)
     * @param pageSize Page size (must be between 1 and MAX_PAGE_SIZE)
     * @throws BadRequestException if parameters are invalid
     */
    protected void validatePagination(int pageNumber, int pageSize) {
        if (pageNumber < 0) {
            throw new BadRequestException(ValidationConstants.Messages.INVALID_PAGE_NUMBER);
        }
        if (pageSize < ValidationConstants.Pagination.MIN_PAGE_SIZE 
            || pageSize > ValidationConstants.Pagination.MAX_PAGE_SIZE) {
            throw new BadRequestException(ValidationConstants.Messages.INVALID_PAGE_SIZE);
        }
    }
    
    /**
     * Validate that a value is not null
     * 
     * @param value Value to check
     * @param fieldName Field name for error message
     * @throws BadRequestException if value is null
     */
    protected void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new BadRequestException(
                String.format("%s is required", fieldName)
            );
        }
    }
    
    /**
     * Validate that a string is not blank
     * 
     * @param value String to check
     * @param fieldName Field name for error message
     * @throws BadRequestException if string is null or blank
     */
    protected void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(
                String.format("%s cannot be blank", fieldName)
            );
        }
    }
    
    /**
     * Save entity
     * 
     * @param entity Entity to save
     * @return Saved entity
     */
    protected T save(T entity) {
        return getRepository().save(entity);
    }
    
    /**
     * Count total entities
     * 
     * @return Total count
     */
    protected long count() {
        return getRepository().count();
    }
}
