package vn.com.twendie.avis.mobile.api.validation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import vn.com.twendie.avis.api.core.util.JsonUtils;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.data.enumtype.JourneyDiaryStepEnum;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.mobile.api.model.payload.ConfirmStationFeesPayload;
import vn.com.twendie.avis.mobile.api.model.payload.JourneyDiaryCostPayload;
import vn.com.twendie.avis.mobile.api.model.payload.JourneyDiaryPayload;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

import static vn.com.twendie.avis.data.enumtype.ContractStatusEnum.IN_PROGRESS;
import static vn.com.twendie.avis.data.enumtype.ContractTypeEnum.WITH_DRIVER;
import static vn.com.twendie.avis.data.enumtype.JourneyDiaryStepEnum.*;

@Service
public class JourneyDiaryValidator {

    private final JsonUtils jsonUtils;
    private final LocalValidatorFactoryBean validator;

    public JourneyDiaryValidator(JsonUtils jsonUtils,
                                 LocalValidatorFactoryBean validator) {
        this.jsonUtils = jsonUtils;
        this.validator = validator;
    }

    public JourneyDiaryPayload validateJourneyDiaryPayload(String journeyDiaryPayloadStr, Class<?>... groups) {
        JourneyDiaryPayload journeyDiaryPayload;
        try {
            journeyDiaryPayload = jsonUtils.toObject(journeyDiaryPayloadStr, JourneyDiaryPayload.class);
        } catch (IOException e) {
            throw new BadRequestException("Can not parse json")
                    .displayMessage(Translator.toLocale("error.invalid_input"));
        }
        Set<ConstraintViolation<JourneyDiaryPayload>> violations = validator.validate(journeyDiaryPayload, groups);
        if (!CollectionUtils.isEmpty(violations)) {
            throw new ConstraintViolationException(violations);
        }
        return journeyDiaryPayload;
    }

    public void validateDriverInContract(User user) {
        if (Objects.isNull(user.getCurrentContractId())) {
            throw new BadRequestException("Current contract is null")
                    .code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("contract.has_no_contract"));
        }
    }

    public void validateContractInProgress(Contract contract) {
        if (Objects.isNull(contract)) {
            throw new BadRequestException("Current contract is null")
                    .code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("contract.has_no_contract"));
        } else if (!IN_PROGRESS.getCode().equals(contract.getStatus())) {
            throw new BadRequestException("Contract status is: " + contract.getStatus())
                    .code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("contract.contract_not_in_progress"));
        } else if (contract.getDriverIsTransferredAnother() || contract.getVehicleIsTransferredAnother()) {
            throw new BadRequestException("Contract is paused")
                    .code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("contract.contract_postpone"));
        }
    }

    public boolean checkCurrentJourneyDiary(JourneyDiary currentJourneyDiary,
                                            JourneyDiaryPayload journeyDiaryPayload,
                                            JourneyDiaryStepEnum step) {
        switch (step) {
            case STARTED:
                return Objects.isNull(currentJourneyDiary);

            case CUSTOMER_GOT_IN:
                return Objects.nonNull(currentJourneyDiary) &&
                        currentJourneyDiary.getId().equals(journeyDiaryPayload.getId()) &&
                        currentJourneyDiary.getStep().equals(STARTED.value());

            case CUSTOMER_GOT_OUT:
                return Objects.nonNull(currentJourneyDiary) &&
                        currentJourneyDiary.getId().equals(journeyDiaryPayload.getId()) &&
                        currentJourneyDiary.getStep().equals(CUSTOMER_GOT_IN.value());

            case BREAKDOWN:
                return Objects.nonNull(currentJourneyDiary) &&
                        Objects.equals(currentJourneyDiary.getId(), journeyDiaryPayload.getId());

            case FINISHED:
                if (Objects.nonNull(currentJourneyDiary) && currentJourneyDiary.getId().equals(journeyDiaryPayload.getId())) {
                    Contract contract = currentJourneyDiary.getContract();
                    JourneyDiaryStepEnum lastStep = contract.getContractType().getId().equals(WITH_DRIVER.value()) ?
                            CUSTOMER_GOT_OUT : STARTED;
                    return currentJourneyDiary.getStep().equals(lastStep.value());
                }

            default:
                return false;
        }
    }

    public void validateKmDriver(JourneyDiary currentJourneyDiary,
                                 JourneyDiaryPayload journeyDiaryPayload,
                                 JourneyDiaryStepEnum step) {
        boolean isValid = true;
        BigDecimal kmDriver;
        switch (step) {
            case CUSTOMER_GOT_IN:
                kmDriver = journeyDiaryPayload.getKmDriverCustomerGetIn();
                break;
            case CUSTOMER_GOT_OUT:
                kmDriver = journeyDiaryPayload.getKmDriverCustomerGetOut();
                break;
            case BREAKDOWN:
            case FINISHED:
                kmDriver = journeyDiaryPayload.getKmDriverEnd();
                break;
            default:
                return;
        }
        if (Objects.isNull(kmDriver)) {
            return;
        }

        if (step.value() > STARTED.value() &&
                Objects.nonNull(currentJourneyDiary.getKmDriverStart()) &&
                kmDriver.intValueExact() < currentJourneyDiary.getKmDriverStart().intValueExact()) {
            isValid = false;
        }
        if (step.value() > CUSTOMER_GOT_IN.value() &&
                Objects.nonNull(currentJourneyDiary.getKmDriverCustomerGetIn()) &&
                kmDriver.intValueExact() < currentJourneyDiary.getKmDriverCustomerGetIn().intValueExact()) {
            isValid = false;
        }
        if (step.value() > CUSTOMER_GOT_OUT.value() &&
                Objects.nonNull(currentJourneyDiary.getKmDriverCustomerGetOut()) &&
                kmDriver.intValueExact() < currentJourneyDiary.getKmDriverCustomerGetOut().intValueExact()) {
            isValid = false;
        }

        if (!isValid) {
            throw new BadRequestException("kmDriver must not be less than previous step")
                    .displayMessage(Translator.toLocale("journey_diary.km_driver_less_than_previous_step"));
        }
    }

    public void validateContractWithDriver(JourneyDiary currentJourneyDiary) {
        Contract contract = Objects.nonNull(currentJourneyDiary) ? currentJourneyDiary.getContract() : null;
        if (Objects.nonNull(contract) && !contract.getContractType().getId().equals(WITH_DRIVER.value())) {
            throw new BadRequestException("Only support contract with driver")
                    .displayMessage(Translator.toLocale("journey_diary.only_support_contract_with_driver"));
        }
    }

    public JourneyDiaryCostPayload validateJourneyDiaryCostPayload(String journeyDiaryCostPayloadString, Class<?>... groups) {
        JourneyDiaryCostPayload journeyDiaryCostPayload;
        try {
            journeyDiaryCostPayload = jsonUtils.toObject(journeyDiaryCostPayloadString, JourneyDiaryCostPayload.class);
        } catch (IOException e) {
            throw new BadRequestException("Can not parse json")
                    .displayMessage(Translator.toLocale("error.invalid_input"));
        }
        Set<ConstraintViolation<JourneyDiaryCostPayload>> violations = validator.validate(journeyDiaryCostPayload, groups);
        if (!CollectionUtils.isEmpty(violations)) {
            throw new ConstraintViolationException(violations);
        }
        return journeyDiaryCostPayload;
    }

    public ConfirmStationFeesPayload validateConfirmStationFeesPayload(String confirmStationFeesPayloadString) {
        ConfirmStationFeesPayload confirmStationFeesPayload;
        try {
            confirmStationFeesPayload = jsonUtils.toObject(confirmStationFeesPayloadString, ConfirmStationFeesPayload.class);
        } catch (IOException e) {
            throw new BadRequestException(e.getMessage())
                    .displayMessage(Translator.toLocale("error.invalid_input"));
        }
        Set<ConstraintViolation<ConfirmStationFeesPayload>> violations = validator.validate(confirmStationFeesPayload);
        if (!CollectionUtils.isEmpty(violations)) {
            throw new ConstraintViolationException(violations);
        }
        return confirmStationFeesPayload;
    }

}
