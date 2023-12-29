package com.emoldino.api.analysis.resource.base.data.repository.transfermessage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface TransferMessageRepository extends JpaRepository<TransferMessage, Long>, QuerydslPredicateExecutor<TransferMessage> {

}
