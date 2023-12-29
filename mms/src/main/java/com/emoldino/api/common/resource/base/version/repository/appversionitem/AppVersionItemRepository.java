package com.emoldino.api.common.resource.base.version.repository.appversionitem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

@Deprecated
public interface AppVersionItemRepository extends JpaRepository<AppVersionItem, Long>, QuerydslPredicateExecutor<AppVersionItem> {

}
