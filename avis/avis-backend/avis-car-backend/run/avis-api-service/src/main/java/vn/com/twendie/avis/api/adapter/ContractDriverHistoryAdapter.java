package vn.com.twendie.avis.api.adapter;

import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.data.enumtype.DriverContractStatusEnum;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.ContractDriverHistory;

import java.util.function.Function;

public class ContractDriverHistoryAdapter implements Function<Contract, ContractDriverHistory> {

    private final DateUtils dateUtils;

    public ContractDriverHistoryAdapter(DateUtils dateUtils) {
        this.dateUtils = dateUtils;
    }

    @Override
    public ContractDriverHistory apply(Contract contract) {
        return ContractDriverHistory.builder()
                .contract(contract)
                .driver(contract.getDriver())
                .fromDate(dateUtils.now())
                .toDate(contract.getToDatetime())
                .status(DriverContractStatusEnum.CONTINUE.code())
                .build();
    }

}
