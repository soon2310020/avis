package saleson.api.data.registration;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saleson.api.category.CategoryRepository;
import saleson.api.company.CompanyRepository;
import saleson.api.location.LocationRepository;
import saleson.api.machine.MachineRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.part.PartRepository;
import saleson.api.user.UserRepository;
import saleson.common.notification.MailService;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.*;
import saleson.service.mail.DataInputRequestEmailContent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataRegistrationService {
    @Autowired
    DataRegistrationRepository dataRegistrationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DataInputRequestEmailContent dataInputRequestEmailContent;
    @Autowired
    MailService mailService;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    PartRepository partRepository;
    @Autowired
    MoldRepository moldRepository;
    @Autowired
    MachineRepository machineRepository;

    @Value("${host.url}")
    String host;
    @Value("${customer.server.name.initial}")
    String serverNameInitial;

    public String generateRequestID() {
        BigDecimal index = dataRegistrationRepository.findMaxIndex().orElseGet(() -> new BigDecimal(0));
        String serverInitial = serverNameInitial.toUpperCase(Locale.ROOT);
        return serverInitial + "-DI-" + String.format("%06d", index.intValue() + 1);
    }

    @Transactional
    public DataRegistration save(DataRegistration dataRegistration) {
        BigDecimal index = dataRegistrationRepository.findMaxIndex().orElseGet(() -> new BigDecimal(0));

        dataRegistration.setRequestIndex(index.intValue() + 1);
        dataRegistration.setDueDate(DateUtils.getInstant(dataRegistration.getDueDay(), DateUtils.DEFAULT_DATE_FORMAT));
        dataRegistration.setDueDay(dataRegistration.getDueDay().substring(0, 8));

        List<Long> userIDs = dataRegistration.getAssignedUsers().stream().map(User::getId).collect(Collectors.toList());
        List<User> users = userRepository.findByIdInAndDeletedIsFalse(userIDs);
        dataRegistration.setAssignedUsers(users);

        User sender = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));

        int companyNumber = dataRegistration.getCompanyNumber() != null ? dataRegistration.getCompanyNumber() : 0;
        int locationNumber = dataRegistration.getLocationNumber() != null ? dataRegistration.getLocationNumber() : 0;
        int categoryNumber = dataRegistration.getCategoryNumber() != null ? dataRegistration.getCategoryNumber() : 0;
        int partNumber = dataRegistration.getPartNumber() != null ? dataRegistration.getPartNumber() : 0;
        int moldNumber = dataRegistration.getMoldNumber() != null ? dataRegistration.getMoldNumber() : 0;
        int machineNumber = dataRegistration.getMachineNumber() != null ? dataRegistration.getMachineNumber() : 0;

        String supportCenter = host + "/support/customer-support/";
        users.forEach(receiver -> {
            List<String> receivers = Collections.singletonList(receiver.getEmail());
            String title = sender.getName() + " from " + sender.getCompany().getName() + " has assigned a Data Input Request to you";
            String message = " has assigned a Data Input Request to you";
            sendMailDataInputRequest(receivers, title, sender, message,
                    companyNumber, locationNumber, categoryNumber, partNumber, moldNumber, machineNumber, supportCenter);
        });

        dataRegistration.setSentMailDay(DateUtils.getDay(Instant.now()));
        dataRegistration.setCompleted(false);
        dataRegistrationRepository.save(dataRegistration);
        return dataRegistration;
    }

    public void sendRemindMail(String day, boolean lastReminded) {
        Optional<List<DataRegistration>> optional = dataRegistrationRepository.findByCompletedAndSentMailDayAndLastReminded(false, day, false);
        optional.ifPresent(dataRegistrations -> dataRegistrations.forEach(dataRegistration -> {
            List<Long> userIds = dataRegistration.getAssignedUsers().stream().map(User::getId).collect(Collectors.toList());

            List<Company> companies = companyRepository.findByCreatedAtAfterAndCreatedByIn(dataRegistration.getCreatedAt(), userIds);
            double company = dataRegistration.getCompanyNumber() - (CollectionUtils.isNotEmpty(companies) ? companies.size() : 0);
            int companyNeedToAdd = (int) (company >= 0 ? company : 0);

            List<Location> locations = locationRepository.findByCreatedAtAfterAndCreatedByIn(dataRegistration.getCreatedAt(), userIds);
            double location = dataRegistration.getLocationNumber() - (CollectionUtils.isNotEmpty(locations) ? locations.size() : 0);
            int locationNeedToAdd = (int) (location >= 0 ? location : 0);

            List<Category> categories = categoryRepository.findByCreatedAtAfterAndCreatedByIn(dataRegistration.getCreatedAt(), userIds);
            double category = dataRegistration.getCategoryNumber() - (CollectionUtils.isNotEmpty(categories) ? categories.size() : 0);
            int categoryNeedToAdd = (int) (category >= 0 ? category : 0);

            List<Part> parts = partRepository.findByCreatedAtAfterAndCreatedByIn(dataRegistration.getCreatedAt(), userIds);
            double part = dataRegistration.getPartNumber() - (CollectionUtils.isNotEmpty(parts) ? parts.size() : 0);
            int partNeedToAdd = (int) (part >= 0 ? part : 0);

            List<Mold> molds = moldRepository.findByCreatedAtAfterAndCreatedByIn(dataRegistration.getCreatedAt(), userIds);
            double mold = dataRegistration.getMoldNumber() - (CollectionUtils.isNotEmpty(molds) ? molds.size() : 0);
            int moldNeedToAdd = (int) (mold >= 0 ? mold : 0);

            List<Machine> machines = machineRepository.findByCreatedAtAfterAndCreatedByIn(dataRegistration.getCreatedAt(), userIds);
            double machine = dataRegistration.getMachineNumber() - (CollectionUtils.isNotEmpty(machines) ? machines.size() : 0);
            int machineNeedToAdd = (int) (machine >= 0 ? machine : 0);

            String supportCenter = host + "/support/customer-support/";
            if (companyNeedToAdd != 0
                    || locationNeedToAdd != 0
                    || categoryNeedToAdd != 0
                    || partNeedToAdd != 0
                    || moldNeedToAdd != 0
                    || machineNeedToAdd != 0) {
                User sender = userRepository.getOne(dataRegistration.getCreatedBy());
                dataRegistration.getAssignedUsers().forEach(receiver -> {
                    List<String> receivers = Collections.singletonList(receiver.getEmail());
                    String title = sender.getName() + " from " + sender.getCompany().getName() + " is waiting for you to complete the Data Input Request";
                    String message = " is waiting for you to complete the Data Input Request";
                    sendMailDataInputRequest(receivers, title, sender, message,
                            companyNeedToAdd, locationNeedToAdd, categoryNeedToAdd, partNeedToAdd, moldNeedToAdd, machineNeedToAdd, supportCenter);
                });

                dataRegistration.setSentMailDay(DateUtils.getDay(Instant.now()));
                if (lastReminded){
                    dataRegistration.setLastReminded(true);
                }
                dataRegistrationRepository.save(dataRegistration);
            } else {
                dataRegistration.setCompleted(true);
                dataRegistrationRepository.save(dataRegistration);
            }
        }));
    }

    private void sendMailDataInputRequest(List<String> receivers, String title, User sender, String message,
                                          int companyNumber, int locationNumber, int categoryNumber,
                                          int partNumber, int moldNumber, int machineNumber, String supportCenter) {
        String dataRegistrationUrl = host + "/admin/data-registration";
        String content = dataInputRequestEmailContent.generateMailContent(new Object[]{
                sender.getName(),
                sender.getCompany().getName(),
                message,
                getCheckboxStyle(companyNumber), companyNumber,
                getCheckboxStyle(locationNumber), locationNumber,
                getCheckboxStyle(categoryNumber), categoryNumber,
                getCheckboxStyle(partNumber), partNumber,
                getCheckboxStyle(moldNumber), moldNumber,
                getCheckboxStyle(machineNumber), machineNumber,
                dataRegistrationUrl,
                supportCenter
        });
        mailService.sendMailByContent(receivers, title, content);
    }

    private boolean checkboxSelected(int value) {
        if (value == 0) return false;
        else return true;
    }

    private String getCheckboxStyle(int value) {
        if (value == 0)
            return "<div style=\"width: 15px;height: 15px;border: 1px solid #707070;border-radius: 1px;opacity: 1; margin-top: 5px; float: left;\"></div>";
        else
            return "<div style=\"width: 15px;height: 15px;border: 1px solid #0075FF;border-radius: 1px;opacity: 1;  margin-top: 5px; float: left;\">\n" +
                    " <div style=\"width: 9px;height: 9px;background: #0075FF 0% 0% no-repeat padding-box;opacity: 1; margin-left: 3px; margin-top: 3px;\"></div></div>";
    }
}
