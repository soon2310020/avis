package com.stg.repository.search;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.expression.LiteralExpression;
import org.hibernate.query.criteria.internal.expression.function.ParameterizedFunctionExpression;
import org.hibernate.query.criteria.internal.predicate.LikePredicate;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class SearchConcatCriteria extends SearchCriteria {
    private List<String> keys;

    public SearchConcatCriteria(List<String> keys, Object value) {
        super(null, value, null);
        this.keys = keys;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Predicate getPredicate(Root<?> root, CriteriaBuilderImpl builderImpl, Map<String, From<?, ?>> alias) {
        List<Expression<String>> expressions = new ArrayList<>();

        for (String s : keys) {
            if (" ".equals(s)) {
                expressions.add(new LiteralExpression<>(builderImpl, s));
            } else {
                expressions.add(builderImpl.lower(paserPath(root, s, alias).as(String.class)));
            }
        }

        return new LikePredicate(builderImpl,
                new ParameterizedFunctionExpression(builderImpl, String.class, "CONCAT", expressions),
                "%" + escape().toLowerCase() + "%");
    }
}
