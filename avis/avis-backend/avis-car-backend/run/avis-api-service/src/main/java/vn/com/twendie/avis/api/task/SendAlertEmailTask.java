package vn.com.twendie.avis.api.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.repository.EmailRepo;
import vn.com.twendie.avis.api.repository.UserRepo;
import vn.com.twendie.avis.api.repository.VehicleRepo;
import vn.com.twendie.avis.data.model.Email;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.data.model.Vehicle;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.DAYS;
import static org.apache.commons.lang.CharEncoding.UTF_8;
import static vn.com.twendie.avis.api.constant.AvisApiConstant.AlertTime.*;

@Service
@Slf4j
public class SendAlertEmailTask {

    private final UserRepo userRepo;
    private final VehicleRepo vehicleRepo;
    private final EmailRepo emailRepo;

    private final JavaMailSender javaMailSender;

    private final DateUtils dateUtils;
    private final SpringTemplateEngine springTemplateEngine;

    private static final String ALERT_TEMPLATE = "alert";
    private static final String DEFAULT_SUBJECT = "NHẮC NHỞ NGÀY ĐẾN HẠN TÀI VÀ XE";

    public SendAlertEmailTask(UserRepo userRepo,
                              VehicleRepo vehicleRepo,
                              EmailRepo emailRepo,
                              JavaMailSender javaMailSender,
                              DateUtils dateUtils,
                              SpringTemplateEngine springTemplateEngine) {
        this.userRepo = userRepo;
        this.vehicleRepo = vehicleRepo;
        this.emailRepo = emailRepo;
        this.javaMailSender = javaMailSender;
        this.dateUtils = dateUtils;
        this.springTemplateEngine = springTemplateEngine;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void sendEmailAlert() throws MessagingException {
        Set<String> emails = emailRepo.findByActiveTrueAndDeletedFalse()
                .stream()
                .map(Email::getAddress)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        sendEmailAlert(emails);
    }

    public void sendEmailAlert(Collection<String> emails) throws MessagingException {
        if (!CollectionUtils.isEmpty(emails)) {
            Timestamp today = dateUtils.startOfToday();

            List<User> drivers = userRepo.findByDriverLicenseExpiryDateAndDeletedFalse(
                    dateUtils.add(today, DRIVER_LICENSE_EXPIRY_DATE, DAYS));
            List<Vehicle> registrationToDateVehicles = vehicleRepo.findByRegistrationToDateAndDeletedFalse(
                    dateUtils.add(today, VEHICLE_REGISTRATION_TO_DATE, DAYS));
            List<Vehicle> travelWarrantExpiryDateVehicles = vehicleRepo.findByTravelWarrantExpiryDateAndDeletedFalse(
                    dateUtils.add(today, VEHICLE_TRAVEL_WARRANT_EXPIRY_DATE, DAYS));

            if (!(drivers.isEmpty() && travelWarrantExpiryDateVehicles.isEmpty() && registrationToDateVehicles.isEmpty())) {
                Context context = new Context();
                context.setVariable("dateUtils", dateUtils);
                context.setVariable("drivers", drivers);
                context.setVariable("registrationToDateVehicles", registrationToDateVehicles);
                context.setVariable("travelWarrantExpiryDateVehicles", travelWarrantExpiryDateVehicles);
                String html = springTemplateEngine.process(ALERT_TEMPLATE, context);
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, UTF_8);
                helper.setTo(emails.toArray(new String[0]));
                helper.setSubject(DEFAULT_SUBJECT);
                helper.setText(html, true);
                javaMailSender.send(message);
            }
        }
    }

}
