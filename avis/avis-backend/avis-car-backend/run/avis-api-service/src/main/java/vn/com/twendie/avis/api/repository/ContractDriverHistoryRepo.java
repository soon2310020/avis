package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.ContractDriverHistory;

public interface ContractDriverHistoryRepo extends JpaRepository<ContractDriverHistory, Long> {
}
