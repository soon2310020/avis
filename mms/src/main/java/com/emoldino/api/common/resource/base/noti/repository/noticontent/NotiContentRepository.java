package com.emoldino.api.common.resource.base.noti.repository.noticontent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface NotiContentRepository extends JpaRepository<NotiContent, Long>, QuerydslPredicateExecutor<NotiContent> {

}
