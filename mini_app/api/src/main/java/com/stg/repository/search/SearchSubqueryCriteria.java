package com.stg.repository.search;

import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Subquery;

public abstract class SearchSubqueryCriteria {

    public abstract Subquery<?> getQuery(CriteriaQuery<?> query, CriteriaBuilderImpl builder);

}
