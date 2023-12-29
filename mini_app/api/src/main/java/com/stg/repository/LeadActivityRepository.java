package com.stg.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stg.entity.lead.LeadActivity;

public interface LeadActivityRepository extends JpaRepository<LeadActivity, Long> {

}
