package com.emoldino.api.common.resource.base.code.repository.codedata;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeDataRepository extends JpaRepository<CodeData, Long>, QuerydslPredicateExecutor<CodeData> {

	Optional<CodeData> findByCodeTypeAndCode(String codeType, String code);	

	Optional<CodeData> findByCodeTypeAndCodeAndCompanyId(String codeType, String code, Long companyId);

}