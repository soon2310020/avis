package vn.com.twendie.avis.mobile.api.controller;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.FileUtils;
import vn.com.twendie.avis.api.core.util.JsonUtils;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.data.enumtype.*;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.mobile.api.adapter.JourneyDiaryDTOAdapter;
import vn.com.twendie.avis.mobile.api.model.payload.ConfirmStationFeesPayload;
import vn.com.twendie.avis.mobile.api.model.payload.JourneyDiaryPayload;
import vn.com.twendie.avis.mobile.api.model.payload.JourneyDiaryStationFeePayload;
import vn.com.twendie.avis.mobile.api.model.response.JourneyDiaryDTO;
import vn.com.twendie.avis.mobile.api.model.response.JourneyDiaryTotalInfo;
import vn.com.twendie.avis.mobile.api.service.*;
import vn.com.twendie.avis.mobile.api.task.UploadImagesTask;
import vn.com.twendie.avis.mobile.api.validation.JourneyDiaryValidator;
import vn.com.twendie.avis.mobile.api.validation.group.CustomerGetIn;
import vn.com.twendie.avis.mobile.api.validation.group.CustomerGetOut;
import vn.com.twendie.avis.mobile.api.validation.group.FinishTrip;
import vn.com.twendie.avis.mobile.api.validation.group.StartTrip;
import vn.com.twendie.avis.notification.service.NotificationService;
import vn.com.twendie.avis.notification.service.NotificationSettingService;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.data.enumtype.ContractTypeEnum.WITH_DRIVER;
import static vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum.*;
import static vn.com.twendie.avis.data.enumtype.JourneyDiaryStepEnum.*;
import static vn.com.twendie.avis.data.enumtype.VehicleStatusEnum.UNAVAILABLE;

@RestController
@RequestMapping(value = "/journey-diary")
@Slf4j
public class JourneyDiaryController {

    private final String uploadFolder;

    private final UserService userService;
    private final JourneyDiaryService journeyDiaryService;
    private final ContractService contractService;
    private final VehicleService vehicleService;
    private final CostTypeService costTypeService;
    private final JourneyDiaryCostTypeService journeyDiaryCostTypeService;

    private final UploadImagesTask uploadImagesTask;
    private final RabbitTemplate rabbitTemplate;
    private final JsonUtils jsonUtils;
    private final DateUtils dateUtils;
    private final FileUtils fileUtils;
    private final MemberCustomerService memberCustomerService;
    private final UserRoleService userRoleService;
    private final JourneyDiarySignatureService journeyDiarySignatureService;
    private final NotificationService notificationService;
    private final NotificationSettingService notificationSettingService;
    @Value("${account.default-password.signature}")
    private String signatureDefaultPassword;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final JourneyDiaryValidator journeyDiaryValidator;

    public JourneyDiaryController(@Value("${spring.upload_folder}") String uploadFolder,
                                  UserService userService,
                                  JourneyDiaryService journeyDiaryService,
                                  ContractService contractService,
                                  VehicleService vehicleService,
                                  CostTypeService costTypeService,
                                  JourneyDiaryCostTypeService journeyDiaryCostTypeService,
                                  UploadImagesTask uploadImagesTask,
                                  RabbitTemplate rabbitTemplate,
                                  JsonUtils jsonUtils,
                                  DateUtils dateUtils,
                                  FileUtils fileUtils,
                                  JourneyDiaryValidator journeyDiaryValidator,
                                  MemberCustomerService memberCustomerService,
                                  UserRoleService userRoleService,
                                  JourneyDiarySignatureService journeyDiarySignatureService,
                                  NotificationService notificationService,
                                  NotificationSettingService notificationSettingService) {
        this.uploadFolder = uploadFolder;
        this.userService = userService;
        this.journeyDiaryService = journeyDiaryService;
        this.contractService = contractService;
        this.vehicleService = vehicleService;
        this.costTypeService = costTypeService;
        this.journeyDiaryCostTypeService = journeyDiaryCostTypeService;
        this.uploadImagesTask = uploadImagesTask;
        this.rabbitTemplate = rabbitTemplate;
        this.jsonUtils = jsonUtils;
        this.dateUtils = dateUtils;
        this.fileUtils = fileUtils;
        this.journeyDiaryValidator = journeyDiaryValidator;
        this.memberCustomerService = memberCustomerService;
        this.userRoleService = userRoleService;
        this.journeyDiarySignatureService = journeyDiarySignatureService;
        this.notificationService = notificationService;
        this.notificationSettingService = notificationSettingService;
    }

    @GetMapping(value = "/total-info/{journey_diary_id}")
    public ResponseEntity<?> getJourneyDiaryTotalInfo(@PathVariable("journey_diary_id") Long id) {
        JourneyDiaryTotalInfo journeyDiaryTotalInfo = journeyDiaryService.getJourneyDiaryTotalInfo(id);
        return ResponseEntity.ok((ApiResponse.success(journeyDiaryTotalInfo)));

    }

    @Transactional
    @PostMapping(value = "/start-trip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> startJourneyDiary(
            @RequestParam("start_trip_payload") String startTripPayload,
            @RequestParam("odo_start_image") MultipartFile odoStartImage,
            @CurrentUser UserDetails userDetails
    ) throws IOException {
        User user = ((UserPrincipal) userDetails).getUser();

        JourneyDiaryPayload journeyDiaryPayload = journeyDiaryValidator
                .validateJourneyDiaryPayload(startTripPayload, StartTrip.class);
        journeyDiaryValidator.validateDriverInContract(user);

        Contract currentContract = contractService.findById(user.getCurrentContractId());
        journeyDiaryValidator.validateContractInProgress(currentContract);

        Vehicle vehicle = currentContract.getVehicle();
        JourneyDiary currentJourneyDiary = journeyDiaryService.findById(user.getCurrentJourneyDiaryId());

        if (!journeyDiaryValidator.checkCurrentJourneyDiary(currentJourneyDiary, journeyDiaryPayload, STARTED)) {
            JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(currentJourneyDiary);
            return ResponseEntity.ok().body(ApiResponse.fail(journeyDiaryDTO));
        }

        String imageOdoLinkStart = fileUtils.saveWithTimestamp(odoStartImage, uploadFolder);

        JourneyDiary journeyDiary = JourneyDiary.builder()
                .contract(currentContract)
                .vehicle(vehicle)
                .driver(user)
                .timeStart(dateUtils.now())
                .addressStart(journeyDiaryPayload.getAddressStart())
                .kmOdoStart(journeyDiaryPayload.getKmOdoStart())
                .kmDriverStart(journeyDiaryPayload.getKmDriverStart())
                .imageOdoLinkStart(imageOdoLinkStart)
                .flagUnavailableVehicle(vehicle.getStatus().equals(UNAVAILABLE.getValue()))
                .step(JourneyDiaryStepEnum.STARTED.value())
                .build();

        journeyDiary = journeyDiaryService.save(journeyDiary);

        userService.assignToJourneyDiary(journeyDiary.getDriver(), journeyDiary);
        userService.save(journeyDiary.getDriver());
        vehicleService.assignToJourneyDiary(journeyDiary.getVehicle(), journeyDiary);

//        rabbitTemplate.convertAndSend(UPLOAD_IMAGE_START_TRIP, journeyDiary.getId());
        uploadImagesTask.uploadImages();

        JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(journeyDiary);
        return ResponseEntity.ok(ApiResponse.success(journeyDiaryDTO));

    }


    @Transactional
    @PatchMapping(value = "/take-customer/{journey_diary_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> takeCustomerV1(
            @PathVariable("journey_diary_id") Long id,
            @RequestParam("take_customer_payload") String takeCustomerPayload,
            @RequestParam("customer_image") MultipartFile customerImage,
            @RequestParam("odo_customer_image") MultipartFile odoCustomerImage,
            @CurrentUser UserDetails userDetails
    ) throws IOException {

        User user = ((UserPrincipal) userDetails).getUser();

        JourneyDiaryPayload journeyDiaryPayload = journeyDiaryValidator
                .validateJourneyDiaryPayload(takeCustomerPayload, CustomerGetIn.class);
        journeyDiaryPayload.setId(id);

        JourneyDiary currentJourneyDiary = journeyDiaryService.findById(user.getCurrentJourneyDiaryId());

        if (!journeyDiaryValidator.checkCurrentJourneyDiary(currentJourneyDiary, journeyDiaryPayload, CUSTOMER_GOT_IN)) {
            JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(currentJourneyDiary);
            return ResponseEntity.ok(ApiResponse.fail(ObjectUtils.defaultIfNull(journeyDiaryDTO, Collections.EMPTY_MAP)));
        }

        journeyDiaryValidator.validateContractWithDriver(currentJourneyDiary);
        journeyDiaryValidator.validateKmDriver(currentJourneyDiary, journeyDiaryPayload, CUSTOMER_GOT_IN);

        String imageCustomerGetInLink = fileUtils.saveWithTimestamp(customerImage, uploadFolder);
        String imageOdoLinkCustomerGetIn = fileUtils.saveWithTimestamp(odoCustomerImage, uploadFolder);

        JourneyDiary journeyDiary = currentJourneyDiary.toBuilder()
                .timeCustomerGetIn(dateUtils.now())
                .addressCustomerGetIn(journeyDiaryPayload.getAddressCustomerGetIn())
                .customerNameUsed(journeyDiaryPayload.getCustomerNameUsed())
                .customerDepartment(journeyDiaryPayload.getCustomerDepartment())
                .kmOdoCustomerGetIn(journeyDiaryPayload.getKmOdoCustomerGetIn())
                .kmDriverCustomerGetIn(journeyDiaryPayload.getKmDriverCustomerGetIn())
                .imageOdoLinkCustomerGetIn(imageOdoLinkCustomerGetIn)
                .imageCustomerGetInLink(imageCustomerGetInLink)
                .step(JourneyDiaryStepEnum.CUSTOMER_GOT_IN.value())
                .build();

        journeyDiary = journeyDiaryService.save(journeyDiary);

//        rabbitTemplate.convertAndSend(UPLOAD_IMAGE_CUSTOMER_GET_IN, journeyDiary.getId());
        uploadImagesTask.uploadImages();

        JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(journeyDiary);
        return ResponseEntity.ok(ApiResponse.success(journeyDiaryDTO));

    }


    @Transactional
    @PatchMapping(value = "/take-customer-v1/{journey_diary_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> takeCustomer(
            @PathVariable("journey_diary_id") Long id,
            @RequestParam("take_customer_payload") String takeCustomerPayload,
            @RequestParam(value = "customer_image", required = false) MultipartFile customerImage,
            @RequestParam(value = "odo_customer_image", required = false) MultipartFile odoCustomerImage,
            @CurrentUser UserDetails userDetails
    ) throws IOException {

        User user = ((UserPrincipal) userDetails).getUser();

        JourneyDiaryPayload journeyDiaryPayload = journeyDiaryValidator
                .validateJourneyDiaryPayload(takeCustomerPayload, CustomerGetIn.class);
        journeyDiaryPayload.setId(id);

        JourneyDiary currentJourneyDiary = journeyDiaryService.findById(user.getCurrentJourneyDiaryId());

        if (!journeyDiaryValidator.checkCurrentJourneyDiary(currentJourneyDiary, journeyDiaryPayload, CUSTOMER_GOT_IN)) {
            JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(currentJourneyDiary);
            return ResponseEntity.ok(ApiResponse.fail(ObjectUtils.defaultIfNull(journeyDiaryDTO, Collections.EMPTY_MAP)));
        }

        journeyDiaryValidator.validateContractWithDriver(currentJourneyDiary);
        journeyDiaryValidator.validateKmDriver(currentJourneyDiary, journeyDiaryPayload, CUSTOMER_GOT_IN);
        String imageCustomerGetInLink = null;
        if(customerImage != null) {
            imageCustomerGetInLink = fileUtils.saveWithTimestamp(customerImage, uploadFolder);
        }

        String imageOdoLinkCustomerGetIn = null;
        if(odoCustomerImage != null){
            imageOdoLinkCustomerGetIn = fileUtils.saveWithTimestamp(odoCustomerImage, uploadFolder);
        }
        JourneyDiary journeyDiary = currentJourneyDiary.toBuilder()
                .timeCustomerGetIn(dateUtils.now())
                .addressCustomerGetIn(journeyDiaryPayload.getAddressCustomerGetIn())
                .customerNameUsed(journeyDiaryPayload.getCustomerNameUsed())
                .customerDepartment(journeyDiaryPayload.getCustomerDepartment())
                .kmOdoCustomerGetIn(journeyDiaryPayload.getKmOdoCustomerGetIn())
                .kmDriverCustomerGetIn(journeyDiaryPayload.getKmDriverCustomerGetIn())
                .imageOdoLinkCustomerGetIn(imageOdoLinkCustomerGetIn)
                .imageCustomerGetInLink(imageCustomerGetInLink)
                .step(JourneyDiaryStepEnum.CUSTOMER_GOT_IN.value())
                .build();

        JourneyDiarySignature journeyDiarySignature = new JourneyDiarySignature();
        journeyDiarySignature.setJourneyDiary(journeyDiary);
        journeyDiarySignature.setTimeStart(journeyDiary.getTimeStart());
        journeyDiarySignature.setStatus(false);
        if (journeyDiaryPayload.getUserCustomerId() != null) {
            MemberCustomer memberCustomer = memberCustomerService.findById(journeyDiaryPayload.getUserCustomerId()).orElse(null);
            if (Objects.isNull(memberCustomer)) {
                throw new BadRequestException("Customer not exists !!!")
                        .code(HttpStatus.BAD_REQUEST.value())
                        .displayMessage(Translator.toLocale("member_customer.name.not_exists"));
            }
            journeyDiarySignature.setMemberCustomer(memberCustomer);
        } else {
            if (memberCustomerService.countByNameAndDepartment(journeyDiaryPayload.getCustomerNameUsed(),
                    journeyDiaryPayload.getCustomerDepartment()) > 0) {
                throw new BadRequestException("Customer name exist !!!")
                        .code(HttpStatus.BAD_REQUEST.value())
                        .displayMessage(Translator.toLocale("member_customer.name.exists"));
            }

            Contract contract = contractService.findById(user.getCurrentContractId());
            if(contract == null){
                throw new BadRequestException("Contract not found !!!")
                        .code(HttpStatus.BAD_REQUEST.value())
                        .displayMessage(Translator.toLocale("contract.not_found"));
            }

            String memberCustomerCode = memberCustomerService.generateMemberCode(0).get(0);
            MemberCustomer memberCustomer = new MemberCustomer();
            memberCustomer.setCode(memberCustomerCode);
            memberCustomer.setCustomer(contract.getCustomer());
            memberCustomer.setParent(contract.getMemberCustomer());
            memberCustomer.setName(journeyDiaryPayload.getCustomerNameUsed());
            memberCustomer.setDepartment(journeyDiaryPayload.getCustomerDepartment());
            memberCustomer.setRole(MemberCustomerRoleEnum.IGNORE.getCode());
            memberCustomer.setActive(false);

            memberCustomerService.save(memberCustomer);

            //        create journey diary signature
            journeyDiarySignature.setMemberCustomer(memberCustomer);
        }
        journeyDiary = journeyDiaryService.save(journeyDiary);
        journeyDiarySignatureService.save(journeyDiarySignature);
//        rabbitTemplate.convertAndSend(UPLOAD_IMAGE_CUSTOMER_GET_IN, journeyDiary.getId());
        uploadImagesTask.uploadImages();

        JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(journeyDiary);
        return ResponseEntity.ok(ApiResponse.success(journeyDiaryDTO));

    }

    @Transactional
    @PatchMapping(value = "/customer-get-out/{journey_diary_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> customerGetOut(
            @PathVariable("journey_diary_id") Long id,
            @RequestParam("customer_get_out_payload") String customerGetOutPayload,
            @RequestParam(value = "odo_customer_get_out_image", required = false) MultipartFile odoCustomerGetOutImage,
            @RequestParam(value = "customer_get_out_image", required = false) MultipartFile customerGetOutImage,
            @CurrentUser UserDetails userDetails
    ) throws IOException {

        User user = ((UserPrincipal) userDetails).getUser();

        JourneyDiaryPayload journeyDiaryPayload = journeyDiaryValidator
                .validateJourneyDiaryPayload(customerGetOutPayload, CustomerGetOut.class);
        journeyDiaryPayload.setId(id);

        JourneyDiary currentJourneyDiary = journeyDiaryService.findById(user.getCurrentJourneyDiaryId());

        if (!journeyDiaryValidator.checkCurrentJourneyDiary(currentJourneyDiary, journeyDiaryPayload, CUSTOMER_GOT_OUT)) {
            JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(currentJourneyDiary);
            return ResponseEntity.ok(ApiResponse.fail(ObjectUtils.defaultIfNull(journeyDiaryDTO, Collections.EMPTY_MAP)));
        }

        journeyDiaryValidator.validateContractWithDriver(currentJourneyDiary);
        journeyDiaryValidator.validateKmDriver(currentJourneyDiary, journeyDiaryPayload, CUSTOMER_GOT_OUT);
        String imageOdoLinkCustomerGetOut = null;
        if(odoCustomerGetOutImage != null) {
            imageOdoLinkCustomerGetOut = fileUtils.saveWithTimestamp(odoCustomerGetOutImage, uploadFolder);
        }
        
        String imageCustomerGetOutLink = null;
        if(customerGetOutImage != null) {
            imageCustomerGetOutLink = fileUtils.saveWithTimestamp(customerGetOutImage, uploadFolder);
        }

        JourneyDiary journeyDiary = currentJourneyDiary.toBuilder()
                .timeCustomerGetOut(dateUtils.now())
                .addressCustomerGetOut(journeyDiaryPayload.getAddressCustomerGetOut())
                .kmOdoCustomerGetOut(journeyDiaryPayload.getKmOdoCustomerGetOut())
                .kmDriverCustomerGetOut(journeyDiaryPayload.getKmDriverCustomerGetOut())
                .imageOdoLinkCustomerGetOut(imageOdoLinkCustomerGetOut)
                .imageCustomerGetOutLink(imageCustomerGetOutLink)
                .step(JourneyDiaryStepEnum.CUSTOMER_GOT_OUT.value())
                .build();

        journeyDiary = journeyDiaryService.save(journeyDiary);

//        rabbitTemplate.convertAndSend(UPLOAD_IMAGE_CUSTOMER_GET_OUT, journeyDiary.getId());
        uploadImagesTask.uploadImages();

        JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(journeyDiary);
        return ResponseEntity.ok(ApiResponse.success(journeyDiaryDTO));

    }

    @Transactional
    @PatchMapping(value = "/finish-trip/{journey_diary_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> finishTrip(
            @PathVariable("journey_diary_id") Long id,
            @RequestParam("finish_trip_payload") String finishTripPayload,
            @RequestParam("odo_finish_image") MultipartFile odoFinishImage,
            @CurrentUser UserDetails userDetails
    ) throws IOException {

        User user = ((UserPrincipal) userDetails).getUser();

        JourneyDiaryPayload journeyDiaryPayload = journeyDiaryValidator
                .validateJourneyDiaryPayload(finishTripPayload, FinishTrip.class);
        journeyDiaryPayload.setId(id);

        JourneyDiary currentJourneyDiary = journeyDiaryService.findById(user.getCurrentJourneyDiaryId());
        if (!journeyDiaryValidator.checkCurrentJourneyDiary(currentJourneyDiary, journeyDiaryPayload, FINISHED)) {
            JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(currentJourneyDiary);
            return ResponseEntity.ok(ApiResponse.fail(ObjectUtils.defaultIfNull(journeyDiaryDTO, Collections.EMPTY_MAP)));
        }

        journeyDiaryValidator.validateKmDriver(currentJourneyDiary, journeyDiaryPayload, FINISHED);

        String imageOdoLinkEnd = fileUtils.saveWithTimestamp(odoFinishImage, uploadFolder);

        Timestamp currentTimestamp = dateUtils.now();

        JourneyDiary journeyDiary = currentJourneyDiary.toBuilder()
                .timeEnd(currentTimestamp)
                .addressEnd(journeyDiaryPayload.getAddressEnd())
                .kmOdoEnd(journeyDiaryPayload.getKmOdoEnd())
                .kmDriverEnd(journeyDiaryPayload.getKmDriverEnd())
                .imageOdoLinkEnd(imageOdoLinkEnd)
                .step(JourneyDiaryStepEnum.FINISHED.value())
                .build();

        journeyDiary = journeyDiaryService.save(journeyDiary);
        JourneyDiarySignature journeyDiarySignature = journeyDiarySignatureService.findByJourneyDiary(journeyDiary);
        if(journeyDiarySignature != null && journeyDiarySignature.getMemberCustomer() != null){
            boolean pushNotification = false;
            journeyDiarySignature.setTimeEnd(journeyDiary.getTimeEnd());
            journeyDiarySignatureService.save(journeyDiarySignature);
            User userPushNotification = journeyDiarySignature.getMemberCustomer().getUser();
            if(userPushNotification != null) {
                NotificationSetting notificationSetting = notificationSettingService.findByUserId(userPushNotification.getId());
                pushNotification = notificationSetting != null && notificationSetting.isDay();
                String dateEndStr = dateUtils.format(journeyDiary.getTimeEnd() != null  ? journeyDiary.getTimeEnd().getTime() : 0, "HH:mm:ss dd/MM/yyyy");
                notificationService.pushNotificationSignature(userPushNotification, NotificationContentEnum.END_JOURNEY_DIARY_SIGNATURE_DAY,
                        NotificationSettingTypeEnum.DAY, journeyDiarySignature.getId(), null, null, pushNotification, dateEndStr);
            }
        }

        userService.unAssignFromJourneyDiary(journeyDiary.getDriver());
        userService.save(journeyDiary.getDriver());
        vehicleService.unAssignFromJourneyDiary(journeyDiary.getVehicle());

//        rabbitTemplate.convertAndSend(UPLOAD_IMAGE_FINISH_TRIP, journeyDiary.getId());
//        rabbitTemplate.convertAndSend(UPDATE_TOTAL_KM_GPS_JOURNEY_DIARY, journeyDiary.getId());
        uploadImagesTask.uploadImages();

        Contract contract = currentJourneyDiary.getContract();
        if (contract.getContractType().getId().equals(WITH_DRIVER.value())) {
            journeyDiaryPayload.getJourneyDiaryCosts().stream()
                    .filter(journeyDiaryCostPayload -> NIGHT_STORAGE_FEE.code()
                            .equals(journeyDiaryCostPayload.getCode()))
                    .forEach(journeyDiaryCostPayload -> {
                        CostType costType = costTypeService.findByCode(journeyDiaryCostPayload.getCode());
                        JourneyDiaryCostType journeyDiaryCostType = JourneyDiaryCostType.builder()
                                .journeyDiary(currentJourneyDiary)
                                .costType(costType)
                                .value(journeyDiaryCostPayload.getValue())
                                .build();
                        journeyDiaryCostTypeService.save(journeyDiaryCostType);
                    });
        }

        JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(journeyDiary);
        return ResponseEntity.ok(ApiResponse.success(journeyDiaryDTO));

    }

    @PatchMapping(value = "/finish-trip-soon/{journey_diary_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> finishTripSoon(
            @PathVariable("journey_diary_id") Long id,
            @RequestParam("finish_trip_payload") String finishTripSoonPayload,
            @RequestParam("odo_finish_image") MultipartFile odoFinishImage,
            @RequestParam("breakdown_image") MultipartFile breakdownImage,
            @CurrentUser UserDetails userDetails
    ) throws IOException {

        User user = ((UserPrincipal) userDetails).getUser();

        JourneyDiaryPayload journeyDiaryPayload = jsonUtils.toObject(finishTripSoonPayload, JourneyDiaryPayload.class);
        journeyDiaryPayload.setId(id);

        JourneyDiary currentJourneyDiary = journeyDiaryService.findById(user.getCurrentJourneyDiaryId());

        if (!journeyDiaryValidator.checkCurrentJourneyDiary(currentJourneyDiary, journeyDiaryPayload, BREAKDOWN)) {
            JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(currentJourneyDiary);
            return ResponseEntity.ok(ApiResponse.fail(ObjectUtils.defaultIfNull(journeyDiaryDTO, Collections.EMPTY_MAP)));
        }

        journeyDiaryValidator.validateContractWithDriver(currentJourneyDiary);
        journeyDiaryValidator.validateKmDriver(currentJourneyDiary, journeyDiaryPayload, FINISHED);

        String imageOdoLinkEnd = fileUtils.saveWithTimestamp(odoFinishImage, uploadFolder);
        String imageBreakdownLink = fileUtils.saveWithTimestamp(breakdownImage, uploadFolder);

        Timestamp currentTimestamp = dateUtils.now();

        JourneyDiary journeyDiary = currentJourneyDiary.toBuilder()
                .timeEnd(currentTimestamp)
                .addressEnd(journeyDiaryPayload.getAddressEnd())
                .kmOdoEnd(journeyDiaryPayload.getKmOdoEnd())
                .kmDriverEnd(journeyDiaryPayload.getKmDriverEnd())
                .imageOdoLinkEnd(imageOdoLinkEnd)
                .timeBreakdown(Objects.nonNull(imageBreakdownLink) ? currentTimestamp : null)
                .step(JourneyDiaryStepEnum.FINISHED.value())
                .build();

        journeyDiary = journeyDiaryService.save(journeyDiary);
        userService.unAssignFromJourneyDiary(journeyDiary.getDriver());
        userService.save(journeyDiary.getDriver());
        vehicleService.unAssignFromJourneyDiary(journeyDiary.getVehicle());

//        rabbitTemplate.convertAndSend(UPLOAD_IMAGE_FINISH_TRIP, journeyDiary.getId());
//        rabbitTemplate.convertAndSend(UPDATE_TOTAL_KM_GPS_JOURNEY_DIARY, journeyDiary.getId());
        uploadImagesTask.uploadImages();

        journeyDiaryPayload.getJourneyDiaryCosts().stream()
                .filter(journeyDiaryCostPayload -> Arrays.asList(NIGHT_STORAGE_FEE.code(), BREAKDOWN_FEE.code())
                        .contains(journeyDiaryCostPayload.getCode()))
                .forEach(journeyDiaryCostPayload -> {
                    CostType costType = costTypeService.findByCode(journeyDiaryCostPayload.getCode());
                    String imageCostLink = null;
                    if (costType.getCode().equals(BREAKDOWN_FEE.code())) {
                        try {
                            imageCostLink = fileUtils.saveWithTimestamp(breakdownImage, uploadFolder);
                        } catch (IOException ignored) {
                        }
                    }

                    JourneyDiaryCostType journeyDiaryCostType = JourneyDiaryCostType.builder()
                            .journeyDiary(currentJourneyDiary)
                            .costType(costType)
                            .value(journeyDiaryCostPayload.getValue())
                            .imageCostLink(imageCostLink)
                            .build();
                    journeyDiaryCostType = journeyDiaryCostTypeService.save(journeyDiaryCostType);

                    if (Objects.nonNull(imageCostLink)) {
//                        rabbitTemplate.convertAndSend(UPLOAD_IMAGE_JOURNEY_DIARY_COST, journeyDiaryCostType.getId());
                        uploadImagesTask.uploadImages();
                    }
                });

        JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(journeyDiary);
        return ResponseEntity.ok(ApiResponse.success(journeyDiaryDTO));

    }

    @Transactional
    @PutMapping(value = "/confirm-station-fees/{journey_diary_id}")
    public ResponseEntity<?> confirmStationFees(@PathVariable("journey_diary_id") Long id,
                                                @RequestParam("confirm_station_fees_payload") String payload,
                                                @RequestParam("station_fee_images") MultipartFile[] stationFeeImages,
                                                @RequestParam(value = "screenshot", required = false) MultipartFile screenshot,
                                                @CurrentUser UserDetails userDetails) throws IOException {
        User user = ((UserPrincipal) userDetails).getUser();
        JourneyDiary currentJourneyDiary = journeyDiaryService.findById(user.getCurrentJourneyDiaryId());
        if (!id.equals(user.getCurrentJourneyDiaryId())) {
            JourneyDiaryDTO journeyDiaryDTO = new JourneyDiaryDTOAdapter().apply(currentJourneyDiary);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ObjectUtils.defaultIfNull(journeyDiaryDTO, Collections.EMPTY_MAP)));
        }
        ConfirmStationFeesPayload confirmStationFeesPayload = journeyDiaryValidator.validateConfirmStationFeesPayload(payload);
        Map<Long, JourneyDiaryStationFeePayload> journeyDiaryStationFeesMap = confirmStationFeesPayload.getJourneyDiaryStationFees()
                .stream()
                .collect(Collectors.toMap(JourneyDiaryStationFeePayload::getId, x -> x));
        journeyDiaryService.fetchJourneyDiaryStationFees(currentJourneyDiary);
        currentJourneyDiary.getJourneyDiaryStationFees()
                .forEach(journeyDiaryStationFee -> {
                    JourneyDiaryStationFeePayload journeyDiaryStationFeePayload = journeyDiaryStationFeesMap
                            .get(journeyDiaryStationFee.getId());
                    if (Objects.nonNull(journeyDiaryStationFeePayload)) {
                        journeyDiaryStationFee.setStationConfirm(journeyDiaryStationFeePayload.getStationConfirm());
                        journeyDiaryStationFee.setFeeConfirm(journeyDiaryStationFeePayload.getFeeConfirm());
                    }
                });
        if (stationFeeImages != null && stationFeeImages.length > 0) {
            List<String> fileNames = fileUtils.saveWithTimestamp(Lists.newArrayList(stationFeeImages), uploadFolder);
            currentJourneyDiary.setStationFeeImages(String.join("\n", fileNames));
        }
        if (screenshot != null) {
            String fileName = fileUtils.saveWithTimestamp(screenshot, uploadFolder);
            currentJourneyDiary.setScreenshot(fileName);
        }
        currentJourneyDiary.setNote(confirmStationFeesPayload.getNote());
        return ResponseEntity.ok(ApiResponse.success(Collections.emptyMap()));
    }

}
