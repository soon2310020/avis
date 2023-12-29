package vn.com.twendie.avis.api.validation;

import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.service.ContractService;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.locale.config.Translator;

import java.sql.Timestamp;
import java.util.Objects;

import static vn.com.twendie.avis.data.enumtype.CommonStatusEnum.DISABLE;
import static vn.com.twendie.avis.data.enumtype.ContractStatusEnum.CANCELED;
import static vn.com.twendie.avis.data.enumtype.ContractStatusEnum.FINISHED;
import static vn.com.twendie.avis.data.enumtype.ContractTypeEnum.WITHOUT_DRIVER;
import static vn.com.twendie.avis.data.enumtype.CustomerTypeEnum.ENTERPRISE;
import static vn.com.twendie.avis.data.enumtype.VehicleStatusEnum.UNAVAILABLE;

@Service
public class ContractValidator {

    private final ContractService contractService;

    public ContractValidator(ContractService contractService) {
        this.contractService = contractService;
    }

    public void validateContractStatusCanEdit(Contract contract) {
        if (contractService.inStatus(contract, CANCELED, FINISHED)) {
            throw new BadRequestException("Can not edit contract")
                    .displayMessage(Translator.toLocale("contract.can_not_edit"));
        }
    }

    public void validateCustomerInfo(Customer customer, MemberCustomer memberCustomer) {
        if (Objects.isNull(memberCustomer) && ENTERPRISE.value().equals(customer.getCustomerType().getId())) {
            throw new BadRequestException("Invalid customer info")
                    .displayMessage(Translator.toLocale("contract.invalid_customer"));
        }
        MemberCustomer parent = Objects.nonNull(memberCustomer) ? memberCustomer.getParent() : null;
        if ((Objects.nonNull(customer) && !customer.isActive()) ||
                Objects.nonNull(parent) && !parent.isActive() ||
                Objects.nonNull(memberCustomer) && !memberCustomer.isActive()) {
            throw new BadRequestException("Invalid customer info")
                    .displayMessage(Translator.toLocale("contract.inactive_customer"));
        }
    }

    public void validateEffectiveDate(Contract contract, Timestamp effectiveDate) {
        if (effectiveDate.after(contract.getToDatetime())) {
            throw new BadRequestException("effective_date can not be after to_datetime")
                    .displayMessage(Translator.toLocale("contract.invalid_effective_date"));
        }
    }

    public void validateDriverNotInJourneyDiary(User driver) {
        if (Objects.nonNull(driver) && Objects.nonNull(driver.getCurrentJourneyDiaryId())) {
            throw new BadRequestException("Driver is current in another journey diary")
                    .displayMessage(Translator.toLocale("driver.driver_is_current_in_journey_diary"));
        }
    }

    public void validateVehicleNotInJourneyDiary(Vehicle vehicle) {
        if (Objects.nonNull(vehicle) && Objects.nonNull(vehicle.getCurrentJourneyDiaryId())) {
            throw new BadRequestException("Vehicle is current in another journey diary")
                    .displayMessage(Translator.toLocale("vehicle.vehicle_is_current_in_journey_diary"));
        }
    }

    public void validateDriverAvailable(User driver) {
        if (Objects.nonNull(driver) && DISABLE.value().equals(driver.getStatus())) {
            throw new BadRequestException("Driver is unavailable")
                    .displayMessage(Translator.toLocale("driver.driver_is_unavailable"));
        }
    }

    public void validateVehicleAvailable(Vehicle vehicle) {
        if (Objects.nonNull(vehicle) && UNAVAILABLE.getValue().equals(vehicle.getStatus())) {
            throw new BadRequestException("Vehicle is unavailable")
                    .displayMessage(Translator.toLocale("vehicle.vehicle_is_unavailable"));
        }
    }

    public void validateNoContractLendingDriver(User driver) {
        if (Objects.nonNull(driver) && Objects.nonNull(driver.getLendingContractId())) {
            throw new BadRequestException("Driver is being lent by another contract")
                    .displayMessage(Translator.toLocale("driver.driver_is_being_lent"));
        }
    }

    public void validateNoContractLendingVehicle(Vehicle vehicle) {
        if (Objects.nonNull(vehicle) && Objects.nonNull(vehicle.getLendingContractId())) {
            throw new BadRequestException("Vehicle is being lent by another contract")
                    .displayMessage(Translator.toLocale("vehicle.vehicle_is_being_lent"));
        }
    }

    public void validateDriverNotInContract(User driver) {
        if (Objects.nonNull(driver) && Objects.nonNull(driver.getCurrentContractId())) {
            throw new BadRequestException("Driver is current in contract")
                    .displayMessage(Translator.toLocale("driver.driver_is_current_in_contract"));
        }
    }

    public void validateVehicleNotInContract(Vehicle vehicle) {
        if (Objects.nonNull(vehicle) && Objects.nonNull(vehicle.getCurrentContractId())) {
            throw new BadRequestException("Vehicle is current in contract")
                    .displayMessage(Translator.toLocale("vehicle.vehicle_is_current_in_contract"));
        }
    }

    public void validateVehicleNotInContractWithoutDriver(Vehicle vehicle) {
        if (Objects.nonNull(vehicle) && Objects.nonNull(vehicle.getCurrentContract())
                && vehicle.getCurrentContract().getContractType().getId().equals(WITHOUT_DRIVER.value())) {
            throw new BadRequestException("Vehicle is current in contract")
                    .displayMessage(Translator.toLocale("vehicle.vehicle_is_current_in_contract_without_driver"));
        }
    }
}
