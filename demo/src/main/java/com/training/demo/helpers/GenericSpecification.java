package com.training.demo.helpers;

import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class GenericSpecification<T> implements Specification<T> {

    private SpecSearchCriteria criteria;

    /**
     * Creates a WHERE clause for a query of the referenced entity in form of a {@link Predicate} for the given
     * {@link Root} and {@link CriteriaQuery}.
     *
     * @param root            must not be {@literal null}.
     * @param query           can be {@literal null} to allow overrides that accept {@link CriteriaDelete} which is an {@link AbstractQuery} but no {@link CriteriaQuery}.
     * @param builder must not be {@literal null}.
     * @return a {@link Predicate}, may be {@literal null}.
     */
    @Override
    public Predicate toPredicate(@NonNull Root<T> root,
                                 CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder builder) {

        var path = root.get(criteria.getKey());
        Class<?> javaType = path.getJavaType();

        Object value = criteria.getValue();

        //Nếu field là enum thì convert String -> Enum
        if (javaType.isEnum() && value instanceof String strVal) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Enum<?> enumValue = Enum.valueOf((Class<? extends Enum>) javaType, strVal);
            value = enumValue;
        }

        return switch (criteria.getOperation()) {
            case EQUALITY     -> builder.equal(path, value);
            case NEGATION     -> builder.notEqual(path, value);
            case GREATER_THAN -> {
                if (javaType == LocalDate.class && value instanceof LocalDate localDate) {
                    yield builder.greaterThan(path.as(LocalDate.class), localDate);
                } else {
                    yield builder.greaterThan(path.as(String.class), value.toString());
                }
            }
            case LESS_THAN    -> builder.lessThan(path.as(String.class), value.toString());
            case LIKE         -> builder.like(path.as(String.class), "%" + value + "%");
            case STARTS_WITH  -> builder.like(path.as(String.class), value + "%");
            case ENDS_WITH    -> builder.like(path.as(String.class), "%" + value);
            case CONTAINS     -> builder.like(path.as(String.class), "%" + value + "%");
        };
    }
}
