package vn.com.twendie.avis.mobile.api.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.com.twendie.avis.api.core.util.FileUtils;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.data.enumtype.JourneyDiaryStepEnum;
import vn.com.twendie.avis.data.model.CostType;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.JourneyDiaryCostType;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.mobile.api.adapter.JourneyDiaryCostTypeDTOAdapter;
import vn.com.twendie.avis.mobile.api.model.payload.JourneyDiaryCostPayload;
import vn.com.twendie.avis.mobile.api.model.response.JourneyDiaryCostTypeDTO;
import vn.com.twendie.avis.mobile.api.service.CostTypeService;
import vn.com.twendie.avis.mobile.api.service.JourneyDiaryCostTypeService;
import vn.com.twendie.avis.mobile.api.service.JourneyDiaryService;
import vn.com.twendie.avis.mobile.api.task.UploadImagesTask;
import vn.com.twendie.avis.mobile.api.validation.JourneyDiaryValidator;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import java.io.IOException;
import java.util.Objects;


@RestController
@RequestMapping(value = "/journey-diary-cost-type")
public class JourneyDiaryCostTypeController {

    private final String uploadFolder;

    private final JourneyDiaryService journeyDiaryService;
    private final CostTypeService costTypeService;
    private final JourneyDiaryCostTypeService journeyDiaryCostTypeService;

    private final RabbitTemplate rabbitTemplate;
    private final UploadImagesTask uploadImagesTask;
    private final FileUtils fileUtils;

    private final JourneyDiaryValidator journeyDiaryValidator;

    public JourneyDiaryCostTypeController(@Value("${spring.upload_folder}") String uploadFolder,
                                          JourneyDiaryService journeyDiaryService,
                                          CostTypeService costTypeService,
                                          JourneyDiaryCostTypeService journeyDiaryCostTypeService,
                                          RabbitTemplate rabbitTemplate,
                                          UploadImagesTask uploadImagesTask,
                                          FileUtils fileUtils,
                                          JourneyDiaryValidator journeyDiaryValidator) {
        this.uploadFolder = uploadFolder;
        this.journeyDiaryService = journeyDiaryService;
        this.costTypeService = costTypeService;
        this.journeyDiaryCostTypeService = journeyDiaryCostTypeService;
        this.rabbitTemplate = rabbitTemplate;
        this.uploadImagesTask = uploadImagesTask;
        this.fileUtils = fileUtils;
        this.journeyDiaryValidator = journeyDiaryValidator;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createJourneyDiaryCostType(
            @RequestParam("journey_diary_cost_payload") String journeyDiaryCostPayloadString,
            @RequestParam("image_cost") MultipartFile imageCost,
            @CurrentUser UserDetails userDetails
    ) throws IOException {

        JourneyDiaryCostPayload journeyDiaryCostPayload = journeyDiaryValidator
                .validateJourneyDiaryCostPayload(journeyDiaryCostPayloadString);
        User user = ((UserPrincipal) userDetails).getUser();

        JourneyDiary currentJourneyDiary = journeyDiaryService.findById(user.getCurrentJourneyDiaryId());
        journeyDiaryValidator.validateContractWithDriver(currentJourneyDiary);
        if (Objects.isNull(currentJourneyDiary)) {
            throw new BadRequestException("No current journey diary")
                    .displayMessage(Translator.toLocale("journey_diary.journey_diary_not_start_yet"));
        }
        if (currentJourneyDiary.getStep().equals(JourneyDiaryStepEnum.FINISHED.value())) {
            throw new BadRequestException("Journey diary finished")
                    .displayMessage(Translator.toLocale("journey_diary.journey_diary_finished"));
        }
        if (!Objects.equals(currentJourneyDiary.getId(), journeyDiaryCostPayload.getJourneyDiaryId())) {
            throw new BadRequestException("You are current in another journey diary")
                    .displayMessage(Translator.toLocale("journey_diary.during_another_journey_diary"));
        }

        CostType costType = costTypeService.findByCode(journeyDiaryCostPayload.getCode());
        String imageCostLink = fileUtils.saveWithTimestamp(imageCost, uploadFolder);

        JourneyDiaryCostType journeyDiaryCostType = JourneyDiaryCostType.builder()
                .journeyDiary(currentJourneyDiary)
                .costType(costType)
                .value(journeyDiaryCostPayload.getValue())
                .imageCostLink(imageCostLink)
                .build();

        journeyDiaryCostType = journeyDiaryCostTypeService.save(journeyDiaryCostType);

//        rabbitTemplate.convertAndSend(UPLOAD_IMAGE_JOURNEY_DIARY_COST, journeyDiaryCostType.getId());
        uploadImagesTask.uploadImages();

        JourneyDiaryCostTypeDTO journeyDiaryCostTypeDTO = new JourneyDiaryCostTypeDTOAdapter().apply(journeyDiaryCostType);
        return ResponseEntity.ok(ApiResponse.success(journeyDiaryCostTypeDTO));

    }

}
