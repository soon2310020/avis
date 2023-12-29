package com.stg.repository;

import com.stg.repository.search.Join;
import com.stg.repository.search.SearchCriteria;
import com.stg.repository.search.SearchCriterias;
import com.stg.repository.search.SearchSubqueryCriteria;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenericSpecification<T> implements Specification<T> {

    private static final long serialVersionUID = 1L;

    private final List<Join> joins;

    private final List<SearchCriterias> list;

    private final Map<String, From<?, ?>> alias;

    private boolean distinct = false;

    public GenericSpecification() {
        this.list = new ArrayList<>();
        this.joins = new ArrayList<>();
        this.alias = new HashMap<>();
    }

    public void add(Join join) {
        joins.add(join);
    }

    public void add(SearchCriteria criteria) {
        list.add(new SearchCriterias(List.of(criteria)));
    }

    public void add(SearchCriterias criteria) {
        list.add(criteria);
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        List<Predicate> predicates = new ArrayList<>();

        joins.forEach(j -> {
            if (j.getRefer() != null) {
                alias.put(j.getAlias(), alias.get(j.getRefer()).join(j.getTable()));
            } else {
                alias.put(j.getAlias(), root.join(j.getTable()));
            }
        });

        query.distinct(distinct);

        if (builder instanceof CriteriaBuilderImpl) {
            CriteriaBuilderImpl builderImpl = (CriteriaBuilderImpl) builder;

            predicates = list.stream().map(e -> {
                return builder.or(e.getCriterias().stream().map(c -> {
                    if (c.getValue() instanceof SearchSubqueryCriteria) {
                        c.setValue(((SearchSubqueryCriteria) c.getValue()).getQuery(query, builderImpl));
                    }
                    return c.getPredicate(root, builderImpl, alias);
                }).collect(Collectors.toList()).toArray(new Predicate[0]));
            }).collect(Collectors.toList());
        }

        return builder.and(predicates.toArray(new Predicate[0]));
    }

}
