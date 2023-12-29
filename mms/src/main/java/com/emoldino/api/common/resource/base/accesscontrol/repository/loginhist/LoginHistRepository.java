package com.emoldino.api.common.resource.base.accesscontrol.repository.loginhist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginHistRepository extends JpaRepository<LoginHist, String>, QuerydslPredicateExecutor<LoginHist> {

}
