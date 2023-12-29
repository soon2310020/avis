package com.stg.repository;

import com.stg.entity.BaasPayOnBehalf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BaasPayOnBehalfRepository extends JpaRepository<BaasPayOnBehalf, Long> {

    BaasPayOnBehalf findBaasPayOnBehalvesByMbTransactionIdAndType(String mbTransactionId, String type);

    @Query(value = "from BaasPayOnBehalf bpob where bpob.status = 'FAIL' AND bpob.type = 'MIC' and bpob.version < 4")
    List<BaasPayOnBehalf> collectMicDataForRetryPay();

    @Query(value = "from BaasPayOnBehalf bpob where bpob.mbalHookStatus = false AND bpob.type = 'MBAL' and bpob.version < 4")
    List<BaasPayOnBehalf> collectMbalDataForRetryPay();

    @Query(value = "from BaasPayOnBehalf bpob where bpob.mbTransactionId = ?1 and  bpob.status = 'FAIL' AND bpob.type = 'MIC'")
    List<BaasPayOnBehalf> findBaasPayOnBehalvesByMbTransactionIdAndTypeAndStatus(String mbTransactionId);


}
