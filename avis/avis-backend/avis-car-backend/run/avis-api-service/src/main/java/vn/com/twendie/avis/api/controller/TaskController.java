package vn.com.twendie.avis.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.service.impl.ImportExcelService;
import vn.com.twendie.avis.api.task.CreateJourneyDiaryDailiesTask;
import vn.com.twendie.avis.api.task.SendAlertEmailTask;
import vn.com.twendie.avis.api.task.UpdateContractStatusTask;
import vn.com.twendie.avis.api.task.UpdateEffectiveDateContractTask;
import vn.com.twendie.avis.security.annotation.RequirePermission;

import javax.mail.MessagingException;
import java.util.Collections;
import java.util.Objects;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private ImportExcelService importExcelService;

    private final UpdateContractStatusTask updateContractStatusTask;
    private final UpdateEffectiveDateContractTask updateEffectiveDateContractTask;
    private final CreateJourneyDiaryDailiesTask createJourneyDiaryDailiesTask;
    private final SendAlertEmailTask sendAlertEmailTask;

    public TaskController(UpdateContractStatusTask updateContractStatusTask,
                          UpdateEffectiveDateContractTask updateEffectiveDateContractTask,
                          CreateJourneyDiaryDailiesTask createJourneyDiaryDailiesTask,
                          SendAlertEmailTask sendAlertEmailTask) {
        this.updateContractStatusTask = updateContractStatusTask;
        this.updateEffectiveDateContractTask = updateEffectiveDateContractTask;
        this.createJourneyDiaryDailiesTask = createJourneyDiaryDailiesTask;
        this.sendAlertEmailTask = sendAlertEmailTask;
    }

    @GetMapping("updateInProgressContract")
    @RequirePermission(acceptedRoles = {"Superuser"})
    public ApiResponse<?> updateInProgressContract() {
        updateContractStatusTask.updateInProgressContract();
        return ApiResponse.success(Collections.emptyMap());
    }

    @GetMapping("updateFinishedContract")
    @RequirePermission(acceptedRoles = {"Superuser"})
    public ApiResponse<?> updateFinishedContract() {
        updateContractStatusTask.updateFinishedContract();
        return ApiResponse.success(Collections.emptyMap());
    }

    @GetMapping("updateEffectiveDateContractFields")
    @RequirePermission(acceptedRoles = {"Superuser"})
    public ApiResponse<?> updateEffectiveDateContractFields() {
        updateEffectiveDateContractTask.updateEffectiveDateContractFields();
        return ApiResponse.success(Collections.emptyMap());
    }

    @GetMapping("createJourneyDiaryDailiesWithDriver")
    @RequirePermission(acceptedRoles = {"Superuser"})
    public ApiResponse<?> createJourneyDiaryDailiesWithDriver() {
        createJourneyDiaryDailiesTask.createJourneyDiaryDailiesWithDriver();
        return ApiResponse.success(Collections.emptyMap());
    }

    @GetMapping("createJourneyDiaryDailiesWithoutDriver")
    @RequirePermission(acceptedRoles = {"Superuser"})
    public ApiResponse<?> createJourneyDiaryDailiesWithoutDriver() {
        createJourneyDiaryDailiesTask.createJourneyDiaryDailiesWithoutDriver();
        return ApiResponse.success(Collections.emptyMap());
    }

    @GetMapping("sendEmailAlert")
    @RequirePermission(acceptedRoles = {"Superuser"})
    public ApiResponse<?> sendEmailAlert(@RequestParam(value = "email", required = false) String email) throws MessagingException {
        if (Objects.isNull(email)) {
            sendAlertEmailTask.sendEmailAlert();
        } else {
            sendAlertEmailTask.sendEmailAlert(Collections.singleton(email));
        }
        return ApiResponse.success(Collections.emptyMap());
    }

    @PostMapping("import-vehicle")
    @RequirePermission(acceptedRoles = {"Superuser"})
    public ApiResponse<?> sendEmailAlert(MultipartFile file) throws Exception {
        importExcelService.importVehicleExcel(file);
        return ApiResponse.success(null);
    }
}
