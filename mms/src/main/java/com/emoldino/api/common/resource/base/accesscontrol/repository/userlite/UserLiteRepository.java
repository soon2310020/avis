package com.emoldino.api.common.resource.base.accesscontrol.repository.userlite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLiteRepository extends JpaRepository<UserLite, Long>, QuerydslPredicateExecutor<UserLite> {

}
