package vn.com.twendie.avis.api.adapter;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.response.ContractChangeHistoryItemDTO;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.data.enumtype.ContractExtendStatusEnum;
import vn.com.twendie.avis.data.enumtype.DriverGroupEnum;
import vn.com.twendie.avis.data.model.ContractChangeHistory;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.data.model.Vehicle;

import java.util.Objects;
import java.util.function.BiFunction;

@Component
public class ContractChangeHistoryDTOAdapter implements BiFunction<ContractChangeHistory, ContractChangeHistory, ContractChangeHistoryItemDTO> {

    private final BranchService branchService;
    private final ContractPeriodTypeService contractPeriodTypeService;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final RentalServiceTypeService rentalServiceTypeService;
    private final WorkingDayService workingDayService;
    private final FuelTypeService fuelTypeService;
    private final DateUtils dateUtils;

    public ContractChangeHistoryDTOAdapter(BranchService branchService,
                                           ContractPeriodTypeService contractPeriodTypeService,
                                           UserService userService,
                                           VehicleService vehicleService,
                                           RentalServiceTypeService rentalServiceTypeService,
                                           WorkingDayService workingDayService,
                                           FuelTypeService fuelTypeService,
                                           DateUtils dateUtils) {
        this.branchService = branchService;
        this.contractPeriodTypeService = contractPeriodTypeService;
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.rentalServiceTypeService = rentalServiceTypeService;
        this.workingDayService = workingDayService;
        this.fuelTypeService = fuelTypeService;
        this.dateUtils = dateUtils;
    }

    @Override
    public ContractChangeHistoryItemDTO apply(ContractChangeHistory contractChangeHistory, ContractChangeHistory previousChange) {
        ContractChangeHistoryItemDTO dto = ContractChangeHistoryItemDTO.builder()
                .createdAt(contractChangeHistory.getCreatedAt())
                .updatedBy(contractChangeHistory.getCreatedBy() != null ?
                        contractChangeHistory.getCreatedBy().getName() : "")
                .effectiveDate(contractChangeHistory.getFromDate())
                .fieldName(contractChangeHistory.getMappingFieldCodeFontend().getName())
                .build();
        switch (contractChangeHistory.getMappingFieldCodeFontend().getFieldName()) {
            case "branch_id":
                setBranchValue(dto, contractChangeHistory);
                break;
            case "contract_period_type_id":
                setContractPeriodTypeValue(dto, contractChangeHistory);
                break;
            case "driver_id":
                setDriverValue(dto, contractChangeHistory);
                break;
            case "vehicle_id":
                setVehicleValue(dto, contractChangeHistory);
                break;
            case "rental_service_type_id":
                setRentalServiceTypeValue(dto, contractChangeHistory, previousChange);
                break;
//            case "time_use_policy_group_id": Not yet
            case "working_time_weekend_holiday_from":
            case "working_time_weekend_holiday_to":
            case "working_time_from":
            case "working_time_to":
                setValueWithTimeUnitAndEffectiveDate(dto, contractChangeHistory, previousChange, DateUtils.HOUR_SHORT_PATTERN,
                        DateUtils.HOUR_SHORT_PATTERN, DateUtils.UTC_TIME_ZONE);
                break;
            case "sign_date":
            case "from_datetime":
            case "to_datetime":
            case "date_early_termination":
                setValueWithTimeUnit(dto, contractChangeHistory, DateUtils.MEDIUM_PATTERN,
                        DateUtils.SHORT_PATTERN, DateUtils.LOCAL_TIME_ZONE);
                break;
            case "working_day_id":
                setWorkingDayTypeValue(dto, contractChangeHistory, previousChange);
                break;
            case "fuel_type_id":
                setFuelTypeValue(dto, contractChangeHistory);
                break;
            case "return_vehicle_early":
            case "driver_know_english":
                convertBooleanValue(dto, contractChangeHistory);
                break;
            case "parking_id":
                setParkingValue(dto, contractChangeHistory);
                break;
            case "fuel_adjust_percent":
            case "working_day":
                setValueWithEffectiveDate(dto, contractChangeHistory, previousChange);
                break;
            case "extend_status":
                setExtendStatusValue(dto, contractChangeHistory);
                break;
            default:
                dto.setOldValue(convertMappingValue(contractChangeHistory.getOldValue(),
                        contractChangeHistory));
                dto.setNewValue(convertMappingValue(contractChangeHistory.getNewValue(),
                        contractChangeHistory));
        }
        return dto;
    }

    private String convertMappingValue(String value, ContractChangeHistory contractChangeHistory) {
        return StringUtils.isBlank(value) ? "" :
                value
                        + StringUtils.defaultIfBlank(" "
                        + StringUtils.defaultIfBlank(contractChangeHistory
                        .getMappingFieldCodeFontend().getUnit(), ""), "");
    }

    private void setValueWithEffectiveDate(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory, ContractChangeHistory previousChange) {

        if (!Objects.isNull(previousChange)
                && previousChange.getMappingFieldCodeFontend()
                .equals(contractChangeHistory.getMappingFieldCodeFontend())) {
            dto.setOldValue(convertMappingValue(previousChange.getNewValue(), previousChange));
        } else {
            dto.setOldValue(convertMappingValue(contractChangeHistory.getOldValue(),
                    contractChangeHistory));
        }

        dto.setNewValue(convertMappingValue(contractChangeHistory.getNewValue(),
                contractChangeHistory));
    }

    private void setParkingValue(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory) {
        dto.setOldValue(checkParkingValue(contractChangeHistory.getOldValue()));
        dto.setNewValue(checkParkingValue(contractChangeHistory.getNewValue()));
    }

    private void setExtendStatusValue(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory) {
        dto.setOldValue(ContractExtendStatusEnum.valueOf(Integer.parseInt(contractChangeHistory.getOldValue())).getName());
        dto.setNewValue(ContractExtendStatusEnum.valueOf(Integer.parseInt(contractChangeHistory.getNewValue())).getName());
    }

    private String checkParkingValue(String value) {
        return StringUtils.isBlank(value) ? ""
                : AvisApiConstant.PARKING_PLACE_MAP.get(Long.parseLong(value));
    }

    private void convertBooleanValue(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory) {
        dto.setOldValue(checkBooleanValue(contractChangeHistory.getOldValue()));
        dto.setNewValue(checkBooleanValue(contractChangeHistory.getNewValue()));

    }

    private String checkBooleanValue(String value) {
        return StringUtils.isNotBlank(value) ?
                AvisApiConstant.BooleanStringValue.TRUE.equals(value) ?
                        "Có" : "Không"
                : "";
    }

    private void setValueWithTimeUnitAndEffectiveDate(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory, ContractChangeHistory previousChange, String inputPattern,
                                      String outputPattern, String timezone) {

        if (!Objects.isNull(previousChange)
                && previousChange.getMappingFieldCodeFontend()
                .equals(contractChangeHistory.getMappingFieldCodeFontend())) {
            dto.setOldValue(dateUtils.dateWithTimeZone(previousChange.getNewValue(), inputPattern, outputPattern, timezone));
        } else {
            dto.setOldValue(dateUtils.dateWithTimeZone(contractChangeHistory.getOldValue(), inputPattern, outputPattern, timezone));
        }

        dto.setNewValue(dateUtils.dateWithTimeZone(contractChangeHistory.getNewValue(), inputPattern, outputPattern, timezone));
    }

    private void setValueWithTimeUnit(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory, String inputPattern,
                                      String outputPattern, String timezone) {

        dto.setOldValue(dateUtils.dateWithTimeZone(contractChangeHistory.getOldValue(), inputPattern, outputPattern, timezone));
        dto.setNewValue(dateUtils.dateWithTimeZone(contractChangeHistory.getNewValue(), inputPattern, outputPattern, timezone));
    }

    private void setFuelTypeValue(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory) {
        dto.setOldValue(fuelTypeService
                .findByIdIgnoreDelete(Long.valueOf(contractChangeHistory.getOldValue()))
                .getName());
        dto.setNewValue(fuelTypeService
                .findByIdIgnoreDelete(Long.valueOf(contractChangeHistory.getNewValue()))
                .getName());
    }

    private void setWorkingDayTypeValue(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory, ContractChangeHistory previousChange) {

        if (!Objects.isNull(previousChange)
                && previousChange.getMappingFieldCodeFontend()
                .equals(contractChangeHistory.getMappingFieldCodeFontend())) {
            dto.setOldValue(workingDayService
                    .findByIdIgnoreDelete(Long.valueOf(previousChange.getNewValue()))
                    .getName());
        } else {
            dto.setOldValue(workingDayService
                    .findByIdIgnoreDelete(Long.valueOf(contractChangeHistory.getOldValue()))
                    .getName());
        }

        dto.setNewValue(workingDayService
                .findByIdIgnoreDelete(Long.valueOf(contractChangeHistory.getNewValue()))
                .getName());
    }

    private void setRentalServiceTypeValue(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory, ContractChangeHistory previousChange) {

        if (!Objects.isNull(previousChange)
                && previousChange.getMappingFieldCodeFontend()
                .equals(contractChangeHistory.getMappingFieldCodeFontend())) {
            dto.setOldValue(rentalServiceTypeService
                    .findByIdIgnoreDelete(Long.valueOf(previousChange.getNewValue()))
                    .getName());
        } else {
            dto.setOldValue(rentalServiceTypeService
                    .findByIdIgnoreDelete(Long.valueOf(contractChangeHistory.getOldValue()))
                    .getName());
        }

        dto.setNewValue(rentalServiceTypeService
                .findByIdIgnoreDelete(Long.valueOf(contractChangeHistory.getNewValue()))
                .getName());
    }

    private void setVehicleValue(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory) {
        dto.setOldValue(checkVehicleValueAndReturn(contractChangeHistory.getOldValue()));
        dto.setNewValue(checkVehicleValueAndReturn(contractChangeHistory.getNewValue()));
    }

    private String checkVehicleValueAndReturn(String input) {
        if (StringUtils.isBlank(input)) {
            return "";
        } else {
            Vehicle vehicle = vehicleService
                    .findByIdIgnoreDelete(Long.valueOf(input));
            return vehicle.getNumberPlate();
        }
    }

    private void setDriverValue(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory) {
        dto.setOldValue(checkDriverValueAndReturn(contractChangeHistory.getOldValue()));
        dto.setNewValue(checkDriverValueAndReturn(contractChangeHistory.getNewValue()));
    }

    private String checkDriverValueAndReturn(String input) {
        if (StringUtils.isBlank(input)) {
            return "";
        } else {
            User user = userService
                    .findByIdIgnoreDelete(Long.valueOf(input));
            if (DriverGroupEnum.CUSTOMER.getId().equals(user.getUserGroup().getId())) {
                if (StringUtils.isBlank(user.getCountryCode()) || StringUtils.isBlank(user.getMobile())) {
                    return user.getName();
                } else {
                    return user.getName() + " " + user.getCountryCode() + user.getMobile();
                }
            } else {
                return user.getName() + " " + StringUtils.defaultIfBlank(user.getIdCard(), "");
            }
        }
    }


    private void setContractPeriodTypeValue(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory) {
        dto.setOldValue(contractPeriodTypeService
                .findByIdIgnoreDelete(Long.valueOf(contractChangeHistory.getOldValue()))
                .getName());
        dto.setNewValue(contractPeriodTypeService
                .findByIdIgnoreDelete(Long.valueOf(contractChangeHistory.getNewValue()))
                .getName());
    }

    private void setBranchValue(ContractChangeHistoryItemDTO dto, ContractChangeHistory contractChangeHistory) {
        dto.setOldValue(branchService
                .findByIdIgnoreDelete(Long.valueOf(contractChangeHistory.getOldValue()))
                .getName());
        dto.setNewValue(branchService
                .findByIdIgnoreDelete(Long.valueOf(contractChangeHistory.getNewValue()))
                .getName());
    }

}
