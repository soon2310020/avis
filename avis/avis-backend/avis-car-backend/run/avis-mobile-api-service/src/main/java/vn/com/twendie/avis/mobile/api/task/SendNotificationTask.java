package vn.com.twendie.avis.mobile.api.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.data.enumtype.JourneyDiaryStepEnum;
import vn.com.twendie.avis.data.enumtype.NotificationContentEnum;
import vn.com.twendie.avis.data.enumtype.NotificationSettingTypeEnum;
import vn.com.twendie.avis.data.enumtype.UserRoleEnum;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.mobile.api.service.JourneyDiarySignatureService;
import vn.com.twendie.avis.mobile.api.service.MemberCustomerService;
import vn.com.twendie.avis.mobile.api.service.UserService;
import vn.com.twendie.avis.notification.service.NotificationService;
import vn.com.twendie.avis.notification.service.NotificationSettingService;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SendNotificationTask {

    private final DateUtils dateUtils;
    private final UserService userService;
    private final JourneyDiarySignatureService journeyDiarySignatureService;
    private final MemberCustomerService memberCustomerService;
    private final NotificationService notificationService;
    private final NotificationSettingService notificationSettingService;

    public SendNotificationTask(DateUtils dateUtils,
                                UserService userService,
                                JourneyDiarySignatureService journeyDiarySignatureService,
                                MemberCustomerService memberCustomerService,
                                NotificationService notificationService,
                                NotificationSettingService notificationSettingService) {
        this.dateUtils = dateUtils;
        this.userService = userService;
        this.journeyDiarySignatureService = journeyDiarySignatureService;
        this.memberCustomerService = memberCustomerService;
        this.notificationService = notificationService;
        this.notificationSettingService = notificationSettingService;
    }


    @Scheduled(cron = "0 0 21 * * 7")
    public void pushNotificationWeek(){
        schedulePushNotification(NotificationSettingTypeEnum.WEEK);
    }

    @Scheduled(cron = "0 0 21 28-31 * ?")
    public void pushNotificationMonth(){
        final Calendar c = Calendar.getInstance();
        if(c.get(Calendar.DATE) == c.getActualMaximum(Calendar.DATE)) {
            schedulePushNotification(NotificationSettingTypeEnum.MONTH);
        }
    }

    public void schedulePushNotification(NotificationSettingTypeEnum type) {
        Timestamp start;
        Timestamp end;
        List<User> users = userService.findByRoleId(UserRoleEnum.SIGNATURE.getId());
        if(users.size() == 0) return;
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<NotificationSetting> notificationSettings = notificationSettingService.findByUserIdIn(userIds);
        List<MemberCustomer> memberCustomers = memberCustomerService
                .findByUserIdIn(userIds);
        switch (type) {
            case WEEK:
                Calendar startWeek = Calendar.getInstance();
                startWeek.setFirstDayOfWeek(Calendar.MONDAY);
                startWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

                Calendar endWeek = Calendar.getInstance();
                endWeek.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                endWeek.setFirstDayOfWeek(Calendar.SATURDAY);
                endWeek.add(Calendar.DATE, 1);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String startDateStr = dateFormat.format(startWeek.getTime());
                String endDateStr = dateFormat.format(endWeek.getTime());
                log.info("send notification to {} frm {}", startDateStr, endDateStr);
                start = dateUtils.startOfDay(new Timestamp(startWeek.getTime().getTime()));
                end = dateUtils.endOfDay(new Timestamp(endWeek.getTime().getTime()));
                for (User user : users) {
                    boolean pushNotificationWeek = false;

                    for(NotificationSetting notificationSetting : notificationSettings){
                        if(notificationSetting.getUser() != null && user.getId().equals(notificationSetting.getUser().getId())){
                            pushNotificationWeek = notificationSetting.isWeek();
                            break;
                        }
                    }

                    for (MemberCustomer memberCustomer : memberCustomers) {
                        if (memberCustomer.getUser() != null && user.getId().equals(memberCustomer.getUser().getId())) {
                            List<JourneyDiarySignature> journeyDiarySignatures = journeyDiarySignatureService
                                    .findByMemberCustomerAndTimeEndBetween(memberCustomer, start, end);
                            long total = journeyDiarySignatures.stream().
                                    filter(j -> j.getJourneyDiary() != null
                                            && JourneyDiaryStepEnum.FINISHED.value().equals(j.getJourneyDiary().getStep()))
                                    .count();
                            long unsigned = journeyDiarySignatures.stream().filter(j -> !j.getStatus()).count();
                            notificationService.pushNotificationSignature(user,
                                    NotificationContentEnum.END_JOURNEY_DIARY_SIGNATURE_WEEK, NotificationSettingTypeEnum.WEEK,
                                    null, start, end, pushNotificationWeek, String.valueOf(total), startDateStr, endDateStr, String.valueOf(unsigned));
                            break;
                        }
                    }
                }
                break;
            case MONTH:
                Calendar startMonth = Calendar.getInstance();
                startMonth.set(Calendar.DAY_OF_MONTH, 1);

                Calendar endMonth = Calendar.getInstance();
                endMonth.set(Calendar.DAY_OF_MONTH, startMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

                String monthStr = new SimpleDateFormat("MM/yyyy").format(startMonth.getTime());
                log.info("send notification month {}", monthStr);

                start = dateUtils.startOfDay(new Timestamp(startMonth.getTime().getTime()));
                end = dateUtils.endOfDay(new Timestamp(endMonth.getTime().getTime()));

                for (User user : users) {
                    boolean pushNotificationWeek = false;
                    for(NotificationSetting notificationSetting : notificationSettings){
                        if(notificationSetting.getUser() != null && user.getId().equals(notificationSetting.getUser().getId())){
                            pushNotificationWeek = notificationSetting.isMonth();
                            break;
                        }
                    }

                    for (MemberCustomer memberCustomer : memberCustomers) {
                        if (memberCustomer.getUser() != null && user.getId().equals(memberCustomer.getUser().getId())) {
                            List<JourneyDiarySignature> journeyDiarySignatures = journeyDiarySignatureService
                                    .findByMemberCustomerAndTimeEndBetween(memberCustomer, start, end);
                            long total = journeyDiarySignatures.stream().
                                    filter(j -> j.getJourneyDiary() != null
                                            && JourneyDiaryStepEnum.FINISHED.value().equals(j.getJourneyDiary().getStep()))
                                    .count();
                            long unsigned = journeyDiarySignatures.stream().filter(j -> !j.getStatus() && j.getJourneyDiary() != null
                                    && JourneyDiaryStepEnum.FINISHED.value().equals(j.getJourneyDiary().getStep())).count();
                            notificationService.pushNotificationSignature(user,
                                    NotificationContentEnum.END_JOURNEY_DIARY_SIGNATURE_MONTH,
                                    NotificationSettingTypeEnum.MONTH, null, start, end, pushNotificationWeek,
                                    String.valueOf(total), monthStr, String.valueOf(unsigned));
                            break;
                        }
                    }
                }
                break;
        }
    }
}
