package com.emoldino.api.common.resource.base.noti.repository.notirecipientconfig;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface NotiRecipientConfigRepository extends JpaRepository<NotiRecipientConfig, Long>, QuerydslPredicateExecutor<NotiRecipientConfig> {

}
