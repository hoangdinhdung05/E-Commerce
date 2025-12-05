package com.training.demo.helpers;

import com.training.demo.utils.enums.SearchOperation;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;

public final class GenericSpecificationsBuilder<T> {
    private final List<SpecSearchCriteria> params = new ArrayList<>();

    public GenericSpecificationsBuilder<T> with(String key, String operation, Object value, String prefix, String suffix) {
        return with(null, key, operation, value, prefix, suffix);
    }

    public GenericSpecificationsBuilder<T> with(String orPredicate, String key, String operation, Object value, String prefix, String suffix) {
        var searchOperation = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (searchOperation != null) {
            if (searchOperation == SearchOperation.EQUALITY) {
                final boolean startWithAsterisk = prefix != null && prefix.contains(SearchOperation.ZERO_OR_MORE_REGEX);
                final boolean endWithAsterisk = suffix != null && suffix.contains(SearchOperation.ZERO_OR_MORE_REGEX);

                if (startWithAsterisk && endWithAsterisk) {
                    searchOperation = SearchOperation.CONTAINS;
                } else if (startWithAsterisk) {
                    searchOperation = SearchOperation.ENDS_WITH;
                } else if (endWithAsterisk) {
                    searchOperation = SearchOperation.STARTS_WITH;
                }
            }
            params.add(new SpecSearchCriteria(orPredicate, key, searchOperation, value));
        }
        return this;
    }

    // ✅ overload cho SpecSearchCriteria
    public GenericSpecificationsBuilder<T> with(SpecSearchCriteria criteria) {
        params.add(criteria);
        return this;
    }

    public Specification<T> build() {
        if (params.isEmpty()) return null;

        Specification<T> result = new GenericSpecification<>(params.get(0));

        for (int i = 1; i < params.size(); i++) {
            result = params.get(i).isOrPredicate()
                    ? Specification.where(result).or(new GenericSpecification<>(params.get(i)))
                    : Specification.where(result).and(new GenericSpecification<>(params.get(i)));
        }

        return result;
    }
}
