package saleson.api.checklist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;

import saleson.api.checklist.payload.ChecklistPayload;
import saleson.api.company.CompanyService;
import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.mold.MoldRepository;
import saleson.api.resource.ResourceHandler;
import saleson.api.versioning.service.VersioningService;
import saleson.common.constant.CommonMessage;
import saleson.common.payload.ApiResponse;
import saleson.common.util.StringUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.Mold;
import saleson.model.checklist.CheckListObjectType;
import saleson.model.checklist.Checklist;
import saleson.model.checklist.ChecklistType;

@RestController
@RequestMapping("/api/checklist")
public class ChecklistController {
    @Autowired
    ChecklistService checklistService;
    @Autowired
    ChecklistRepository checklistRepository;
    @Autowired
    VersioningService versioningService;
    @Autowired
    CompanyService companyService;

    @Autowired
    private ResourceHandler handler;

    @Autowired
    MoldRepository moldRepository;

    @Autowired
    ColumnTableConfigService columnTableConfigService;

    @GetMapping
    @DataLeakDetector(disabled = true)
    public ResponseEntity<Page<Checklist>> topics(ChecklistPayload payload, Pageable pageable) {
        payload.setSelectedFields(columnTableConfigService.getListManagementSelectedFields(payload.getChecklistType()));
        Page<Checklist> pageContent = checklistService.findAll(payload.getPredicate(), pageable);
        return new ResponseEntity<>(pageContent, HttpStatus.OK);
    }

    private ApiResponse valid(ChecklistPayload payload){
//        if(payload.getCompanyId()==null){
//            return new ApiResponse(false, "Company empty!");
//        }
        if(StringUtils.isEmpty(payload.getChecklistCode())){
            return new ApiResponse(false, "Checklist Id  empty!");
        }
//        Company company=companyService.findById(payload.getCompanyId());
//        if(CollectionUtils.isEmpty(payload.getCompanyIds())){
//            return new ApiResponse(false, "Company not exist!");
//        }
        Long existCode;
        if (CheckListObjectType.PICK_LIST.equals(payload.getObjectType())) {
            existCode = checklistRepository.countAllByChecklistCodeAndObjectTypeExist(payload.getChecklistCode(), payload.getObjectType().name(), payload.getId());
        } else {
            existCode = checklistRepository.countAllByChecklistCodeAndChecklistTypeAndObjectTypeExist(payload.getChecklistCode(), payload.getChecklistType().getCode(), payload.getObjectType().name(), payload.getId());
        }

        if (existCode > 0) {
            return new ApiResponse(false, "Checklist ID is already registered in the system.");
        }
//        if (payload.getEnabled() == true && payload.getChecklistType().equals(ChecklistType.REJECT_RATE)) {
//            List<Checklist> existConfigList = checklistRepository.findAllByCompanyIdAndChecklistTypeAndEnabledIsTrue(payload.getCompanyId(), payload.getChecklistType());
//            long existOther = existConfigList.size();
//            if (payload.getId() != null) {
//                existOther = existConfigList.stream().filter(c -> !c.getId().equals(payload.getId())).count();
//            }
//            if (existOther > 0) {
//                return new ApiResponse(false, "A checklist has already been enabled for this company. Please disable it before creating the new checklist.");
//            }
//        }
        return null;
    }
    @PostMapping("create")
    public ApiResponse create(@RequestBody ChecklistPayload payload) {
        try {
            ApiResponse valid = valid(payload);
            if (valid != null) return valid;

            Checklist checklist =checklistService.newChecklist(payload);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse(false, "Fail!");
        }
        return new ApiResponse(true, "OK!!");
    }

    @GetMapping("{id}")
    public ResponseEntity detail(@PathVariable("id") Long id){
        Checklist checklist = checklistRepository.findById(id).orElse(null);
        if(checklist ==null){
            return ResponseEntity.badRequest().body("checklist not exist!");
        }
        return new ResponseEntity<>(checklist, HttpStatus.OK);

    }
    @PutMapping("{id}")
    public ApiResponse update(@PathVariable("id") Long id,@RequestBody ChecklistPayload payload) {


        try {
            if(id==null){
                return new ApiResponse(false, "Id empty!");
            }
            payload.setId(id);
            ApiResponse valid = valid(payload);
            if (valid != null) return valid;

            Checklist checklist = checklistRepository.findById(id).orElse(null);
            if(checklist ==null){
                return new ApiResponse(false, "checklist not exist!");
            }
            payload.bindData(checklist);
            checklistService.save(checklist,payload);
            versioningService.writeHistory(checklist);

        } catch (Exception e) {
            return new ApiResponse(false, "Fail!");
        }
        return new ApiResponse(true, "OK!!");
    }
    @DeleteMapping("{id}")
    public ApiResponse delete(@PathVariable("id") Long id){
        Checklist checklist = checklistRepository.findById(id).orElse(null);
        if(checklist ==null){
            return new ApiResponse(false, "checklist not exist!");
        }
        checklistService.deleteChecklist(id);
        return new ApiResponse(true, "OK!!");
    }
    @PutMapping("{id}/enable")
    public ApiResponse changeStatus(@PathVariable("id") Long id, @RequestBody ChecklistPayload payload){
        Checklist checklist = checklistRepository.findById(id).orElse(null);
        if(checklist ==null){
            return new ApiResponse(false, "checklist not exist!");
        }
        checklist.setEnabled(payload.getEnabled());
        checklistRepository.save(checklist);
        versioningService.writeHistory(checklist);
        return new ApiResponse(true, "OK!!");
    }

    @GetMapping("checklist-mold")
    public List<String> checklistMold(@RequestParam(value = "moldId") Long moldId,
                                      @RequestParam(required = false, value = "checklistType") String checklistType) {
        ChecklistType enumChecklistType = checklistType != null ? ChecklistType.valueOf(checklistType) : ChecklistType.MAINTENANCE;
        Map<String,String> resources= handler.getAllMessagesOfCurrentUser();
        List<String> checklistValue = Arrays.asList(
                resources.get("dry_the_mold"), resources.get("clean_mold_cavities"), resources.get("check_the_hardware"),
                resources.get("check_eject_com"), resources.get("check_all_eject_com"), resources.get("inspect_cavity")
        );
        Mold mold = moldRepository.findById(moldId).orElse(null);
        if (mold != null && mold.getLocation() != null && mold.getLocation().getCompanyId() != null) {
            List<Checklist> checklists = checklistRepository.findAllByCompanyContainsAndChecklistTypeAndObjectTypeAndEnabledIsTrue(mold.getLocation().getCompanyId(), enumChecklistType, CheckListObjectType.CHECK_LIST);
            Checklist checklist = CollectionUtils.isNotEmpty(checklists) ? checklists.get(0) : null;
            if (checklist != null && !checklist.getChecklistItems().isEmpty()) {
                checklistValue = checklist.getChecklistItems();
            }
        }
        return checklistValue;
    }

    @GetMapping("checklist-mold-new")
    public ApiResponse checklistMoldNew(@RequestParam(value = "moldId") Long moldId,
                                         @RequestParam(required = false, value = "checklistType") String checklistType) {
        try {
            ChecklistType enumChecklistType = checklistType != null ? ChecklistType.valueOf(checklistType) : ChecklistType.MAINTENANCE;
            Map<String,String> resources= handler.getAllMessagesOfCurrentUser();
            List<String> maintenanceDefaultChecklist = Arrays.asList(
                    resources.get("dry_the_mold"), resources.get("clean_mold_cavities"), resources.get("check_the_hardware"),
                    resources.get("check_eject_com"), resources.get("check_all_eject_com"), resources.get("inspect_cavity")
            );
            List<String> rejectRateDefaultChecklist = Arrays.asList(
                    resources.get("black_dot"), resources.get("bubbles"), resources.get("burn_mark"), resources.get("dented"),
                    resources.get("drag_mark"), resources.get("flashing"), resources.get("flow_mark"), resources.get("gas_mark"), resources.get("loose_burr"),
                    resources.get("missing"), resources.get("oily"), resources.get("parting_line"), resources.get("pin_mark"), resources.get("wrinkle")
            );
            List<Checklist> checklist = new ArrayList<>();
            Mold mold = moldRepository.findById(moldId).orElse(null);
            if (mold != null && mold.getLocation() != null && mold.getLocation().getCompanyId() != null) {
                checklist.addAll(checklistRepository.findAllByCompanyContainsAndChecklistTypeAndObjectTypeAndEnabledIsTrue(mold.getLocation().getCompanyId(), enumChecklistType, CheckListObjectType.CHECK_LIST));
            }
            if (CollectionUtils.isEmpty(checklist)) {
                if (enumChecklistType.equals(ChecklistType.REJECT_RATE)) {
                    Checklist defaultChecklist = new Checklist();
                    defaultChecklist.setChecklistCode("Default Checklist");
                    defaultChecklist.setChecklistItems(rejectRateDefaultChecklist);
                    checklist.add(defaultChecklist);
                } else {
                    Checklist defaultChecklist = new Checklist();
                    defaultChecklist.setChecklistCode("Default Checklist");
                    defaultChecklist.setChecklistItems(maintenanceDefaultChecklist);
                    checklist.add(defaultChecklist);
                }
            }
            return ApiResponse.success(CommonMessage.OK, checklist);
        } catch (Exception e) {
            return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
        }
    }

    @PostMapping("/change-status-in-batch")
    public ApiResponse changeStatusInBatch(@RequestBody BatchUpdateDTO dto) {
        return checklistService.changeStatusInBatch(dto);
    }

    @DataLeakDetector(disabled = true)
    @GetMapping(value = "/list")
    public ApiResponse listCheckList(@RequestParam(value = "type") CheckListObjectType objectType,
                                     @RequestParam(value = "checklistType", required = false) ChecklistType checklistType) {
        return checklistService.listCheckList(objectType, checklistType);
    }

    @GetMapping("/checkListTypeCount")
    public Map getCheckListTypeCount() {
        return checklistService.countCheckListByType();
    }


}
