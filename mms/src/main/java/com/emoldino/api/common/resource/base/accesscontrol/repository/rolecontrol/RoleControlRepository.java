package com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleControlRepository extends JpaRepository<RoleControl, Long>, QuerydslPredicateExecutor<RoleControl> {
	RoleControl findByAuthority(String authority);
}
