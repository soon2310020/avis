package com.stg.repository.sale;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stg.entity.sale.RmInformation;

public interface RmInformationRepository extends JpaRepository<RmInformation, Long> {

    Optional<RmInformation> findByRmCode(String rmCode);
    
}
