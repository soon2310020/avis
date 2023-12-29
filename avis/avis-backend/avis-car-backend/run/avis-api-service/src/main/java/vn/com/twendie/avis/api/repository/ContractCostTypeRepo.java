package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.ContractCostType;
import vn.com.twendie.avis.data.model.ContractCostTypeId;

import java.util.Collection;
import java.util.List;

public interface ContractCostTypeRepo extends JpaRepository<ContractCostType, ContractCostTypeId> {

    List<ContractCostType> findAllByIdContractId(Long contractId);

    List<ContractCostType> findAllByIdContractIdIn(Collection<Long> contractIds);

}
