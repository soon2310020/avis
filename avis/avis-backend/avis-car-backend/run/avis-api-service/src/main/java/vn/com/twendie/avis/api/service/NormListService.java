package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.CodeValueModel;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.ContractNormList;
import vn.com.twendie.avis.data.model.NormList;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface NormListService {

    List<NormList> findAll();

    NormList findById(Long id);

    NormList findByCode(String code);

    List<ContractNormList> getContractNormLists(Contract contract);

    BigDecimal getContractNorm(Contract contract, String code);

    void fetchContractNormLists(Collection<Contract> contracts);

    default List<ContractNormList> fetchContractNormLists(Contract contract) {
        fetchContractNormLists(Collections.singleton(contract));
        return contract.getContractNormLists();
    }

    List<ContractNormList> saveAll(Collection<ContractNormList> contractNormLists);

    List<ContractNormList> buildContractNormList(Contract contract, List<CodeValueModel> norms);

}
