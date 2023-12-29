package com.stg.repository.sale;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stg.entity.sale.IcInfomation;

public interface IcInformationRepository extends JpaRepository<IcInfomation, Long> {
    
    List<IcInfomation> findAllByIcCode(String icCode);

    List<IcInfomation> findAllByBranchCodeAndIcCodeNot(String branchCode, String icCode);
    
    Optional<IcInfomation> findByBranchCodeAndIcCode(String branchCode, String icCode);
    
}
