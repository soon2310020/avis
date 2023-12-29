package com.emoldino.api.common.resource.base.noti.repository.notirecipient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface NotiRecipientRepository extends JpaRepository<NotiRecipient, Long>, QuerydslPredicateExecutor<NotiRecipient> {

}
