package vn.com.twendie.avis.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpServerErrorException;
import vn.com.twendie.avis.api.adapter.ContractChangeHistoryDTOAdapter;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.projection.LogContractCostProjection;
import vn.com.twendie.avis.api.model.projection.LogContractNormProjection;
import vn.com.twendie.avis.api.model.response.ContractChangeHistoryItemDTO;
import vn.com.twendie.avis.api.repository.ContractChangeHistoryRepo;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.api.service.ContractChangeHistoryService;
import vn.com.twendie.avis.api.service.LogContractCostTypeService;
import vn.com.twendie.avis.api.service.LogContractNormListService;
import vn.com.twendie.avis.api.service.MappingFieldCodeFontendService;
import vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum;
import vn.com.twendie.avis.data.model.*;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContractChangeHistoryServiceImpl implements ContractChangeHistoryService {

    private final ContractChangeHistoryRepo contractChangeHistoryRepo;

    private final MappingFieldCodeFontendService mappingFieldCodeFontendService;
    private final ContractChangeHistoryDTOAdapter contractChangeHistoryDTOAdapter;
    private final LogContractCostTypeService logContractCostTypeService;
    private final LogContractNormListService logContractNormListService;

    private final DateUtils dateUtils;

    public ContractChangeHistoryServiceImpl(ContractChangeHistoryRepo contractChangeHistoryRepo,
                                            MappingFieldCodeFontendService mappingFieldCodeFontendService,
                                            ContractChangeHistoryDTOAdapter contractChangeHistoryDTOAdapter,
                                            LogContractCostTypeService logContractCostTypeService,
                                            LogContractNormListService logContractNormListService,
                                            DateUtils dateUtils) {
        this.contractChangeHistoryRepo = contractChangeHistoryRepo;
        this.mappingFieldCodeFontendService = mappingFieldCodeFontendService;
        this.contractChangeHistoryDTOAdapter = contractChangeHistoryDTOAdapter;
        this.logContractCostTypeService = logContractCostTypeService;
        this.logContractNormListService = logContractNormListService;
        this.dateUtils = dateUtils;
    }

    @Override
    public ContractChangeHistory save(ContractChangeHistory contractChangeHistory) {
        return contractChangeHistoryRepo.save(contractChangeHistory);
    }

    @Override
    public List<ContractChangeHistory> saveAll(Collection<ContractChangeHistory> contractChangeHistories) {
        return contractChangeHistoryRepo.saveAll(contractChangeHistories);
    }

    @Override
    public List<ContractChangeHistory> buildContractChangeHistories(Contract oldContract, Contract newContract, Timestamp fromDate, User createdBy) {
        return Arrays.stream(Contract.class.getDeclaredFields())
                .filter(field -> {
                    field.setAccessible(true);
                    try {
                        return Objects.nonNull(field.get(newContract)) &&
                                (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(JoinColumn.class)) &&
                                !Collection.class.isAssignableFrom(field.getDeclaringClass()) &&
                                !Objects.equals(field.get(oldContract), field.get(newContract));
                    } catch (IllegalAccessException e) {
                        return false;
                    }
                })
                .map(field -> {
                    try {
                        String tableName = Contract.class.getAnnotation(Table.class).name();
                        String fieldName = field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class).name() :
                                field.isAnnotationPresent(JoinColumn.class) ? field.getAnnotation(JoinColumn.class).name() :
                                        field.getName().toLowerCase();
                        MappingFieldCodeFontend mapping = mappingFieldCodeFontendService.findByTableAndField(tableName, fieldName);
                        Object oldValue;
                        Object newValue;
                        Class<?> fieldClass = field.getType();
                        if (BaseModel.class.isAssignableFrom(fieldClass)) {
                            Field idField = Arrays.stream(fieldClass.getDeclaredFields())
                                    .filter(childField -> childField.isAnnotationPresent(Id.class))
                                    .findFirst()
                                    .orElseThrow(() -> new NotFoundException("Not found id field of class: " + fieldClass.getSimpleName()));
                            idField.setAccessible(true);
                            oldValue = idField.get(field.get(oldContract));
                            newValue = idField.get(field.get(newContract));
                        } else {
                            oldValue = field.get(oldContract);
                            newValue = field.get(newContract);
                        }
                        if (fromDate.before(dateUtils.now())) {
                            field.set(oldContract, field.get(newContract));
                        }
                        return ContractChangeHistory.builder()
                                .oldValue(Objects.isNull(oldValue) ? null : String.valueOf(oldValue))
                                .newValue(Objects.isNull(newValue) ? null : String.valueOf(newValue))
                                .fromDate(fromDate)
                                .createdBy(createdBy)
                                .contract(oldContract)
                                .mappingFieldCodeFontend(mapping)
                                .build();
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public ContractChangeHistory buildContractChangeDriverHistory(Contract contract, User oldDriver, User newDriver, Timestamp fromDate, User createdBy) {
        if (!Objects.equals(oldDriver, newDriver)) {
            MappingFieldCodeFontend mapping = mappingFieldCodeFontendService.findByTableAndField("contract", "driver_id");
            return ContractChangeHistory.builder()
                    .oldValue(Objects.isNull(oldDriver) ? null : String.valueOf(oldDriver.getId()))
                    .newValue(Objects.isNull(newDriver) ? null : String.valueOf(newDriver.getId()))
                    .fromDate(fromDate)
                    .createdBy(createdBy)
                    .contract(contract)
                    .mappingFieldCodeFontend(mapping)
                    .build();
        } else {
            return null;
        }
    }

    @Override
    public ContractChangeHistory buildContractChangeVehicleHistory(Contract contract, Vehicle oldVehicle, Vehicle newVehicle, Timestamp fromDate, User createdBy) {
        if (!Objects.equals(oldVehicle, newVehicle)) {
            MappingFieldCodeFontend mapping = mappingFieldCodeFontendService.findByTableAndField("contract", "vehicle_id");
            return ContractChangeHistory.builder()
                    .oldValue(Objects.isNull(oldVehicle) ? null : String.valueOf(oldVehicle.getId()))
                    .newValue(Objects.isNull(newVehicle) ? null : String.valueOf(newVehicle.getId()))
                    .fromDate(fromDate)
                    .createdBy(createdBy)
                    .contract(contract)
                    .mappingFieldCodeFontend(mapping)
                    .build();
        } else {
            return null;
        }
    }

    @Override
    public GeneralPageResponse<ContractChangeHistoryItemDTO> findByContractId(Long contractId, int page) {
        List<ContractChangeHistory> immediatelyChanges = findImmediatelyChangesByContractId(contractId);
        List<ContractChangeHistoryItemDTO> immediatelyChangeDTOs = new ArrayList<>();

        if (!CollectionUtils.isEmpty(immediatelyChanges)) {
            for (int i = 0; i < immediatelyChanges.size(); i++) {
                immediatelyChangeDTOs.add(
                        contractChangeHistoryDTOAdapter
                                .apply(immediatelyChanges.get(i),
                                        i > 0 ? immediatelyChanges.get(i - 1) : null)
                );
            }
        }


        List<LogContractCostProjection> priceChanges = logContractCostTypeService.findAllByContractId(contractId);
        List<LogContractNormProjection> quotaChanges = logContractNormListService.findAllByContractId(contractId);

        List<ContractChangeHistoryItemDTO> priceChangeDTOs = transformCost(priceChanges);
        List<ContractChangeHistoryItemDTO> quotaChangeDTOS = transformNorm(quotaChanges);

        immediatelyChangeDTOs.addAll(priceChangeDTOs);
        immediatelyChangeDTOs.addAll(quotaChangeDTOS);
        immediatelyChangeDTOs.sort(Comparator
                .comparing(ContractChangeHistoryItemDTO::getCreatedAt)
                .reversed());

        return GeneralPageResponse.toResponse(toPageWithNumberOrder(page,
                AvisApiConstant.DEFAULT_PAGE_SIZE, immediatelyChangeDTOs));
    }

    @Override
    public String findLastChangeOfField(Long contractId, Long fieldId, Timestamp timestamp) {
        List<String> closetChanges = contractChangeHistoryRepo.findClosetChangeOfField(contractId, fieldId, timestamp);
        if (CollectionUtils.isEmpty(closetChanges)) {
            return null;
        } else {
            return closetChanges.get(0);
        }
    }

    @Override
    public ContractChangeHistory findLastChangeOfField(Contract contract, String field, Timestamp timestamp) {
        List<ContractChangeHistory> contractChangeHistories = getContractChangeHistories(contract, field);
        if (contractChangeHistories.isEmpty()) {
            return null;
        } else {
            return contractChangeHistories.stream()
                    .filter(history -> !history.getFromDate().after(dateUtils.endOfDay(timestamp)))
                    .findFirst()
                    .orElse(contractChangeHistories.get(contractChangeHistories.size() - 1));
        }
    }

    @Override
    public void fetchContractChangeHistories(Collection<Contract> contracts) {
        if (!CollectionUtils.isEmpty(contracts)) {
            Set<Long> contractIds = contracts.stream()
                    .map(Contract::getId)
                    .collect(Collectors.toSet());
            List<ContractChangeHistory> contractChangeHistories = contractChangeHistoryRepo.findAllByContractIdIn(contractIds);
            contracts.forEach(contract -> contract.setContractChangeHistories(
                    contractChangeHistories.stream()
                            .filter(contractChangeHistory -> contract.getId().equals(contractChangeHistory.getContractId()))
                            .collect(Collectors.toSet())
            ));
        }
    }

    @Override
    public void validNewValueOfHistory(Contract contract, ContractChangeHistory history, Timestamp toDate) {
        if (history != null
                && StringUtils.isBlank(history.getNewValue())) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Got invalid data from change history of contract id: " + contract.getId() +
                            " history field id: " + MappingFieldCodeFrontendEnum.WORKING_DAY_ID.getId() +
                            " before time: " + toDate.getTime());
        }
    }

    private Page<ContractChangeHistoryItemDTO> toPageWithNumberOrder(int page, int size,
                                                                     List<ContractChangeHistoryItemDTO> list) {
        int pageOffSet = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageOffSet, size);

        int total = list.size();
        List<ContractChangeHistoryItemDTO> pageContents;
        int totalPage = (int) Math.ceil((double) total / size);
        if (page > totalPage) {
            pageContents = Collections.emptyList();
        } else {
            pageContents = list.subList(pageOffSet * size,
                    Math.min((pageOffSet * size + size), total));
        }

        for (int i = 0; i < pageContents.size(); i++) {
            pageContents.get(i).setNumberOrder(pageOffSet * size + i + 1);
        }

        return new PageImpl<>(pageContents, pageable, total);
    }

    private List<ContractChangeHistoryItemDTO> transformNorm(List<LogContractNormProjection> quotaChanges) {
        if (!CollectionUtils.isEmpty(quotaChanges)) {
            List<ContractChangeHistoryItemDTO> dtos = new ArrayList<>(Collections.emptyList());
            long id = 0;
            String oldVal = null;
            for (LogContractNormProjection log : quotaChanges) {
                if (log.getNormListId() != id) {
                    id = log.getNormListId();
                    oldVal = String.valueOf(log.getQuota());
                } else {
                    ContractChangeHistoryItemDTO dto = ContractChangeHistoryItemDTO.builder()
                            .effectiveDate(log.getFromDate())
                            .createdAt(log.getCreatedAt())
                            .updatedBy(StringUtils.isBlank(log.getUserName()) ? ""
                                    : log.getUserName())
                            .fieldName(log.getNormName())
                            .oldValue(oldVal + " " + log.getUnit())
                            .newValue(log.getQuota() + " " + log.getUnit())
                            .build();
                    dtos.add(dto);
                    oldVal = String.valueOf(log.getQuota());
                }
            }

            return dtos;
        } else {
            return Collections.emptyList();
        }
    }

    private List<ContractChangeHistoryItemDTO> transformCost(List<LogContractCostProjection> priceChanges) {
        if (!CollectionUtils.isEmpty(priceChanges)) {
            List<ContractChangeHistoryItemDTO> dtos = new ArrayList<>(Collections.emptyList());
            long id = 0;
            String oldVal = null;
            for (LogContractCostProjection log : priceChanges) {
                if (log.getCostTypeId() != id) {
                    id = log.getCostTypeId();
                    oldVal = String.valueOf(log.getPrice());
                } else {
                    ContractChangeHistoryItemDTO dto = ContractChangeHistoryItemDTO.builder()
                            .effectiveDate(log.getFromDate())
                            .createdAt(log.getCreatedAt())
                            .updatedBy(StringUtils.isBlank(log.getUserName()) ? ""
                                    : log.getUserName())
                            .fieldName(log.getCostTypeName())
                            .oldValue(oldVal + " VNĐ")
                            .newValue(log.getPrice() + " VNĐ")
                            .build();
                    dtos.add(dto);
                    oldVal = String.valueOf(log.getPrice());
                }
            }

            return dtos;
        } else {
            return Collections.emptyList();
        }
    }

    private List<ContractChangeHistory> findImmediatelyChangesByContractId(Long contractId) {
        return contractChangeHistoryRepo.findAllByContractIdAndDeletedFalse(contractId);
    }

}
