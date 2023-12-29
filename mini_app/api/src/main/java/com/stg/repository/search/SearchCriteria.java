package com.stg.repository.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.predicate.ComparisonPredicate;
import org.hibernate.query.criteria.internal.predicate.ComparisonPredicate.ComparisonOperator;
import org.hibernate.query.criteria.internal.predicate.InPredicate;
import org.hibernate.query.criteria.internal.predicate.LikePredicate;
import org.hibernate.query.criteria.internal.predicate.NullnessPredicate;
import org.springframework.data.jpa.repository.query.EscapeCharacter;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class SearchCriteria {

    private String key;
    private Object value;
    private SearchOperation operation;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Predicate getPredicate(Root<?> root, CriteriaBuilderImpl builderImpl, Map<String, From<?, ?>> alias) {

        Path<?> expression = paserPath(root, key, alias);

        if (operation.equals(SearchOperation.GREATER_THAN)) {
            return new ComparisonPredicate(builderImpl, ComparisonOperator.GREATER_THAN, expression, value);
        } else if (operation.equals(SearchOperation.LESS_THAN)) {
            return new ComparisonPredicate(builderImpl, ComparisonOperator.LESS_THAN, expression, value);
        } else if (operation.equals(SearchOperation.GREATER_THAN_EQUAL)) {
            return new ComparisonPredicate(builderImpl, ComparisonOperator.GREATER_THAN_OR_EQUAL, expression, value);
        } else if (operation.equals(SearchOperation.LESS_THAN_EQUAL)) {
            return new ComparisonPredicate(builderImpl, ComparisonOperator.LESS_THAN_OR_EQUAL, expression, value);
        } else if (operation.equals(SearchOperation.NOT_EQUAL)) {
            if (value == null) {
                return new NullnessPredicate(builderImpl, expression).not();
            }
            return new ComparisonPredicate(builderImpl, ComparisonOperator.NOT_EQUAL, expression, value);
        } else if (operation.equals(SearchOperation.EQUAL)) {
            if (value == null) {
                return new NullnessPredicate(builderImpl, expression);
            }
            return new ComparisonPredicate(builderImpl, ComparisonOperator.EQUAL, expression, value);
        } else if (operation.equals(SearchOperation.MATCH)) {
            return new LikePredicate(builderImpl, builderImpl.lower(expression.as(String.class)),
                    "%" + escape().toLowerCase() + "%");
        } else if (operation.equals(SearchOperation.MATCH_END)) {
            return new LikePredicate(builderImpl, builderImpl.lower(expression.as(String.class)),
                    escape().toLowerCase() + "%");
        } else if (operation.equals(SearchOperation.IN)) {
            InPredicate predicate = new InPredicate<>(builderImpl, expression);
            List<?> tmp = (List<?>) value;
            tmp.stream().forEach(v -> predicate.value(v));
            return predicate;
        }  else if (operation.equals(SearchOperation.NOT_IN)) {
            InPredicate predicate = new InPredicate<>(builderImpl, expression);
            List<?> tmp = (List<?>) value;
            tmp.stream().forEach(v -> predicate.value(v));
            return predicate.not();
        }

        return builderImpl.and(new Predicate[] {});
    }

    protected Path<?> paserPath(Root<?> root, String s, Map<String, From<?, ?>> alias) {

        String[] keys = new String[] { s };

        if (s.indexOf(".") != -1) {
            keys = s.split("\\.");
        }

        Path<?> expression = alias.get(keys[0]);

        if (expression == null) {
            expression = root.get(keys[0]);
        }

        for (int i = 1; i < keys.length; i++) {
            expression = expression.get(keys[i]);
        }

        return expression;
    }

    protected String escape() {
        return EscapeCharacter.DEFAULT.escape(value.toString());
    }
}
