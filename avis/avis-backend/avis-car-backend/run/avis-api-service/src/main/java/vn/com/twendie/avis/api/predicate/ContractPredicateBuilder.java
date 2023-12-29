package vn.com.twendie.avis.api.predicate;

import vn.com.twendie.avis.data.enumtype.ContractStatusEnum;
import vn.com.twendie.avis.data.model.BaseModel;
import vn.com.twendie.avis.data.model.Contract;

import java.util.Arrays;
import java.util.function.Predicate;

public class ContractPredicateBuilder {

    public static Predicate<Contract> not(Predicate<Contract> predicate) {
        return predicate.negate();
    }

    public static Predicate<Contract> inStatus(ContractStatusEnum... status) {
        return contract -> Arrays.stream(status)
                .anyMatch(value -> value.getCode().equals(contract.getStatus()));
    }

    public static Predicate<Contract> lendingDriver() {
        return Contract::getDriverIsTransferredAnother;
    }

    public static Predicate<Contract> lendingVehicle() {
        return Contract::getVehicleIsTransferredAnother;
    }

    public static Predicate<Contract> deleted() {
        return BaseModel::isDeleted;
    }

}
