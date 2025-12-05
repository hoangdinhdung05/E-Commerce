package com.training.demo.helpers;

import com.training.demo.utils.enums.SearchOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterParser {

    public static List<SpecSearchCriteria> parse(Map<String, String> filters) {
        List<SpecSearchCriteria> criteriaList = new ArrayList<>();

        filters.forEach((rawKey, value) -> {
            SearchOperation operation = null;
            String key = rawKey;

            // duyệt qua các ký hiệu toán tử trong enum
            for (String opSymbol : SearchOperation.SIMPLE_OPERATION_SET) {
                if (rawKey.contains(opSymbol)) {
                    operation = SearchOperation.getSimpleOperation(opSymbol.charAt(0));
                    key = rawKey.replace(opSymbol, ""); // bỏ ký hiệu ra để còn lại field
                    break;
                }
            }

            // fallback nếu không tìm được toán tử
            if (operation == null) {
                operation = SearchOperation.EQUALITY;
            }

            criteriaList.add(new SpecSearchCriteria(key, operation, value));
        });

        return criteriaList;
    }
}