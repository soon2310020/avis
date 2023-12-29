package vn.com.twendie.avis.mobile.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.twendie.avis.api.core.util.FileUtils;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.mobile.api.adapter.NotificationDTOAdapter;
import vn.com.twendie.avis.mobile.api.model.response.NotificationDTO;
import vn.com.twendie.avis.mobile.api.model.response.NotificationSettingDTO;
import vn.com.twendie.avis.mobile.api.service.*;
import vn.com.twendie.avis.notification.model.onesignal.NotificationSettingPayload;
import vn.com.twendie.avis.notification.service.NotificationService;
import vn.com.twendie.avis.notification.service.NotificationSettingService;
import vn.com.twendie.avis.security.annotation.RequirePermission;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("signature")
public class JourneyDiaryUserSignatureController {

    private JourneyDiaryUserSignatureService journeyDiaryUserSignatureService;

    private JourneyDiarySignatureService journeyDiarySignatureService;

    private String uploadFolder;

    private NotificationSettingService notificationSettingService;

    private FileUtils fileUtils;

    private NotificationService notificationService;

    private ListUtils listUtils;

    public JourneyDiaryUserSignatureController(JourneyDiaryUserSignatureService journeyDiaryUserSignatureService,
                                               JourneyDiarySignatureService journeyDiarySignatureService,
                                               @Value("${spring.upload_folder}") String uploadFolder,
                                               FileUtils fileUtils,
                                               NotificationSettingService notificationSettingService,
                                               NotificationService notificationService,
                                               ListUtils listUtils) {
        this.journeyDiaryUserSignatureService = journeyDiaryUserSignatureService;
        this.journeyDiarySignatureService = journeyDiarySignatureService;
        this.uploadFolder = uploadFolder;
        this.fileUtils = fileUtils;
        this.notificationSettingService = notificationSettingService;
        this.notificationService = notificationService;
        this.listUtils = listUtils;
    }

    @GetMapping("journey-diary")
    @RequirePermission(acceptedRoles = {"Signature"})
    public ApiResponse<?> get(@CurrentUser UserDetails userDetails,
                              @RequestParam("page") Integer page,
                              @RequestParam("size") Integer size,
                              @RequestParam(value = "from_date") Long fromDate,
                              @RequestParam(value = "to_date") Long toDate) {
        User user = ((UserPrincipal) userDetails).getUser();
        return ApiResponse.success(GeneralPageResponse
                .toResponse(journeyDiaryUserSignatureService.getJourneyDiarySignature(fromDate, toDate, user, page, size)));
    }

    @GetMapping("journey-diary/{journey-diary-signature-id}")
    public ApiResponse<?> get(@PathVariable("journey-diary-signature-id") Long id){
        return ApiResponse.success(journeyDiaryUserSignatureService.getById(id));
    }
    @PutMapping(value = "journey-diary/confirm", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission(acceptedRoles = {"Signature"})
    public HttpEntity<?> confirm(@RequestParam("journey_diary_signature_id") String journeyDiarySignatureId,
                              @RequestParam("signature_image") MultipartFile signatureImage,
                                 @RequestParam("comment") String comment) throws IOException {
        List<Long> journeyDiarySignatureIds = null;
        try {
            journeyDiarySignatureIds = Arrays.stream(journeyDiarySignatureId.split(","))
                    .map(Long::parseLong).collect(Collectors.toList());
            if(journeyDiarySignatureIds.size() == 0){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }catch (NumberFormatException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String signatureUrl = fileUtils.saveWithTimestamp(signatureImage, uploadFolder);
        List<JourneyDiarySignature> journeyDiarySignatures = journeyDiarySignatureService.findByIdIn(journeyDiarySignatureIds);
        journeyDiarySignatures.forEach(e ->{
            e.setStatus(true);
            e.setComment(comment);
            e.setSignatureImageUrl(signatureUrl);
        });

        journeyDiarySignatureService.saveAll(journeyDiarySignatures);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("suggest-user/{contractId}")
    @RequirePermission(acceptedRoles = {"Signature"})
    public ApiResponse<?> getUserSuggest(@CurrentUser UserDetails userDetails,
                                         @PathVariable("contractId") long contractId,
                                         @RequestParam(value = "name", required = false, defaultValue = "") String name) {
        User user = ((UserPrincipal) userDetails).getUser();
        return ApiResponse.success(journeyDiaryUserSignatureService.suggestionMember(user, contractId, name));
    }

    @PutMapping("notification-setting")
    @RequirePermission(acceptedRoles = "Signature")
    public HttpEntity<?> notificationSetting(@CurrentUser UserDetails userDetails,
                                             @RequestBody @Valid NotificationSettingPayload notificationSettingPayload) {
        User user = ((UserPrincipal) userDetails).getUser();
        notificationSettingService.changeSetting(user, notificationSettingPayload);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("notification-setting")
    @RequirePermission(acceptedRoles = "Signature")
    public ApiResponse<?> notificationSetting(@CurrentUser UserDetails userDetails){
        User user = ((UserPrincipal) userDetails).getUser();
        NotificationSetting notificationSetting = notificationSettingService.findByUserId(user.getId());
        NotificationSettingDTO response = null;
        if(notificationSetting != null) {
            response = NotificationSettingDTO.builder()
                    .id(notificationSetting.getId())
                    .day(notificationSetting.isDay())
                    .month(notificationSetting.isMonth())
                    .week(notificationSetting.isWeek())
                    .build();
        }
        return ApiResponse.success(response);
    }

    @GetMapping("notifications")
    @RequirePermission(acceptedRoles = "Signature")
    public ApiResponse<?> getNotification(@Positive @RequestParam(value = "page", defaultValue = "1") int page,
                                          @Positive @RequestParam(value = "size", defaultValue = "10") int size,
                                          @CurrentUser UserDetails userDetails) {
        User user = ((UserPrincipal) userDetails).getUser();
        Page<Notification> notifications = notificationService.findByUserId(user.getId(), page, size);
        Page<NotificationDTO> notificationDTOS = listUtils.transform(notifications, new NotificationDTOAdapter());
        return ApiResponse.success(GeneralPageResponse.toResponse(notificationDTOS));
    }


    @PostMapping("migrate-time")
    public void migrate(){
        journeyDiarySignatureService.migrateTimeStartAndTimeEnd();
    }

}
