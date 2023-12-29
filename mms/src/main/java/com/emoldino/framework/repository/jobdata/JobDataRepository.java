package com.emoldino.framework.repository.jobdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface JobDataRepository extends JpaRepository<JobData, Long>, QuerydslPredicateExecutor<JobData> {

}
