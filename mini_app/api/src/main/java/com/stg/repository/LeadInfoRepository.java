package com.stg.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stg.entity.lead.LeadInfo;

public interface LeadInfoRepository extends JpaRepository<LeadInfo, Long> {

    Optional<LeadInfo> findByLeadId(String leadId);
    
}
