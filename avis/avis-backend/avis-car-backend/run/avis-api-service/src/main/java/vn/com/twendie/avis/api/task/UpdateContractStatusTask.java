package vn.com.twendie.avis.api.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.repository.ContractRepo;
import vn.com.twendie.avis.api.service.ContractService;
import vn.com.twendie.avis.api.service.UserService;
import vn.com.twendie.avis.api.service.VehicleService;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.data.model.Vehicle;
import vn.com.twendie.avis.notification.service.NotificationService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static vn.com.twendie.avis.data.enumtype.ContractStatusEnum.*;

@Service
@Slf4j
public class UpdateContractStatusTask {

    private final ContractRepo contractRepo;
    private final ContractService contractService;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final NotificationService notificationService;

    private final DateUtils dateUtils;
    private final ListUtils listUtils;

    public UpdateContractStatusTask(ContractRepo contractRepo,
                                    ContractService contractService,
                                    UserService userService,
                                    VehicleService vehicleService,
                                    NotificationService notificationService,
                                    DateUtils dateUtils,
                                    ListUtils listUtils) {
        this.contractRepo = contractRepo;
        this.contractService = contractService;
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.notificationService = notificationService;
        this.dateUtils = dateUtils;
        this.listUtils = listUtils;
    }

//    @Scheduled(cron = "0 30 17 * * *")
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void updateContractStatus() {
        updateInProgressContract();
        updateFinishedContract();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void updateInProgressContract() {
        log.info("Start task: updateInProgressContract");
        Timestamp endOfToday = dateUtils.endOfDay(dateUtils.now());
        List<Contract> contracts = contractRepo
                .findAllByFromDatetimeBeforeAndStatusAndDeletedFalse(endOfToday, ASSIGNED_CAR.getCode());
        contracts.forEach(contract -> contract.setStatus(IN_PROGRESS.getCode()));
        contractRepo.saveAll(contracts);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void updateFinishedContract() {
        log.info("Start task: updateFinishedContract");
        Timestamp startOfToday = dateUtils.startOfToday();
        List<Integer> status = new ArrayList<Integer>() {{
            add(WAITING_ASSIGN_CAR.getCode());
            add(ASSIGNED_CAR.getCode());
            add(IN_PROGRESS.getCode());
        }};
        List<Contract> contracts = contractRepo
                .findAllByToDatetimeBeforeAndStatusInAndDeletedFalse(startOfToday, status);
        contracts.forEach(contract -> {
            contract.setStatus(FINISHED.getCode());
            contractService.releaseDriver(contract);
            contractService.releaseVehicle(contract);

            User driver = contract.getDriver();
            if (Objects.nonNull(driver)) {
                Contract contractLendingDriver = contractService.findById(driver.getLendingContractId());
                userService.unAssignFromContract(driver, contract);
                contractService.retrieveDriver(contractLendingDriver);
                if (Objects.nonNull(contractLendingDriver)) {
                    contractService.save(contractLendingDriver);
                }
                userService.save(driver);
            }

            Vehicle vehicle = contract.getVehicle();
            if (Objects.nonNull(vehicle)) {
                Contract contractLendingVehicle = contractService.findById(vehicle.getLendingContractId());
                vehicleService.unAssignFromContract(vehicle, contract);
                contractService.retrieveVehicle(contractLendingVehicle);
                if (Objects.nonNull(contractLendingVehicle)) {
                    contractService.save(contractLendingVehicle);
                }
                vehicleService.save(vehicle);
            }

        });
        contractRepo.saveAll(contracts);
        notificationService.saveAll(listUtils.transform(contracts, notificationService::buildCustomerNoti));
    }

}
