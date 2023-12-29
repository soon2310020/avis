package vn.com.twendie.avis.api.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.repository.*;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.ContractService;
import vn.com.twendie.avis.api.service.CostTypeService;
import vn.com.twendie.avis.api.service.NormListService;
import vn.com.twendie.avis.data.model.*;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.data.enumtype.ContractStatusEnum.CANCELED;
import static vn.com.twendie.avis.data.enumtype.ContractStatusEnum.FINISHED;

@Service
@Slf4j
public class UpdateEffectiveDateContractTask {

    private final ApplicationContext context;

    private final ContractRepo contractRepo;
    private final ContractChangeHistoryRepo contractChangeHistoryRepo;
    private final ContractCostTypeRepo contractCostTypeRepo;
    private final ContractNormListRepo contractNormListRepo;
    private final LogContractCostTypeRepo logContractCostTypeRepo;
    private final LogContractNormListRepo logContractNormListRepo;

    private final ContractService contractService;
    private final CostTypeService costTypeService;
    private final NormListService normListService;

    private final DateUtils dateUtils;

    private static final Set<String> EFFECTIVE_DATE_FIELDS = new HashSet<String>() {{
        add("rental_service_type_id");
        add("working_time_weekend_holiday_from");
        add("working_time_weekend_holiday_to");
        add("working_time_from");
        add("working_time_to");
        add("working_day_id");
        add("working_day");
        add("fuel_adjust_percent");
    }};

    public UpdateEffectiveDateContractTask(ApplicationContext context,
                                           ContractRepo contractRepo,
                                           ContractChangeHistoryRepo contractChangeHistoryRepo,
                                           ContractCostTypeRepo contractCostTypeRepo,
                                           ContractNormListRepo contractNormListRepo,
                                           LogContractCostTypeRepo logContractCostTypeRepo,
                                           LogContractNormListRepo logContractNormListRepo,
                                           ContractService contractService,
                                           CostTypeService costTypeService,
                                           NormListService normListService,
                                           DateUtils dateUtils) {
        this.context = context;
        this.contractRepo = contractRepo;
        this.contractChangeHistoryRepo = contractChangeHistoryRepo;
        this.contractCostTypeRepo = contractCostTypeRepo;
        this.contractNormListRepo = contractNormListRepo;
        this.logContractCostTypeRepo = logContractCostTypeRepo;
        this.logContractNormListRepo = logContractNormListRepo;
        this.contractService = contractService;
        this.costTypeService = costTypeService;
        this.normListService = normListService;
        this.dateUtils = dateUtils;
    }

//    @Scheduled(cron = "0 0 18 * * *")
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void updateEffectiveDateContractFields() {
        log.info("Start task: updateEffectiveDateContractFields");
        Timestamp now = dateUtils.now();
        Timestamp startOfToday = dateUtils.startOfDay(now);
        Timestamp endOfToday = dateUtils.endOfDay(now);

        List<ContractChangeHistory> contractChangeHistories = contractChangeHistoryRepo
                .findAllByMappingFieldCodeFontendFieldNameInAndFromDateBetweenAndContractDeletedFalseAndDeletedFalseOrderByCreatedAt(
                        EFFECTIVE_DATE_FIELDS, startOfToday, endOfToday);
        contractChangeHistories.stream()
                .collect(Collectors.groupingBy(ContractChangeHistory::getContractId))
                .forEach((contractId, histories) -> {
                    Contract contract = contractService.findById(contractId);
                    if (Objects.nonNull(contract) && !contractService.inStatus(contract, CANCELED, FINISHED)) {
                        histories.forEach(history -> {
                            String columnName = history.getMappingFieldCodeFontend().getFieldName();
                            Field updatedField = findEntityField(Contract.class, columnName);
                            if (Objects.nonNull(updatedField)) {
                                try {
                                    String newValueString = history.getNewValue();
                                    updateObjectField(contract, updatedField, newValueString);
                                    contractRepo.save(contract);
                                } catch (Exception e) {
                                    log.error(ExceptionUtils.getRootCauseMessage(e));
                                }
                            } else {
                                log.error("Not found field in contract model with column name: {}", columnName);
                            }
                        });
                    }
                });

        List<LogContractPriceCostType> logContractCostTypes = logContractCostTypeRepo
                .findAllByFromDateBetweenAndContractDeletedFalseAndDeletedFalseOrderByCreatedAt(startOfToday, endOfToday);
        logContractCostTypes.stream()
                .collect(Collectors.groupingBy(LogContractPriceCostType::getContractId))
                .forEach((contractId, logs) -> {
                    Contract contract = contractService.findById(contractId);
                    if (Objects.nonNull(contract) && !contractService.inStatus(contract, CANCELED, FINISHED)) {
                        logs.forEach(log -> {
                            CostType costType = log.getCostType();
                            ContractCostType contractCostType = costTypeService
                                    .getContractCostTypes(contract).stream()
                                    .filter(item -> item.getId().getCostType().getCode().equals(costType.getCode()))
                                    .findFirst()
                                    .orElse(null);
                            if (Objects.nonNull(contractCostType)) {
                                contractCostType.setPrice(log.getPrice());
                                contractCostTypeRepo.save(contractCostType);
                            }
                        });
                    }
                });

        List<LogContractNormList> logContractNormLists = logContractNormListRepo
                .findAllByFromDateBetweenAndContractDeletedFalseAndDeletedFalseOrderByCreatedAt(startOfToday, endOfToday);
        logContractNormLists.stream()
                .collect(Collectors.groupingBy(LogContractNormList::getContractId))
                .forEach((contractId, logs) -> {
                    Contract contract = contractService.findById(contractId);
                    if (Objects.nonNull(contract) && !contractService.inStatus(contract, CANCELED, FINISHED)) {
                        logs.forEach(log -> {
                            NormList normList = log.getNormList();
                            ContractNormList contractNormList = normListService
                                    .getContractNormLists(contract)
                                    .stream()
                                    .filter(item -> item.getId().getNormList().getCode().equals(normList.getCode()))
                                    .findFirst()
                                    .orElse(null);
                            if (Objects.nonNull(contractNormList)) {
                                contractNormList.setQuota(log.getQuota());
                                contractNormListRepo.save(contractNormList);
                            }
                        });
                    }
                });
    }

    private void updateObjectField(Object object, Field updatedField, String newValueString) throws Exception {
        updatedField.setAccessible(true);
        Object newValue = null;
        if (Objects.nonNull(newValueString)) {
            if (updatedField.isAnnotationPresent(Column.class)) {
                if (updatedField.getType().equals(String.class)) {
                    newValue = newValueString;
                } else if (updatedField.getType().equals(BigDecimal.class)) {
                    newValue = new BigDecimal(newValueString);
                } else {
                    newValue = updatedField.getType()
                            .getDeclaredMethod("valueOf", String.class)
                            .invoke(null, newValueString);
                }
            } else if (updatedField.isAnnotationPresent(JoinColumn.class)) {
                Long newId = Long.valueOf(newValueString);
                newValue = findEntityById(updatedField.getType(), newId);
            }
        }
        updatedField.set(object, newValue);
    }

    private Field findEntityField(Class<?> entityClass, String columnName) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> isAnnotationColumnWithName(field, columnName) ||
                        isAnnotationJoinColumnWithName(field, columnName))
                .findFirst()
                .orElse(null);
    }

    private boolean isAnnotationColumnWithName(Field field, String columnName) {
        return field.isAnnotationPresent(Column.class) &&
                field.getAnnotation(Column.class).name().equals(columnName);
    }

    private boolean isAnnotationJoinColumnWithName(Field field, String columnName) {
        return field.isAnnotationPresent(JoinColumn.class) &&
                field.getAnnotation(JoinColumn.class).name().equals(columnName);
    }

    private Object findEntityById(Class<?> entityClass, Long id)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object repo = getRepositoryBean(entityClass);
        Optional<?> optional = (Optional<?>) repo.getClass()
                .getMethod("findById", Object.class)
                .invoke(repo, id);
        return optional.orElseThrow(() ->
                new NotFoundException("Not found " + entityClass.getSimpleName() + " with id: " + id));
    }

    private Object getRepositoryBean(Class<?> entityClass) {
        String beanName = context.getBeanNamesForType(ResolvableType
                .forClassWithGenerics(JpaRepository.class, entityClass, Long.class))[0];
        return context.getBean(beanName);
    }

}
