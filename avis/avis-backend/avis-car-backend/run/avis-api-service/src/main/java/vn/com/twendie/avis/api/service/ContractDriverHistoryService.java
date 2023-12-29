package vn.com.twendie.avis.api.service;

import org.javatuples.Quartet;
import vn.com.twendie.avis.data.enumtype.DriverContractStatusEnum;
import vn.com.twendie.avis.data.enumtype.NotificationContentEnum;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.ContractDriverHistory;
import vn.com.twendie.avis.data.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ContractDriverHistoryService {

    void save(ContractDriverHistory contractDriverHistory);

    void saveAll(Collection<ContractDriverHistory> contractDriverHistories);

    ContractDriverHistory continueContract(Contract contract, User driver);

    ContractDriverHistory pauseContract(Contract contract, User driver);

    ContractDriverHistory buildContractDriverHistory(Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum> quartet);

    List<ContractDriverHistory> buildContractDriverHistories(Collection<Quartet<Contract, User, DriverContractStatusEnum, NotificationContentEnum>> quartets);
}
