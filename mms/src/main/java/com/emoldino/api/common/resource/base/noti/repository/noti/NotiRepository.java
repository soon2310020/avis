package com.emoldino.api.common.resource.base.noti.repository.noti;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface NotiRepository extends JpaRepository<Noti, Long>, QuerydslPredicateExecutor<Noti> {

}
