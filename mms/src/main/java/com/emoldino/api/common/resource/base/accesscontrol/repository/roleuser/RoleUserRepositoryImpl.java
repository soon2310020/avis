package com.emoldino.api.common.resource.base.accesscontrol.repository.roleuser;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

@Repository
public class RoleUserRepositoryImpl extends QuerydslRepositorySupport {
	public RoleUserRepositoryImpl() {
		super(RoleUser.class);
	}
}
