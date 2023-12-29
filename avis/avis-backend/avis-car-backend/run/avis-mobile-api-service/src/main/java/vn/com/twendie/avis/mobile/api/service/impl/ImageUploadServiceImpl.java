package vn.com.twendie.avis.mobile.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.JourneyDiaryCostType;
import vn.com.twendie.avis.mobile.api.service.ImageUploadService;
import vn.com.twendie.avis.mobile.api.service.JourneyDiaryCostTypeService;
import vn.com.twendie.avis.mobile.api.service.JourneyDiaryService;
import vn.com.twendie.avis.mobile.api.support.FtpService;

import static vn.com.twendie.avis.queue.constant.QueueConstant.RoutingKeys.*;

@Slf4j
@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    private final JourneyDiaryService journeyDiaryService;
    private final JourneyDiaryCostTypeService journeyDiaryCostTypeService;

    private final FtpService ftpService;

    public ImageUploadServiceImpl(JourneyDiaryService journeyDiaryService,
                                  JourneyDiaryCostTypeService journeyDiaryCostTypeService,
                                  FtpService ftpService) {
        this.journeyDiaryService = journeyDiaryService;
        this.journeyDiaryCostTypeService = journeyDiaryCostTypeService;
        this.ftpService = ftpService;
    }

    @Override
    @RabbitListener(queues = UPLOAD_IMAGE_START_TRIP)
    public void uploadImageStartTrip(Long journeyDiaryId) {
        try {
            Thread.sleep(10000);
            JourneyDiary journeyDiary = journeyDiaryService.findById(journeyDiaryId);
            String imageOdoLinkStart = ftpService.uploadFile(journeyDiary.getImageOdoLinkStart());
//            journeyDiary.setImageOdoLinkStart(imageOdoLinkStart);
//            journeyDiaryService.save(journeyDiary);
        } catch (Exception e) {
            log.error("Error upload image start trip with id {}: {}", journeyDiaryId, ExceptionUtils.getRootCauseMessage(e));
        }
    }

    @Override
    @RabbitListener(queues = UPLOAD_IMAGE_CUSTOMER_GET_IN)
    public void uploadImageCustomerGetIn(Long journeyDiaryId) {
        try {
            Thread.sleep(10000);
            JourneyDiary journeyDiary = journeyDiaryService.findById(journeyDiaryId);
            String imageOdoLinkCustomerGetIn = ftpService.uploadFile(journeyDiary.getImageOdoLinkCustomerGetIn());
            String imageCustomerGetInLink = ftpService.uploadFile(journeyDiary.getImageCustomerGetInLink());
            journeyDiary.setImageOdoLinkCustomerGetIn(imageOdoLinkCustomerGetIn);
//            journeyDiary.setImageCustomerGetInLink(imageCustomerGetInLink);
//            journeyDiaryService.save(journeyDiary);
        } catch (Exception e) {
            log.error("Error upload image customer get in with id {}: {}", journeyDiaryId, ExceptionUtils.getRootCauseMessage(e));
        }
    }

    @Override
    @RabbitListener(queues = UPLOAD_IMAGE_CUSTOMER_GET_OUT)
    public void uploadImageCustomerGetOut(Long journeyDiaryId) {
        try {
            Thread.sleep(10000);
            JourneyDiary journeyDiary = journeyDiaryService.findById(journeyDiaryId);
            String imageOdoLinkCustomerGetOut = ftpService.uploadFile(journeyDiary.getImageOdoLinkCustomerGetOut());
            String imageCustomerGetOutLink = ftpService.uploadFile(journeyDiary.getImageCustomerGetOutLink());
            journeyDiary.setImageOdoLinkCustomerGetOut(imageOdoLinkCustomerGetOut);
//            journeyDiary.setImageCustomerGetOutLink(imageCustomerGetOutLink);
//            journeyDiaryService.save(journeyDiary);
        } catch (Exception e) {
            log.error("Error upload image customer get out with id {}: {}", journeyDiaryId, ExceptionUtils.getRootCauseMessage(e));
        }
    }

    @Override
    @RabbitListener(queues = UPLOAD_IMAGE_FINISH_TRIP)
    public void uploadImageFinishTrip(Long journeyDiaryId) {
        try {
            Thread.sleep(10000);
            JourneyDiary journeyDiary = journeyDiaryService.findById(journeyDiaryId);
            String imageOdoLinkEnd = ftpService.uploadFile(journeyDiary.getImageOdoLinkEnd());
//            journeyDiary.setImageOdoLinkEnd(imageOdoLinkEnd);
//            journeyDiaryService.save(journeyDiary);
        } catch (Exception e) {
            log.error("Error upload image finish trip with id {}: {}", journeyDiaryId, ExceptionUtils.getRootCauseMessage(e));
        }
    }

    @Override
    @RabbitListener(queues = UPLOAD_IMAGE_JOURNEY_DIARY_COST)
    public void uploadImageJourneyDiaryCost(Long journeyDiaryCostId) {
        try {
            Thread.sleep(10000);
            JourneyDiaryCostType journeyDiaryCostType = journeyDiaryCostTypeService.findById(journeyDiaryCostId);
            String imageCostLink = ftpService.uploadFile(journeyDiaryCostType.getImageCostLink());
//            journeyDiaryCostType.setImageCostLink(imageCostLink);
//            if (journeyDiaryCostType.getCostType().getCode().equals(JourneyDiaryCostTypeEnum.BREAKDOWN_FEE.code())) {
//                JourneyDiary journeyDiary = journeyDiaryCostType.getJourneyDiary();
//                journeyDiary.setImageBreakdownLink(imageCostLink);
//                journeyDiaryService.save(journeyDiary);
//            }
//            journeyDiaryCostTypeService.save(journeyDiaryCostType);
        } catch (Exception e) {
            log.error("Error upload image journey diary cost with id {}: {}", journeyDiaryCostId, ExceptionUtils.getRootCauseMessage(e));
        }
    }

}
