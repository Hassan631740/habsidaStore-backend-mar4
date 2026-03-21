package com.habsida.store.spec;

import com.habsida.store.enums.CustomerStatus;
import com.habsida.store.enums.OrderStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds JPA Specifications from a map of field name -> value (null/empty skipped).
 * Use only with a whitelist of allowed fields to avoid abuse.
 */
public final class FilterSpecs {

    private FilterSpecs() {
    }

    /**
     * Builds a Specification that ANDs equality for each non-null, non-empty value
     * for the given allowed fields. String values use case-insensitive contains.
     */
    public static <T> Specification<T> from(Map<String, String> params, Map<String, FilterMode> fieldModes) {
        if (params == null || params.isEmpty()) {
            return (root, query, cb) -> null;
        }
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (Map.Entry<String, String> e : params.entrySet()) {
                String param = e.getKey();
                String value = e.getValue();
                if (value == null || value.isBlank()) {
                    continue;
                }
                FilterMode mode = fieldModes.get(param);
                if (mode == null) {
                    continue;
                }
                try {
                    if (mode == FilterMode.EQUALS) {
                        predicates.add(cb.equal(root.get(param), value));
                    } else if (mode == FilterMode.EQUALS_ORDER_STATUS) {
                        predicates.add(cb.equal(root.get(param), OrderStatus.valueOf(value)));
                    } else if (mode == FilterMode.EQUALS_CUSTOMER_STATUS) {
                        predicates.add(cb.equal(root.get(param), CustomerStatus.valueOf(value)));
                    } else if (mode == FilterMode.EQUALS_LONG) {
                        predicates.add(cb.equal(root.get(param), Long.parseLong(value)));
                    } else if (mode == FilterMode.CONTAINS_IGNORE_CASE) {
                        predicates.add(cb.like(cb.lower(root.get(param)), "%" + value.toLowerCase() + "%"));
                    } else if (mode == FilterMode.EQUALS_BOOLEAN) {
                        boolean b = "true".equalsIgnoreCase(value);
                        predicates.add(cb.equal(root.get(param), b));
                    }
                } catch (Exception ignored) {
                    // skip invalid value (e.g. non-numeric for long)
                }
            }
            if (predicates.isEmpty()) {
                return null;
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public enum FilterMode {
        EQUALS,
        /** Equality on {@link OrderStatus} (query param must match enum constant name). */
        EQUALS_ORDER_STATUS,
        /** Equality on {@link CustomerStatus}. */
        EQUALS_CUSTOMER_STATUS,
        EQUALS_LONG,
        EQUALS_BOOLEAN,
        CONTAINS_IGNORE_CASE
    }
}
