package saleson.api.systemNote;


import com.emoldino.framework.dto.SuccessOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saleson.api.notification.NotificationService;
import saleson.api.systemNote.dto.SystemNoteOut;
import saleson.api.systemNote.payload.SystemNotePayload;
import saleson.api.user.UserService;
import saleson.api.user.payload.UserParam;
import saleson.common.payload.ApiResponse;
import saleson.common.payload.ApiResponseEntity;
import saleson.common.util.SecurityUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.dto.RestData;
import saleson.dto.SystemNoteParam;
import saleson.dto.UserLiteDTO;
import saleson.model.SystemNote;
import saleson.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system-note")
public class SystemNoteController {

    @Autowired
    SystemNoteService systemNoteService;
    @Autowired
    SystemNoteRepository systemNoteRepository;
    @Autowired
    UserService userService;
    @Autowired
    NotificationService notificationService;

    @GetMapping("/list")
    public ResponseEntity getSystemNotes(SystemNotePayload payload) {
//        List<SystemNote> resList = systemNoteRepository.findAllBySystemNoteFunctionAndObjectFunctionIdAndDeleted(payload.getSystemNoteFunction(), payload.getObjectFunctionId(), payload.isTrashBin());
        Page<SystemNote> systemNotePage=systemNoteService.findAll(payload.getPredicate(),PageRequest.of(0,Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC,"createdAt")));
        Long totalUnread = systemNoteService.countTotalUnread(payload.getUnreadPredicate());
        //        return resList;
        return ApiResponseEntity.ok(new SystemNoteOut(systemNotePage.getContent(),systemNotePage.getTotalElements(),totalUnread));

    }

    @PostMapping("/create-system-note")
    public ResponseEntity create(@RequestBody SystemNotePayload payload) {
        try {
            SystemNote systemNote = systemNoteService.save(payload.getModel(), payload);
            return new ResponseEntity<>(systemNote, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Fail!");
        }
    }

    @PostMapping("/batch-create")
    public ApiResponse createBatch(@RequestBody SystemNotePayload payload) {
        return systemNoteService.batchSave(payload);
    }
    @PutMapping("{id}")
    public ResponseEntity update(@PathVariable("id") Long id, @RequestBody SystemNotePayload payload) {
        try {
            SystemNote systemNoteOld = systemNoteRepository.findById(id).orElse(null);
            if (systemNoteOld == null) {
                return ResponseEntity.badRequest().body("Note not found!");
            }
            List<SystemNoteParam> systemNoteParamList =  systemNoteOld.getSystemNoteParamList();
            SystemNote systemNote = systemNoteService.save(payload.getModel(systemNoteOld), null);
            notificationService.createNoteNotification(systemNote, systemNoteParamList);

            return new ResponseEntity<>(systemNote, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Fail!");
        }
    }

    @DeleteMapping("{id}")
    public ApiResponse deleteSystemNote(@PathVariable("id") Long id) {
        SystemNote systemNote = systemNoteRepository.findById(id).orElse(null);
        if (systemNote == null) {
            return new ApiResponse(false, "Fail!");
        }
        if(systemNote.getCreator()!=null && SecurityUtils.getUserId()!=null && !SecurityUtils.getUserId().equals(systemNote.getCreator().getId())){
            return new ApiResponse(false, "User can only remove owner message!");
        }
        systemNoteService.deleteSystemNote(systemNote);
        return new ApiResponse(true, "OK!");
    }
    @PostMapping("/restore/{id}")
    public ApiResponse restoreSystemNote(@PathVariable("id") Long id) {
        SystemNote systemNote = systemNoteRepository.findById(id).orElse(null);
        if (systemNote == null) {
            return new ApiResponse(false, "Fail!");
        }
        systemNoteService.restoreSystemNote(systemNote);

        return new ApiResponse(true, "OK!");
    }

    @GetMapping("/user-lite-list")
    public ResponseEntity getUserLiteListAll() {
        List<UserLiteDTO> liteDTOS = new ArrayList<>();
        UserParam param = new UserParam();
        param.setStatus("active");
        Page<User> pageContent = userService.findAll(param, PageRequest.of(0,Integer.MAX_VALUE));
        if(pageContent!=null){
            liteDTOS = pageContent.getContent().stream().map(u-> new UserLiteDTO(u)).collect(Collectors.toList());
        }
//        return liteDTOS;
        return ApiResponseEntity.ok(new RestData(liteDTOS.size(), liteDTOS));

    }

    @GetMapping("/search-object-by-code")
    public ResponseEntity searchObjectByCode(@RequestParam(value = "code", required = false) String code){
        List<SystemNoteParam> systemNoteParamList= systemNoteService.searchObjectByCode(code);
        return ApiResponseEntity.ok(new RestData(systemNoteParamList.size(), systemNoteParamList));

    }

    /**
     * show detail note
     * Id of system note or reply note
     * @param id
     * @return
     */
    @GetMapping("{id}/detail")
    public ResponseEntity detail(@PathVariable("id") Long id) {
        try {
//            List<Long> toReads = new ArrayList<>();
            SystemNote systemNote = systemNoteRepository.findById(id).orElse(null);
            if (systemNote == null) {
                return ResponseEntity.badRequest().body("Note not found!");
            }
//            toReads.add(systemNote.getId());
            if(systemNote.getParentId()!=null){
                systemNote = systemNoteRepository.findById(systemNote.getParentId()).orElse(null);
                if (systemNote == null) {
                    return ResponseEntity.badRequest().body("Note not found!");
                }
//                toReads.add(systemNote.getId());
            }

            systemNoteService.checkUnreadStatus(Arrays.asList(systemNote), SecurityUtils.getUserId());
//            systemNoteService.updateRead(toReads);
            return new ResponseEntity<>(systemNote, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Fail!");
        }
    }

    @PostMapping("/read")
    public SuccessOut updateRead(@RequestBody BatchUpdateDTO batchUpdateDTO){
        systemNoteService.updateRead(batchUpdateDTO.getIds());
        return SuccessOut.getDefault();
    }


}
