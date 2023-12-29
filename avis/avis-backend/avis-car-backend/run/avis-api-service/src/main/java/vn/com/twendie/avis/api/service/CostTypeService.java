package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.CodeValueModel;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.ContractCostType;
import vn.com.twendie.avis.data.model.CostType;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface CostTypeService {

    List<CostType> findAll();

    CostType findById(Long id);

    CostType findByCode(String code);

    List<ContractCostType> getContractCostTypes(Contract contract);

    BigDecimal getContractCost(Contract contract, String code);

    void fetchContractCostTypes(Collection<Contract> contracts);

    default List<ContractCostType> fetchContractCostTypes(Contract contract) {
        fetchContractCostTypes(Collections.singleton(contract));
        return contract.getContractCostTypes();
    }

    List<ContractCostType> saveAll(Collection<ContractCostType> contractCostTypes);

    List<ContractCostType> buildContractCostTypes(Contract contract, List<CodeValueModel> costs);

}
