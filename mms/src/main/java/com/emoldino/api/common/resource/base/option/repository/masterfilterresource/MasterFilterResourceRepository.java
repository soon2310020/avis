package com.emoldino.api.common.resource.base.option.repository.masterfilterresource;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterResourceType;

public interface MasterFilterResourceRepository extends JpaRepository<MasterFilterResource, Long>, QuerydslPredicateExecutor<MasterFilterResource> {

	List<MasterFilterResource> findAllByFilterCodeAndUserIdOrderByPosition(String filterCode, Long userId);

	Optional<MasterFilterResource> findByFilterCodeAndUserIdAndResourceType(String filterCode, Long userId, MasterFilterResourceType resourceType);

	Integer deleteAllByFilterCodeAndUserIdAndResourceType(String filterCode, Long userId, MasterFilterResourceType resourceType);

	Integer deleteAllByFilterCodeAndUserId(String filterCode, Long userId);

}
