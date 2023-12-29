package vn.com.twendie.avis.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import vn.com.twendie.avis.api.adapter.VehicleDTOAdapter;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.core.util.PageUtils;
import vn.com.twendie.avis.api.core.util.ValidObjectUtil;
import vn.com.twendie.avis.api.core.util.ValidUtils;
import vn.com.twendie.avis.api.model.payload.VehiclePayload;
import vn.com.twendie.avis.api.model.payload.VehicleSuggestionPayload;
import vn.com.twendie.avis.api.model.projection.VehicleProjection;
import vn.com.twendie.avis.api.model.response.*;
import vn.com.twendie.avis.api.repository.VehicleRepo;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.ContractChangeHistory;
import vn.com.twendie.avis.data.model.Vehicle;
import vn.com.twendie.avis.locale.config.Translator;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.VEHICLE_ID;
import static vn.com.twendie.avis.data.enumtype.VehicleStatusEnum.*;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepo vehicleRepo;

    private final BranchService branchService;
    private final UserService userService;
    private final FuelTypeGroupService fuelTypeGroupService;
    private final VehicleSupplierGroupService vehicleSupplierGroupService;
    private final ContractChangeHistoryService contractChangeHistoryService;

    private final ModelMapper modelMapper;
    private final VehicleDTOAdapter vehicleDTOAdapter;

    private final ListUtils listUtils;

    public VehicleServiceImpl(VehicleRepo vehicleRepo,
                              BranchService branchService,
                              UserService userService,
                              FuelTypeGroupService fuelTypeGroupService,
                              VehicleSupplierGroupService vehicleSupplierGroupService,
                              @Lazy ContractChangeHistoryService contractChangeHistoryService,
                              ModelMapper modelMapper,
                              VehicleDTOAdapter vehicleDTOAdapter,
                              ListUtils listUtils) {
        this.vehicleRepo = vehicleRepo;
        this.branchService = branchService;
        this.userService = userService;
        this.fuelTypeGroupService = fuelTypeGroupService;
        this.vehicleSupplierGroupService = vehicleSupplierGroupService;
        this.contractChangeHistoryService = contractChangeHistoryService;
        this.modelMapper = modelMapper;
        this.vehicleDTOAdapter = vehicleDTOAdapter;
        this.listUtils = listUtils;
    }

    @Override
//    @Cacheable(cacheNames = "Vehicle", key = "#p0", condition = "#p0 != null")
    public Vehicle findById(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        return vehicleRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Not found vehicle with id: " + id)
                        .displayMessage(Translator.toLocale("vehicle.not_found")));
    }

    @Override
    public Map<Long, VehicleProjection> findByIdIn(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        } else {
            return vehicleRepo.findByIdIn(ids)
                    .stream()
                    .collect(Collectors.toMap(VehicleProjection::getId, v -> v));
        }
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        return vehicleRepo.save(vehicle);
    }

    @Override
    public GeneralPageResponse<VehicleDTO> vehicleSuggestionsByNumberPlate(VehicleSuggestionPayload payload) {
        List<Vehicle> vehicles;

        if (StringUtils.isBlank(payload.getNumberPlate())) {
            return GeneralPageResponse.toResponse(Page.empty());
        } else {
            vehicles = vehicleRepo.findAllVehicleByNumberPlate(
                    ValidUtils.normalizeString(payload.getNumberPlate().trim()));
        }

        if (CollectionUtils.isEmpty(vehicles)) {
            return GeneralPageResponse.toResponse(Page.empty());
        }

        return GeneralPageResponse.toResponse(PageUtils
                .toPage(payload.getPage(), payload.getSize(), listUtils.transform(vehicles, vehicleDTOAdapter)));
    }

    @Override
    public Vehicle findByIdIgnoreDelete(Long id) {
        return vehicleRepo.findById(id).orElseThrow(() ->
                new NotFoundException("Not found by id: " + id));
    }

    @Override
    public Boolean deleteVehicle(Long id) {
        Vehicle vehicle = findByIdIgnoreDelete(id);

        if (vehicle.isDeleted()) {
            throw new BadRequestException("Vehicle already deleted!!!")
                    .displayMessage(Translator.toLocale("vehicle.already_deleted"));
        }

        if (vehicle.isInContract()) {
            throw new BadRequestException("Vehicle was assigned to a contract!!!")
                    .displayMessage(Translator.toLocale("vehicle.was_assigned_to_contract"));
        }

        vehicle.setDeleted(true);
        return true;
    }

    @Override
//    @Cacheable(cacheNames = "CreateVehicleOptions", key = "#root.method.name")
    public CreateVehicleOptionsWrapper getCreateOptions() {
        List<FuelTypeGroupDTO> fuelGroups = fuelTypeGroupService.findByDeletedFalse().stream()
                .map(x -> modelMapper.map(x, FuelTypeGroupDTO.class)).collect(Collectors.toList());
        List<VehicleSupplierGroupDTO> supplierGroups = vehicleSupplierGroupService.findByDeletedFalse().stream()
                .map(x -> modelMapper.map(x, VehicleSupplierGroupDTO.class)).collect(Collectors.toList());
        List<BranchDTO> branches = branchService.getBranchInfos();
        return CreateVehicleOptionsWrapper.builder()
                .fuelGroups(fuelGroups)
                .supplierGroups(supplierGroups)
                .branches(branches)
                .build();
    }

    @Override
    public VehicleDetailDTO createVehicle(VehiclePayload payload) {
        ValidObjectUtil.trimReflectAndNormalizeString(payload);
        payload.setId(null);
        avoidManualSetAppointedStatusVehicle(payload.getStatus());

        Vehicle vehicle = modelMapper.map(payload, Vehicle.class);
        setDropdownValue(vehicle, payload);

        Vehicle savedVehicle = vehicleRepo.save(vehicle);

        checkNumberPlateExist(savedVehicle.getNumberPlate(), savedVehicle.getId());

        return modelMapper.map(savedVehicle, VehicleDetailDTO.class);
    }

    private void setDropdownValue(Vehicle vehicle, VehiclePayload payload) {
        vehicle.setAccountant(userService.findAccountantById(payload.getAccountantId()));
        vehicle.setBranch(branchService.findById(payload.getBranchId()));
        vehicle.setOperationAdmin(userService.findOperationAdminById(payload.getOperationAdminId()));
        vehicle.setUnitOperator(userService.findUnitOperatorById(payload.getUnitOperatorId()));
        vehicle.setVehicleSupplierGroup(vehicleSupplierGroupService.findById(payload.getVehicleSupplierGroupId()));
        vehicle.setFuelTypeGroup(fuelTypeGroupService.findById(payload.getFuelTypeGroupId()));
    }

    private void avoidManualSetAppointedStatusVehicle(Integer status) {
        if (APPOINTED.getValue().equals(status)) {
            throw new BadRequestException("Status cannot be 2!!!")
                    .displayMessage(Translator.toLocale("vehicle.valid_error.status_wrong_format"));
        }
    }

    @Override
    public VehicleDetailDTO updateVehicle(VehiclePayload payload) {
        ValidObjectUtil.trimReflectAndNormalizeString(payload);

        Vehicle currentVehicle = findById(payload.getId());
        checkValidManualUpdateVehicleStatus(currentVehicle, payload.getStatus());

        modelMapper.map(payload, currentVehicle);
        setDropdownValue(currentVehicle, payload);

        Vehicle savedVehicle = vehicleRepo.save(currentVehicle);

        checkNumberPlateExist(savedVehicle.getNumberPlate(), savedVehicle.getId());

        return modelMapper.map(savedVehicle, VehicleDetailDTO.class);
    }

    private void checkValidManualUpdateVehicleStatus(Vehicle currentVehicle, Integer newStatus) {
        if (!currentVehicle.getStatus().equals(newStatus)) {
            if (UNAVAILABLE.getValue().equals(currentVehicle.getStatus())) {
                if (Objects.nonNull(currentVehicle.getCurrentJourneyDiaryId())) {
                    throw new BadRequestException("Vehicle is current in another journey diary")
                            .displayMessage(Translator.toLocale("vehicle.vehicle_is_current_in_journey_diary"));
                }

                if (APPOINTED.getValue().equals(newStatus)
                        && Objects.isNull(currentVehicle.getCurrentContractId())) {
                    throw new BadRequestException("Cannot set status from Unavailable to Appointed!!!")
                            .displayMessage(Translator.toLocale("vehicle.vehicle_is_not_assigned_to_any_contract"));
                }

                if (WAITING.getValue().equals(newStatus)
                        && !Objects.isNull(currentVehicle.getCurrentContractId())) {
                    throw new BadRequestException("Cannot set status from Unavailable to Waiting!!!")
                            .displayMessage(Translator.toLocale("vehicle.vehicle_is_current_in_a_contract"));
                }


            } else if (WAITING.getValue().equals(currentVehicle.getStatus())
                    && APPOINTED.getValue().equals(newStatus)) {
                throw new BadRequestException("Cannot manual set vehicle status from Waiting to Appointed!!!")
                        .displayMessage(Translator.toLocale("vehicle.avoid_manual_set_waiting_to_appointed_status"));
            } else if (APPOINTED.getValue().equals(currentVehicle.getStatus())
                    && WAITING.getValue().equals(newStatus)) {
                throw new BadRequestException("Cannot manual set vehicle status from Appointed to Waiting!!!")
                        .displayMessage(Translator.toLocale("vehicle.avoid_manual_set_appointed_to_waiting_status"));
            }

        }
    }

    private void checkNumberPlateExist(String numberPlate, Long id) {
        if (vehicleRepo.existsByNumberPlateAndIdNotAndDeletedFalse(numberPlate, id)) {
            throw new BadRequestException("Number plate already exist!!!")
                    .displayMessage(Translator.toLocale("vehicle.valid_error.number_plate_already_exist"));
        }
    }

    @Override
    public void assignToContract(Vehicle vehicle, Contract contract) {
        if (Objects.nonNull(vehicle)) {
            vehicle.setLendingContractId(vehicle.getCurrentContractId());
            vehicle.setCurrentContractId(contract.getId());
            vehicle.setInContract(true);
            updateVehicleStatus(vehicle);
        }
    }

    @Override
    public void unAssignFromContract(Vehicle vehicle, Contract contract) {
        if (Objects.nonNull(vehicle)) {
            if (contract.getId().equals(vehicle.getCurrentContractId())) {
                vehicle.setCurrentContractId(vehicle.getLendingContractId());
            }
            vehicle.setLendingContractId(null);
            updateVehicleStatus(vehicle);
        }
    }

    @Override
    public void updateVehicleStatus(Vehicle vehicle) {
        if (Objects.nonNull(vehicle) && !UNAVAILABLE.getValue().equals(vehicle.getStatus())) {
            if (Objects.nonNull(vehicle.getCurrentJourneyDiaryId())) {
                vehicle.setStatus(UNAVAILABLE.getValue());
            } else if (Objects.isNull(vehicle.getCurrentContractId()) && Objects.isNull(vehicle.getLendingContractId())) {
                vehicle.setStatus(WAITING.getValue());
            } else {
                vehicle.setStatus(APPOINTED.getValue());
            }
        }
    }

    @Override
//    @Cacheable(cacheNames = "VehicleFilterOptions", key = "#root.method.name")
    public VehicleFilterOptionsWrapper getFilterOptions() {
        return VehicleFilterOptionsWrapper.builder()
                .supplierGroupDTOS(vehicleSupplierGroupService.findByDeletedFalse().stream()
                        .map(x -> modelMapper.map(x, VehicleSupplierGroupDTO.class)).collect(Collectors.toList()))
                .vehicleStatusEnums(AvisApiConstant.VEHICLE_STATUS_ENUMS)
                .build();
    }

    @Override
    public Vehicle getContractVehicleAtTime(Contract contract, Timestamp timestamp) {
        ContractChangeHistory history = contractChangeHistoryService
                .findLastChangeOfField(contract, VEHICLE_ID.getName(), timestamp);
        if (Objects.nonNull(history)) {
            if (!timestamp.before(history.getFromDate())) {
                return findByIdIgnoreDelete(Long.parseLong(history.getNewValue()));
            } else if (Objects.nonNull(history.getOldValue())) {
                return findByIdIgnoreDelete(Long.parseLong(history.getOldValue()));
            } else {
                return null;
            }
        } else {
            return contract.getVehicle();
        }
    }
}
