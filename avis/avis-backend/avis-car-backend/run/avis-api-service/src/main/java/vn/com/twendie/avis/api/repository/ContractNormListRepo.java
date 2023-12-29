package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.ContractNormList;
import vn.com.twendie.avis.data.model.ContractNormListId;

import java.util.Collection;
import java.util.List;

public interface ContractNormListRepo extends JpaRepository<ContractNormList, ContractNormListId> {

    List<ContractNormList> findAllByIdContractId(Long contractId);

    List<ContractNormList> findAllByIdContractIdIn(Collection<Long> contractIds);

}
