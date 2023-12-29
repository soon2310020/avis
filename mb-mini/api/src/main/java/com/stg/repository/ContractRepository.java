package com.stg.repository;

import com.stg.entity.InsuranceContract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<InsuranceContract, Long> {
}
