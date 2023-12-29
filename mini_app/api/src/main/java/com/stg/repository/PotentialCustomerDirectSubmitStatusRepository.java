package com.stg.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.entity.potentialcustomer.PotentialCustomerDirectSubmitStatus;

public interface PotentialCustomerDirectSubmitStatusRepository
        extends JpaRepository<PotentialCustomerDirectSubmitStatus, Long> {

    Optional<PotentialCustomerDirectSubmitStatus> findByPotentialCustomerDirect(PotentialCustomerDirect direct);

}
