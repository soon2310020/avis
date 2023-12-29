package vn.com.twendie.avis.api.predicate;

import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.compile.RenderingContext;
import org.hibernate.query.criteria.internal.predicate.LikePredicate;

import javax.persistence.criteria.Expression;

public class CustomLikePredicate extends LikePredicate {

    public CustomLikePredicate(CriteriaBuilderImpl criteriaBuilder, Expression<String> matchExpression, String pattern) {
        super(criteriaBuilder, matchExpression, pattern);
    }

    @Override
    public String render(boolean isNegated, RenderingContext renderingContext) {
        return super.render(isNegated, renderingContext) + "  collate utf8mb4_la_0900_ai_ci";
    }
}
