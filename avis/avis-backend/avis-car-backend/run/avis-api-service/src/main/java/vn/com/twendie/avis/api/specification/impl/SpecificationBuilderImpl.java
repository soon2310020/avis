package vn.com.twendie.avis.api.specification.impl;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.internal.BasicPathUsageException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.enumtype.PredicateTypeEnum;
import vn.com.twendie.avis.api.specification.SpecificationBuilder;

import javax.persistence.ManyToOne;
import javax.persistence.criteria.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static javax.persistence.FetchType.EAGER;

@Component
public class SpecificationBuilderImpl<T> implements SpecificationBuilder<T> {

    private final DateUtils dateUtils;

    public SpecificationBuilderImpl(DateUtils dateUtils) {
        this.dateUtils = dateUtils;
    }

    @Override
    public Specification<T> build(Object filter) {
        return (Specification<T>) (root, query, builder) -> {
            if (!isCountQuery(query)) {
                Arrays.stream(root.getJavaType().getDeclaredFields())
                        .filter(field -> field.isAnnotationPresent(ManyToOne.class))
                        .filter(field -> field.getAnnotation(ManyToOne.class).fetch().equals(EAGER))
                        .forEach(field -> root.fetch(field.getName(), JoinType.LEFT));
            }
            return builder.and(toPredicates(root, query, builder, filter));
        };
    }

    private Predicate[] toPredicates(From<?, ?> root, CriteriaQuery<?> query, CriteriaBuilder builder, Object filter) {
        if (Objects.isNull(filter)) {
            return new Predicate[0];
        }
        List<Predicate> predicates = Arrays.stream(filter.getClass().getDeclaredFields())
                .map(field -> {
                    try {
                        field.setAccessible(true);
                        return toPredicate(root, query, builder, field.getName(), field.get(filter));
                    } catch (IllegalAccessException e) {
                        return builder.and();
                    }
                })
                .collect(Collectors.toList());
        predicates.add(deletedFalsePredicate(root, builder));
        return predicates.toArray(new Predicate[0]);
    }

    private Predicate toPredicate(From<?, ?> root, CriteriaQuery<?> query, CriteriaBuilder builder, String field, Object value) {
        if (StringUtils.isBlank(Objects.toString(value, ""))) {
            return builder.and();
        }
        PredicateTypeEnum predicateType = PredicateTypeEnum.parse(field, value);
        field = cleanField(field, predicateType);
        try {
            root = root.join(field);
            return builder.and(toPredicates(root, query, builder, value));
        } catch (BasicPathUsageException e) {
            return toPredicate(root, builder, field, value, predicateType);
        } catch (IllegalArgumentException e) {
            return builder.and();
        }
    }

    private Predicate toPredicate(From<?, ?> root, CriteriaBuilder builder, String field, Object value, PredicateTypeEnum predicateType) {
        switch (predicateType) {
            case EQ:
                return toEqPredicate(root, builder, field, value);
            case IN:
                return toInPredicate(root, builder, field, (Collection<?>) value);
            case LIKE:
                return toLikePredicate(root, builder, field, value);
            case NOT:
                return toNotPredicate(root, builder, field, value);
            case GT:
                return toGtPredicate(root, builder, field, value);
            case GTE:
                return toGtePredicate(root, builder, field, value);
            case LT:
                return toLtPredicate(root, builder, field, value);
            case LTE:
                return toLtePredicate(root, builder, field, value);
        }
        return builder.and();
    }

    private Predicate toEqPredicate(From<?, ?> root, CriteriaBuilder builder, String field, Object value) {
        if (value instanceof Timestamp) {
            Timestamp startOfDay = dateUtils.startOfDay((Timestamp) value);
            Timestamp endOfDay = dateUtils.endOfDay((Timestamp) value);
            return builder.and(
                    builder.greaterThanOrEqualTo(root.get(field), startOfDay),
                    builder.lessThan(root.get(field), endOfDay)
            );
        } else {
            if (value instanceof String) {
                value = normalize((String) value).toLowerCase();
                return builder.equal(builder.lower(root.get(field)), value);
            } else {
                return builder.equal(root.get(field), value);
            }
        }
    }

    private Predicate toInPredicate(From<?, ?> root, CriteriaBuilder builder, String field, Collection<?> values) {
        return builder.and(root.get(field).in(values));
    }

    private Predicate toLikePredicate(From<?, ?> root, CriteriaBuilder builder, String field, Object value) {
        return builder.like(builder.lower(root.get(field)), normalize(value.toString()).toLowerCase());
    }

    private Predicate toNotPredicate(From<?, ?> root, CriteriaBuilder builder, String field, Object value) {
        return builder.notEqual(root.get(field), value);
    }

    private Predicate toGtPredicate(From<?, ?> root, CriteriaBuilder builder, String field, Object value) {
        if (value instanceof Timestamp) {
            return builder.greaterThan(root.get(field), (Timestamp) value);
        }
        if (value instanceof Time) {
            return builder.greaterThan(root.get(field), (Time) value);
        }
        if (value instanceof Number) {
            return builder.gt(root.get(field), (Number) value);
        }
        return builder.greaterThan(root.get(field), value.toString());
    }

    private Predicate toGtePredicate(From<?, ?> root, CriteriaBuilder builder, String field, Object value) {
        if (value instanceof Timestamp) {
            return builder.greaterThanOrEqualTo(root.get(field), (Timestamp) value);
        }
        if (value instanceof Time) {
            return builder.greaterThanOrEqualTo(root.get(field), (Time) value);
        }
        if (value instanceof Number) {
            return builder.ge(root.get(field), (Number) value);
        }
        return builder.greaterThanOrEqualTo(root.get(field), value.toString());
    }

    private Predicate toLtPredicate(From<?, ?> root, CriteriaBuilder builder, String field, Object value) {
        if (value instanceof Timestamp) {
            return builder.lessThan(root.get(field), (Timestamp) value);
        }
        if (value instanceof Time) {
            return builder.lessThan(root.get(field), (Time) value);
        }
        if (value instanceof Number) {
            return builder.lt(root.get(field), (Number) value);
        }
        return builder.lessThan(root.get(field), value.toString());
    }

    private Predicate toLtePredicate(From<?, ?> root, CriteriaBuilder builder, String field, Object value) {
        if (value instanceof Timestamp) {
            return builder.lessThanOrEqualTo(root.get(field), (Timestamp) value);
        }
        if (value instanceof Time) {
            return builder.lessThanOrEqualTo(root.get(field), (Time) value);
        }
        if (value instanceof Number) {
            return builder.le(root.get(field), (Number) value);
        }
        return builder.lessThanOrEqualTo(root.get(field), value.toString());
    }

    private Predicate deletedFalsePredicate(From<?, ?> root, CriteriaBuilder builder) {
        try {
            return builder.equal(root.get("deleted"), false);
        } catch (Exception e) {
            return builder.and();
        }
    }

    private String cleanField(String field, PredicateTypeEnum predicateType) {
        return field.replaceAll(predicateType.alias() + "$", "");
    }

    private String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFC);
    }

    private boolean isCountQuery(CriteriaQuery<?> query) {
        return Long.class.isAssignableFrom(query.getResultType());
    }

}
