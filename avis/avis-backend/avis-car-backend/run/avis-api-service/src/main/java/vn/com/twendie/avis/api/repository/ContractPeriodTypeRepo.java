package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.ContractPeriodType;

public interface ContractPeriodTypeRepo extends JpaRepository<ContractPeriodType, Long> {
}
