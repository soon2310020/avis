package com.emoldino.api.common.resource.base.option.repository.maserfilteritem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterResourceType;

public interface MasterFilterItemRepository extends JpaRepository<MasterFilterItem, Long>, QuerydslPredicateExecutor<MasterFilterItem> {

	List<MasterFilterItem> findAllByFilterCodeAndUserIdAndResourceType(String filterCode, Long userId, MasterFilterResourceType resourceType);

	Integer deleteAllByFilterCodeAndUserIdAndResourceType(String filterCode, Long userId, MasterFilterResourceType resourceType);

	Integer deleteAllByFilterCodeAndUserId(String filterCode, Long userId);

}
