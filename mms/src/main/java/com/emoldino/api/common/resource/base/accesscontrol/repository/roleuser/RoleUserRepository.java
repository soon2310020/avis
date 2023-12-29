package com.emoldino.api.common.resource.base.accesscontrol.repository.roleuser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleUserRepository extends JpaRepository<RoleUser, Long>, QuerydslPredicateExecutor<RoleUser> {

}
