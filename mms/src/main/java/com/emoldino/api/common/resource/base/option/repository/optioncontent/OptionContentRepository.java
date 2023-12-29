package com.emoldino.api.common.resource.base.option.repository.optioncontent;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface OptionContentRepository extends JpaRepository<OptionContent, Long>, QuerydslPredicateExecutor<OptionContent> {

	Optional<OptionContent> findByOptionNameAndUserId(String optionName, Long userId);

}
