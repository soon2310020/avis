package saleson.common.notification;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.emoldino.api.common.resource.base.noti.service.email.NotiEmailService;
import com.emoldino.framework.util.BeanUtils;

import lombok.extern.slf4j.Slf4j;
import saleson.api.broadcastNotification.BroadcastNotificationService;
import saleson.api.mold.MoldCorrectiveRepository;
import saleson.api.mold.MoldCycleTimeRepository;
import saleson.api.mold.MoldDisconnectRepository;
import saleson.api.mold.MoldDowntimeEventRepository;
import saleson.api.mold.MoldEfficiencyRepository;
import saleson.api.mold.MoldLocationRepository;
import saleson.api.mold.MoldMaintenanceRepository;
import saleson.api.mold.MoldMisconfigureRepository;
import saleson.api.mold.MoldService;
import saleson.api.notification.NotificationService;
import saleson.api.terminal.TerminalDisconnectRepository;
import saleson.api.user.UserAlertRepository;
import saleson.api.user.UserInviteRepository;
import saleson.api.user.UserRepository;
import saleson.common.batch.BatchMessageRepository;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.BatchStatus;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.CorrectiveStatus;
import saleson.common.enumeration.DowntimeStatus;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.common.enumeration.MessageType;
import saleson.common.enumeration.MisconfigureStatus;
import saleson.common.enumeration.MoldLocationStatus;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.PeriodType;
import saleson.model.*;
import saleson.service.mail.AlertEmailService;
import saleson.service.mail.ReminderInviteUserEmailContent;
import saleson.service.transfer.LogUserAlertRepository;
import saleson.service.util.DateTimeUtils;

@Slf4j
@Service("mailService")
public class MailService {

	@Autowired
	private BatchMessageRepository batchMessageRepository;

	@Autowired
	private LogUserAlertRepository logUserAlertRepository;

	@Autowired
	private UserAlertRepository userAlertRepository;

	@Autowired
	private MoldLocationRepository moldLocationRepository;

	@Autowired
	private MoldDisconnectRepository moldDisconnectRepository;

	@Autowired
	private MoldMaintenanceRepository moldMaintenanceRepository;

	@Autowired
	private MoldCorrectiveRepository moldCorrectiveRepository;

	@Autowired
	private MoldEfficiencyRepository moldEfficiencyRepository;

	@Autowired
	private MoldCycleTimeRepository moldCycleTimeRepository;

	@Autowired
	private MoldMisconfigureRepository moldMisconfigureRepository;

	@Autowired
	private MoldDowntimeEventRepository moldDowntimeEventRepository;

	@Autowired
	private TerminalDisconnectRepository terminalDisconnectRepository;

	@Autowired
	private UserInviteRepository userInviteRepository;

	@Lazy
	@Autowired
	private MoldService moldService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ReminderInviteUserEmailContent reminderInviteUserEmailContent;

	@Value("${host.url}")
	private String host;

	@Value("${customer.server.name}")
	private String serverName;

	@Autowired
	private AlertEmailService alertEmailService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private BroadcastNotificationService broadcastNotificationService;

	public MailMessage testHtml(){
//		String yesterday = DateUtils.getYesterday("MMM dd, yyyy");
//		return yesterday + " Week " + DateUtils.getWeekYear(yesterday, "MMM dd, yyyy");
		MoldLocation moldLocation = moldLocationRepository.findById(144785L).orElse(null);
		MoldDisconnect moldDisconnect = moldDisconnectRepository.findById(145915L).orElse(null);
		TerminalDisconnect terminalDisconnect = terminalDisconnectRepository.findById(146204L).orElse(null);
		MoldCycleTime moldCycleTime = moldCycleTimeRepository.findById(111762L).orElse(null);
		MoldMaintenance moldMaintenance = moldMaintenanceRepository.findById(7328L).orElse(null);
		MoldEfficiency moldEfficiency = moldEfficiencyRepository.findById(111755L).orElse(null);
		MoldMisconfigure moldMisconfigure = moldMisconfigureRepository.findById(56793L).orElse(null);
		if(moldLocation != null){
			List<MoldLocation> moldLocationList = Arrays.asList(moldLocation);
			MailMessage mailMessage = SystemMailMessage.builder().alertEmailService(alertEmailService)
					.host(host)
					.receivers(Arrays.asList("duongvt@itleadpro.vn"))
					.periodType(PeriodType.MONTHLY)
					.moldLocations(moldLocationList)
					.moldDisconnects(Arrays.asList(moldDisconnect))
					.terminalDisconnects(Arrays.asList(terminalDisconnect))
					.moldCycleTimes(Arrays.asList(moldCycleTime))
					.moldMaintenances(Arrays.asList(moldMaintenance))
					.moldEfficiencies(Arrays.asList(moldEfficiency))
					.moldMisconfigures(Arrays.asList(moldMisconfigure))
					.build();
			return mailMessage;
		}
		MailMessage mailMessage = SystemMailMessage.builder().alertEmailService(alertEmailService).build();
		return mailMessage;
	}

	public void testMail(){
		MoldLocation moldLocation = moldLocationRepository.findById(144785L).orElse(null);
		Message message = SystemMailMessage.builder().alertEmailService(alertEmailService)
				.host(host)
				.receivers(Arrays.asList("duongvt@itleadpro.vn"))
				.periodType(PeriodType.MONTHLY)
				.moldLocations(Arrays.asList(moldLocation))
				.build();
		send(message);
	}

	public List<UserAlert> testFunction(Long userId){
		return userAlertRepository.findAllByAlertType(AlertType.DISCONNECTED);
	}

	public void send(Message message) {
		BatchMessage batchMessage = BatchMessage.builder()
				.messageType(MessageType.EMAIL)
				.batchStatus(BatchStatus.PENDING)
				.title(message.getTitle())
				.content(message.getContent())
				.receivers(String.join(",", message.getReceivers()))
				.sender(message.getFrom())
				.build();


		batchMessageRepository.save(batchMessage);
	}


	public void sendAlertMail(AlertType alertType, String toolingID) {
		Message message = AlertMailMessage.builder()
				.receivers(Arrays.asList("skc@onlinepowers.com", "tlsrmrckd@naver.com"))
				.alertType(AlertType.MISCONFIGURE)
				.toolingId("MOLD_34234")
				.build();

		//mailService.send(message);
	}

	@Transactional
	public void sendSystemAlert_old(){
		List<UserAlert> userAlertDailyList = userAlertRepository.findByEmailAndPeriodType(true, PeriodType.DAILY);
		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		List<MoldLocation> moldLocationDailyList = moldLocationRepository.findAllByCreatedAtBetweenAndMoldLocationStatusAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), MoldLocationStatus.PENDING, true, sort);
		List<MoldDisconnect> moldDisconnectDailyList = moldDisconnectRepository.findAllByCreatedAtBetweenAndNotificationStatusAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), NotificationStatus.ALERT, true, sort);
		List<TerminalDisconnect> terminalDisconnectDailyList = terminalDisconnectRepository.findAllByCreatedAtBetweenAndNotificationStatusAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), NotificationStatus.ALERT, true, sort);
		List<MoldCycleTime> moldCycleTimeDailyList = moldCycleTimeRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(), DateTimeUtils.getStartOfTomorrow(), true, PeriodType.DAILY, sort);
		List<MoldMaintenance> moldMaintenanceDailyList = moldMaintenanceRepository.findAllByCreatedAtBetweenAndMaintenanceStatusIsInAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), Arrays.asList(MaintenanceStatus.OVERDUE, MaintenanceStatus.UPCOMING), true, sort);
		List<MoldMisconfigure> moldMisconfigureDailyList = moldMisconfigureRepository.findAllByCreatedAtBetweenAndMisconfigureStatusAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), MisconfigureStatus.MISCONFIGURED, true, sort);
		List<MoldEfficiency> moldEfficiencyDailyList = moldEfficiencyRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(), DateTimeUtils.getStartOfTomorrow(), true, PeriodType.DAILY, sort);
		List<MoldDowntimeEvent> moldDowntimeDailyList = moldDowntimeEventRepository.findAllByCreatedAtBetweenAndDowntimeStatusIsInAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), Arrays.asList(DowntimeStatus.DOWNTIME), true, sort);

		Map<User, List<AlertType>> userAlertListMap = getUserAlertListMap(userAlertDailyList);
		sendMailForEachUser(userAlertListMap, PeriodType.DAILY, moldLocationDailyList, moldDisconnectDailyList, terminalDisconnectDailyList, moldCycleTimeDailyList,
				moldMaintenanceDailyList, moldMisconfigureDailyList, moldEfficiencyDailyList, null, null, moldDowntimeDailyList);

		if(DateTimeUtils.todayIsMonday()){
			List<UserAlert> userAlertWeeklyList = userAlertRepository.findByEmailAndPeriodType(true, PeriodType.WEEKLY);
			List<MoldLocation> moldLocationWeeklyList = moldLocationRepository.findAllByCreatedAtBetweenAndMoldLocationStatusAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), MoldLocationStatus.PENDING, true, sort);
			List<MoldDisconnect> moldDisconnectWeeklyList = moldDisconnectRepository.findAllByCreatedAtBetweenAndNotificationStatusAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), NotificationStatus.ALERT, true, sort);
			List<TerminalDisconnect> terminalDisconnectWeeklyList = terminalDisconnectRepository.findAllByCreatedAtBetweenAndNotificationStatusAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), NotificationStatus.ALERT, true, sort);
			List<MoldCycleTime> moldCycleTimeWeeklyList = moldCycleTimeRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(), DateTimeUtils.getStartOfTomorrow(), true, PeriodType.WEEKLY, sort);
			List<MoldMaintenance> moldMaintenanceWeeklyList = moldMaintenanceRepository.findAllByCreatedAtBetweenAndMaintenanceStatusIsInAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), Arrays.asList(MaintenanceStatus.OVERDUE, MaintenanceStatus.UPCOMING), true, sort);
			List<MoldMisconfigure> moldMisconfigureWeeklyList = moldMisconfigureRepository.findAllByCreatedAtBetweenAndMisconfigureStatusAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), MisconfigureStatus.MISCONFIGURED, true, sort);
			List<MoldEfficiency> moldEfficiencyWeeklyList = moldEfficiencyRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(), DateTimeUtils.getStartOfTomorrow(), true, PeriodType.WEEKLY, sort);
			List<MoldDowntimeEvent> moldDowntimeWeeklyList = moldDowntimeEventRepository.findAllByCreatedAtBetweenAndDowntimeStatusIsInAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), Arrays.asList(DowntimeStatus.DOWNTIME), true, sort);
			Map<User, List<AlertType>> userAlertWeeklyListMap = getUserAlertListMap(userAlertWeeklyList);
			sendMailForEachUser(userAlertWeeklyListMap, PeriodType.WEEKLY, moldLocationWeeklyList, moldDisconnectWeeklyList, terminalDisconnectWeeklyList, moldCycleTimeWeeklyList,
					moldMaintenanceWeeklyList, moldMisconfigureWeeklyList, moldEfficiencyWeeklyList, null, null, moldDowntimeWeeklyList);
		}

		if(DateTimeUtils.todayIsFirstDayOfMonth()){
			List<UserAlert> userAlertMonthlyList = userAlertRepository.findByEmailAndPeriodType(true, PeriodType.MONTHLY);
			List<MoldLocation> moldLocationMonthlyList = moldLocationRepository.findAllByCreatedAtBetweenAndMoldLocationStatusAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), MoldLocationStatus.PENDING, true, sort);
			List<MoldDisconnect> moldDisconnectMonthlyList = moldDisconnectRepository.findAllByCreatedAtBetweenAndNotificationStatusAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), NotificationStatus.ALERT, true, sort);
			List<TerminalDisconnect> terminalDisconnectMonthlyList = terminalDisconnectRepository.findAllByCreatedAtBetweenAndNotificationStatusAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), NotificationStatus.ALERT, true, sort);
			List<MoldCycleTime> moldCycleTimeMonthlyList = moldCycleTimeRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(), DateTimeUtils.getStartOfTomorrow(), true, PeriodType.MONTHLY, sort);
			List<MoldMaintenance> moldMaintenanceMonthlyList = moldMaintenanceRepository.findAllByCreatedAtBetweenAndMaintenanceStatusIsInAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), Arrays.asList(MaintenanceStatus.OVERDUE, MaintenanceStatus.UPCOMING), true, sort);
			List<MoldMisconfigure> moldMisconfigureMonthlyList = moldMisconfigureRepository.findAllByCreatedAtBetweenAndMisconfigureStatusAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), MisconfigureStatus.MISCONFIGURED, true, sort);
			List<MoldEfficiency> moldEfficiencyMonthlyList = moldEfficiencyRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(), DateTimeUtils.getStartOfTomorrow(), true, PeriodType.MONTHLY, sort);
			List<MoldDowntimeEvent> moldDowntimeMonthlyList = moldDowntimeEventRepository.findAllByCreatedAtBetweenAndDowntimeStatusIsInAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), Arrays.asList(DowntimeStatus.DOWNTIME), true, sort);
			Map<User, List<AlertType>> userAlertMonthlyListMap = getUserAlertListMap(userAlertMonthlyList);
			sendMailForEachUser(userAlertMonthlyListMap, PeriodType.MONTHLY, moldLocationMonthlyList, moldDisconnectMonthlyList, terminalDisconnectMonthlyList, moldCycleTimeMonthlyList,
					moldMaintenanceMonthlyList, moldMisconfigureMonthlyList, moldEfficiencyMonthlyList, null, null, moldDowntimeMonthlyList);
		}
	}

	@Transactional
	public void sendSystemAlert(){
		List<UserAlert> userAlertList = userAlertRepository.findByEmailAndPeriodType(true, PeriodType.DAILY);
		PeriodType periodType = PeriodType.DAILY;
		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		List<MoldLocation> moldLocations = moldLocationRepository.findAllByCreatedAtBetweenAndMoldLocationStatusAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), MoldLocationStatus.PENDING, true, sort);
		List<MoldDisconnect> moldDisconnects = moldDisconnectRepository.findAllByCreatedAtBetweenAndNotificationStatusAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), NotificationStatus.ALERT, true, sort);
		List<TerminalDisconnect> terminalDisconnects = terminalDisconnectRepository.findAllByCreatedAtBetweenAndNotificationStatusAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), NotificationStatus.ALERT, true, sort);
		List<MoldCycleTime> moldCycleTimes = moldCycleTimeRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(), DateTimeUtils.getStartOfTomorrow(), true, PeriodType.DAILY, sort);
//		List<MoldMaintenance> moldMaintenances = moldMaintenanceRepository.findAllByCreatedAtBetweenAndMaintenanceStatusIsInAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), Arrays.asList(MaintenanceStatus.OVERDUE, MaintenanceStatus.UPCOMING), true, sort);
//		List<MoldMisconfigure> moldMisconfigures = moldMisconfigureRepository.findAllByCreatedAtBetweenAndMisconfigureStatusAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), MisconfigureStatus.MISCONFIGURED, true, sort);
		List<MoldEfficiency> moldEfficiencies = moldEfficiencyRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(), DateTimeUtils.getStartOfTomorrow(), true, PeriodType.DAILY, sort);
//		List<MoldDowntimeEvent> moldDowntimeEvents = moldDowntimeEventRepository.findAllByCreatedAtBetweenAndDowntimeStatusIsInAndLatest(DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), Arrays.asList(DowntimeStatus.DOWNTIME), true, sort);

		if(DateTimeUtils.todayIsMonday()){
			userAlertList = userAlertRepository.findByEmailAndPeriodType(true, PeriodType.WEEKLY);
			periodType = PeriodType.WEEKLY;
			moldLocations = moldLocationRepository.findAllByCreatedAtBetweenAndMoldLocationStatusAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), MoldLocationStatus.PENDING, true, sort);
			moldDisconnects = moldDisconnectRepository.findAllByCreatedAtBetweenAndNotificationStatusAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), NotificationStatus.ALERT, true, sort);
			terminalDisconnects = terminalDisconnectRepository.findAllByCreatedAtBetweenAndNotificationStatusAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), NotificationStatus.ALERT, true, sort);
			moldCycleTimes = moldCycleTimeRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(), DateTimeUtils.getStartOfTomorrow(), true, PeriodType.WEEKLY, sort);
//			moldMaintenances = moldMaintenanceRepository.findAllByCreatedAtBetweenAndMaintenanceStatusIsInAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), Arrays.asList(MaintenanceStatus.OVERDUE, MaintenanceStatus.UPCOMING), true, sort);
//			moldMisconfigures = moldMisconfigureRepository.findAllByCreatedAtBetweenAndMisconfigureStatusAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), MisconfigureStatus.MISCONFIGURED, true, sort);
			moldEfficiencies = moldEfficiencyRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(), DateTimeUtils.getStartOfTomorrow(), true, PeriodType.WEEKLY, sort);
//			moldDowntimeEvents = moldDowntimeEventRepository.findAllByCreatedAtBetweenAndDowntimeStatusIsInAndLatest(DateTimeUtils.getStartOfTheWeek(), DateTimeUtils.getStartOfToday(), Arrays.asList(DowntimeStatus.DOWNTIME), true, sort);
		}

		if(DateTimeUtils.todayIsFirstDayOfMonth()){
			userAlertList = userAlertRepository.findByEmailAndPeriodType(true, PeriodType.MONTHLY);
			periodType = PeriodType.MONTHLY;
			moldLocations = moldLocationRepository.findAllByCreatedAtBetweenAndMoldLocationStatusAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), MoldLocationStatus.PENDING, true, sort);
			moldDisconnects = moldDisconnectRepository.findAllByCreatedAtBetweenAndNotificationStatusAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), NotificationStatus.ALERT, true, sort);
			terminalDisconnects = terminalDisconnectRepository.findAllByCreatedAtBetweenAndNotificationStatusAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), NotificationStatus.ALERT, true, sort);
			moldCycleTimes = moldCycleTimeRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(), DateTimeUtils.getStartOfTomorrow(), true, PeriodType.MONTHLY, sort);
//			moldMaintenances = moldMaintenanceRepository.findAllByCreatedAtBetweenAndMaintenanceStatusIsInAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), Arrays.asList(MaintenanceStatus.OVERDUE, MaintenanceStatus.UPCOMING), true, sort);
//			moldMisconfigures = moldMisconfigureRepository.findAllByCreatedAtBetweenAndMisconfigureStatusAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), MisconfigureStatus.MISCONFIGURED, true, sort);
			moldEfficiencies = moldEfficiencyRepository.findAllByCreatedAtBetweenAndLatestAndPeriodType(DateTimeUtils.getStartOfToday(), DateTimeUtils.getStartOfTomorrow(), true, PeriodType.MONTHLY, sort);
//			moldDowntimeEvents = moldDowntimeEventRepository.findAllByCreatedAtBetweenAndDowntimeStatusIsInAndLatest(DateTimeUtils.getStartOfTheMonth(), DateTimeUtils.getStartOfToday(), Arrays.asList(DowntimeStatus.DOWNTIME), true, sort);
		}

		Map<User, List<AlertType>> userAlertListMap = getUserAlertListMap(userAlertList);

		//send notification
		sendMailForEachUser_New(userAlertListMap, periodType, moldLocations, moldDisconnects, terminalDisconnects, moldCycleTimes,
				null, null, moldEfficiencies, null, null, null);
	}

	@Transactional
	public void sendReminder(){
		List<MoldCorrective> upcomingMoldCorrectives = moldCorrectiveRepository.findByCorrectiveStatusNotLikeAndExpectedEndTimeBetweenAndLatest(CorrectiveStatus.COMPLETED,
				DateTimeUtils.getStartOfYesterday(), DateTimeUtils.getStartOfToday(), true);
		if(upcomingMoldCorrectives != null && upcomingMoldCorrectives.size() > 0){
			sendCorrectiveReminder(upcomingMoldCorrectives, true, null);
		}

		if(serverName.equalsIgnoreCase("dyson")) {
			List<MoldCorrective> overdueMoldCorrectives = moldCorrectiveRepository.findByCorrectiveStatusNotLikeAndExpectedEndTimeBetweenAndLatest(CorrectiveStatus.COMPLETED,
					DateTimeUtils.getStartOfPreviousDay(4), DateTimeUtils.getStartOfPreviousDay(3), true);
			if (overdueMoldCorrectives != null && overdueMoldCorrectives.size() > 0) {
				sendCorrectiveReminder(overdueMoldCorrectives, null, true);
			}
		}
	}

	public void sendReminderInviteUser(){
		List<UserInvite> userInviteList = userInviteRepository.findByCreatedAtBetweenAndEnabled(
				DateTimeUtils.getStartOfToday().minus(8, ChronoUnit.DAYS),
				DateTimeUtils.getStartOfToday().minus(7, ChronoUnit.DAYS),true);
		try {
			String supportCenter =  host+"/support/customer-support/";

			userInviteList.forEach(ui -> {
				List<String> receivers = Arrays.asList(ui.getEmail());
				String title = ui.getSender().getName() + " from " + ui.getSender().getCompany().getName() + " is waiting for you to join eMoldino platform";
				String setUpAccount = host + "/create-account/" + ui.getHashCode();
				String content = reminderInviteUserEmailContent.generateMailContent(new Object[]{
						ui.getSender().getName(),
						ui.getSender().getEmail(),
						ui.getSender().getCompany().getName(),
						setUpAccount,
						supportCenter
				});
				sendMailByContent(receivers, title, content);

			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendCorrectiveReminder(List<MoldCorrective> moldCorrectives, Boolean reminderUpcoming, Boolean reminderOverdue){
		if(moldCorrectives == null || moldCorrectives.size() == 0) return;

		List<Long> alertIds = moldCorrectives.stream().map(x -> x.getId()).collect(Collectors.toList());
		List<LogUserAlert> logUserAlerts = logUserAlertRepository.findByAlertTypeAndAlertIdIn(AlertType.CORRECTIVE_MAINTENANCE, alertIds);
		List<Long> userIds = logUserAlerts.stream().map(x -> x.getUserId()).distinct().collect(Collectors.toList());
		List<User> users = userRepository.findByIdInAndDeletedIsFalse(userIds);
		List<UserAlert> userAlerts = userAlertRepository.findByUserInAndAlertTypeInAndEmailIsTrue(users, Arrays.asList(AlertType.CORRECTIVE_MAINTENANCE));

		if(reminderUpcoming != null && reminderUpcoming == true) {
			userAlerts.forEach(userAlert -> {
				if(userAlert.getUser().getCompany().getCompanyType().equals(CompanyType.SUPPLIER)) {
					Set<MoldCorrective> moldCorrectiveList = new HashSet<>();
					logUserAlerts.forEach(logUserAlert -> {
						if (logUserAlert.getUserId().equals(userAlert.getUser().getId())) {
							List<MoldCorrective> subCorrectiveList = moldCorrectives.stream()
									.filter(x -> x.getId().equals(logUserAlert.getAlertId()))
									.collect(Collectors.toList());
							moldCorrectiveList.addAll(subCorrectiveList);
						}
					});
					Message message = SystemMailMessage.builder().alertEmailService(alertEmailService)
							.host(host)
							.receivers(Arrays.asList(userAlert.getUser().getEmail()))
							.periodType(PeriodType.REAL_TIME)
							.moldCorrectives(new ArrayList<>(moldCorrectiveList))
							.reminderUpcoming(reminderUpcoming)
							.build();
					send(message);
				}
			});
		}else if(reminderOverdue != null && reminderOverdue == true){
			userAlerts.forEach(userAlert -> {
				if(userAlert.getUser().getCompany().getCompanyType().equals(CompanyType.IN_HOUSE)) {
					Set<MoldCorrective> moldCorrectiveList = new HashSet<>();
					logUserAlerts.forEach(logUserAlert -> {
						if (logUserAlert.getUserId().equals(userAlert.getUser().getId())) {
							List<MoldCorrective> subCorrectiveList = moldCorrectives.stream()
									.filter(x -> x.getId().equals(logUserAlert.getAlertId()) && x.getMold().getEngineersInCharge().contains(userAlert.getUser()))
									.collect(Collectors.toList());
							moldCorrectiveList.addAll(subCorrectiveList);
						}
					});
					Message message = SystemMailMessage.builder().alertEmailService(alertEmailService)
							.host(host)
							.receivers(Arrays.asList(userAlert.getUser().getEmail()))
							.periodType(PeriodType.REAL_TIME)
							.moldCorrectives(new ArrayList<>(moldCorrectiveList))
							.reminderOverdue(reminderOverdue)
							.build();
					send(message);
				}
			});
		}
	}

	public Map<User, List<AlertType>> getUserAlertListMap(List<UserAlert> userAlertList){
		Map<User, List<AlertType>> userAlertListMap = new HashMap<>();
		userAlertList.forEach(userAlert -> {
			if (!userAlertListMap.containsKey(userAlert.getUser())) {
				List<AlertType> alertTypeList = new ArrayList<>();
				alertTypeList.add(userAlert.getAlertType());
				userAlertListMap.put(userAlert.getUser(), alertTypeList);
			} else {
				List<AlertType> alertTypeList = userAlertListMap.get(userAlert.getUser());
				alertTypeList.add(userAlert.getAlertType());
				userAlertListMap.put(userAlert.getUser(), alertTypeList);
			}
		});
		return userAlertListMap;
	}

	public Map<User, Map<AlertType,PeriodType>> getUserAlertListMapEntry(List<UserAlert> userAlertList){
		Map<User, Map<AlertType,PeriodType>> userAlertListMap = new HashMap<>();
		userAlertList.forEach(userAlert -> {
			if (!userAlertListMap.containsKey(userAlert.getUser())) {
				Map<AlertType,PeriodType> alertTypeMap = new HashMap<>();
				alertTypeMap.put(userAlert.getAlertType(), userAlert.getPeriodType());
				userAlertListMap.put(userAlert.getUser(), alertTypeMap);
			} else {
				Map<AlertType,PeriodType> alertTypeMap = userAlertListMap.get(userAlert.getUser());
				alertTypeMap.put(userAlert.getAlertType(), userAlert.getPeriodType());
				userAlertListMap.put(userAlert.getUser(), alertTypeMap);
			}
		});
		return userAlertListMap;
	}

	public void sendMailForEachUser(Map<User, List<AlertType>> userAlertListMap, PeriodType periodType, List<MoldLocation> moldLocations, List<MoldDisconnect> moldDisconnects,
									List<TerminalDisconnect> terminalDisconnects, List<MoldCycleTime> moldCycleTimes, List<MoldMaintenance> moldMaintenances,
									List<MoldMisconfigure> moldMisconfigures, List<MoldEfficiency> moldEfficiencies, List<MoldDataSubmission> moldDataSubmissions,
									List<MoldCorrective> moldCorrectives, List<MoldDowntimeEvent> moldDowntimeEvents){
		userAlertListMap.forEach((user, alertTypeList) -> {
			List<MoldLocation> moldLocationList = new ArrayList<>();
			List<MoldDisconnect> moldDisconnectList = new ArrayList<>();
			List<TerminalDisconnect> terminalDisconnectList = new ArrayList<>();
			List<MoldCycleTime> moldCycleTimeList = new ArrayList<>();
			List<MoldMaintenance> moldMaintenanceList = new ArrayList<>();
			List<MoldMisconfigure> moldMisconfigureList = new ArrayList<>();
			List<MoldEfficiency> moldEfficiencyList = new ArrayList<>();
			List<MoldDataSubmission> moldDataSubmissionList = new ArrayList<>();
			List<MoldCorrective> moldCorrectiveList = new ArrayList<>();
			List<MoldDowntimeEvent> moldDowntimeEventList = new ArrayList<>();

			List<LogUserAlert> notSentAlerts = logUserAlertRepository.findByUserIdAndEmail(user.getId(), false);
			List<LogUserAlert> alertsToSend = new ArrayList<>();

			if(notSentAlerts != null && notSentAlerts.size() > 0) {
				if (alertTypeList.contains(AlertType.RELOCATION) && moldLocations != null) {
					List<LogUserAlert> notSentRelocationAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.RELOCATION)).collect(Collectors.toList());
					if(notSentRelocationAlert != null && notSentRelocationAlert.size() > 0) {
						List<Long> notSentRelocationAlertIds = notSentRelocationAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldLocationList = moldLocations.stream().filter(x -> notSentRelocationAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentRelocationAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentRelocationAlert);
					}
				}
				if (alertTypeList.contains(AlertType.DISCONNECTED)) {
					if (moldDisconnects != null) {
						List<LogUserAlert> notSentMoldDisconnectionAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.DISCONNECTED)).collect(Collectors.toList());
						if (notSentMoldDisconnectionAlert != null && notSentMoldDisconnectionAlert.size() > 0) {
							List<Long> notSentMoldDisconnectionAlertIds = notSentMoldDisconnectionAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
							moldDisconnectList = moldDisconnects.stream().filter(x -> notSentMoldDisconnectionAlertIds.contains(x.getId())).collect(Collectors.toList());
							notSentMoldDisconnectionAlert.forEach(x -> x.setEmail(true));
							alertsToSend.addAll(notSentMoldDisconnectionAlert);
						}
					}
				}
				if (alertTypeList.contains(AlertType.TERMINAL_DISCONNECTED) || alertTypeList.contains(AlertType.DISCONNECTED)) {
					if (terminalDisconnects != null) {
						List<LogUserAlert> notSentTerminalDisconnectionAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.TERMINAL_DISCONNECTED)).collect(Collectors.toList());
						if(notSentTerminalDisconnectionAlert != null && notSentTerminalDisconnectionAlert.size() > 0){
							List<Long> notSentTerminalDisconnectionAlertIds = notSentTerminalDisconnectionAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
							terminalDisconnectList = terminalDisconnects.stream().filter(x -> notSentTerminalDisconnectionAlertIds.contains(x.getId())).collect(Collectors.toList());
							notSentTerminalDisconnectionAlert.forEach(x -> x.setEmail(true));
							alertsToSend.addAll(notSentTerminalDisconnectionAlert);
						}
					}
				}
				if (alertTypeList.contains(AlertType.CYCLE_TIME) && moldCycleTimes != null) {
					List<LogUserAlert> notSentCycleTimeAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.CYCLE_TIME)).collect(Collectors.toList());
					if(notSentCycleTimeAlert != null && notSentCycleTimeAlert.size() > 0){
						List<Long> notSentCycleTimeAlertIds = notSentCycleTimeAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldCycleTimeList = moldCycleTimes.stream().filter(x -> notSentCycleTimeAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentCycleTimeAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentCycleTimeAlert);
					}
				}
				if (alertTypeList.contains(AlertType.MAINTENANCE) && moldMaintenances != null) {
					List<LogUserAlert> notSentMaintenanceAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.MAINTENANCE)).collect(Collectors.toList());
					if(notSentMaintenanceAlert != null && notSentMaintenanceAlert.size() > 0){
						List<Long> notSentMaintenanceAlertIds = notSentMaintenanceAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldMaintenanceList = moldMaintenances.stream().filter(x -> notSentMaintenanceAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentMaintenanceAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentMaintenanceAlert);
					}
				}

				if (alertTypeList.contains(AlertType.CORRECTIVE_MAINTENANCE) && moldCorrectives != null) {
					List<LogUserAlert> notSentCorrectiveAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.CORRECTIVE_MAINTENANCE)).collect(Collectors.toList());
					if(notSentCorrectiveAlert != null && notSentCorrectiveAlert.size() > 0){
						List<Long> notSentCorrectiveAlertIds = notSentCorrectiveAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldCorrectiveList = moldCorrectives.stream().filter(x -> notSentCorrectiveAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentCorrectiveAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentCorrectiveAlert);
					}
				}

				if (alertTypeList.contains(AlertType.MISCONFIGURE) && moldMisconfigures != null) {
					List<LogUserAlert> notSentMisconfigureAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.MISCONFIGURE)).collect(Collectors.toList());
					if(notSentMisconfigureAlert != null && notSentMisconfigureAlert.size() > 0){
						List<Long> notSentMisconfigureAlertIds = notSentMisconfigureAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldMisconfigureList = moldMisconfigures.stream().filter(x -> notSentMisconfigureAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentMisconfigureAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentMisconfigureAlert);
					}
				}
				if (alertTypeList.contains(AlertType.EFFICIENCY) && moldEfficiencies != null) {
					List<LogUserAlert> notSentEfficiencyAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.EFFICIENCY)).collect(Collectors.toList());
					if(notSentEfficiencyAlert != null && notSentEfficiencyAlert.size() > 0){
						List<Long> notSentEfficiencyAlertIds = notSentEfficiencyAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldEfficiencyList = moldEfficiencies.stream().filter(x -> notSentEfficiencyAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentEfficiencyAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentEfficiencyAlert);
					}
				}
				if(alertTypeList.contains(AlertType.DATA_SUBMISSION) && moldDataSubmissions != null){
					List<LogUserAlert> notSentDataSubmissionAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.DATA_SUBMISSION)).collect(Collectors.toList());
					if(notSentDataSubmissionAlert != null && notSentDataSubmissionAlert.size() > 0){
						List<Long> notSentDataSubmissionAlertIds = notSentDataSubmissionAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldDataSubmissionList = moldDataSubmissions.stream().filter(x -> notSentDataSubmissionAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentDataSubmissionAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentDataSubmissionAlert);
					}
				}
				if(alertTypeList.contains(AlertType.DOWNTIME) && moldDowntimeEvents != null){
					List<LogUserAlert> notSentDowntimeAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.DOWNTIME)).collect(Collectors.toList());
					if(notSentDowntimeAlert != null && notSentDowntimeAlert.size() > 0){
						List<Long> notSentDowntimeAlertIds = notSentDowntimeAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldDowntimeEventList = moldDowntimeEvents.stream().filter(x -> notSentDowntimeAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentDowntimeAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentDowntimeAlert);
					}
				}

				if ((moldLocationList != null && !moldLocationList.isEmpty()) || (moldDisconnectList != null && !moldDisconnectList.isEmpty()) || (terminalDisconnectList != null && !terminalDisconnectList.isEmpty())
						|| (moldCycleTimeList != null && !moldCycleTimeList.isEmpty()) || (moldMaintenanceList != null && !moldMaintenanceList.isEmpty())
						|| (moldCorrectiveList != null && !moldCorrectiveList.isEmpty()) || (moldMisconfigureList != null && !moldMisconfigureList.isEmpty())
						|| (moldEfficiencyList != null && !moldEfficiencyList.isEmpty()) || (moldDataSubmissionList != null && !moldDataSubmissionList.isEmpty())
						|| (moldDowntimeEventList != null && !moldDowntimeEventList.isEmpty())) {

/*
					if((moldMisconfigureList != null && !moldMisconfigureList.isEmpty())){
						String titleMail="Alerts for Reset Request";
						String titleTable;
						if(moldMisconfigures.size() == 1) titleTable = "1 tooling is requesting reset";
						else titleTable = moldMisconfigures.size() + " toolings are requesting reset";
						String content=alertEmailService.generateMailContent(new Object[]{
								titleTable
						});
						sendMailByContent(Arrays.asList(user.getEmail()),titleMail,content);
					}else {

					}
*/
					Message message = SystemMailMessage.builder().alertEmailService(alertEmailService)
							.host(host)
							.receivers(Arrays.asList(user.getEmail()))
							.periodType(periodType)
							.moldLocations(moldLocationList)
							.moldDisconnects(moldDisconnectList)
							.terminalDisconnects(terminalDisconnectList)
							.moldCycleTimes(moldCycleTimeList)
							.moldMaintenances(moldMaintenanceList)
							.moldCorrectives(moldCorrectiveList)
							.moldMisconfigures(moldMisconfigureList)
							.moldEfficiencies(moldEfficiencyList)
							.moldDataSubmissions(moldDataSubmissionList)
							.moldDowntimeEvents(moldDowntimeEventList)
							.build();
					send(message);
					logUserAlertRepository.saveAll(alertsToSend);

				}
			}
		});
	}

	public void sendMailForEachUser_New(Map<User, List<AlertType>> userAlertListMap, PeriodType periodType, List<MoldLocation> moldLocations, List<MoldDisconnect> moldDisconnects,
									List<TerminalDisconnect> terminalDisconnects, List<MoldCycleTime> moldCycleTimes, List<MoldMaintenance> moldMaintenances,
									List<MoldMisconfigure> moldMisconfigures, List<MoldEfficiency> moldEfficiencies, List<MoldDataSubmission> moldDataSubmissions,
									List<MoldCorrective> moldCorrectives, List<MoldDowntimeEvent> moldDowntimeEvents){
		userAlertListMap.forEach((user, alertTypeList) -> {
			List<MoldLocation> moldLocationList = new ArrayList<>();
			List<MoldDisconnect> moldDisconnectList = new ArrayList<>();
			List<TerminalDisconnect> terminalDisconnectList = new ArrayList<>();
			List<MoldCycleTime> moldCycleTimeList = new ArrayList<>();
			List<MoldMaintenance> moldMaintenanceList = new ArrayList<>();
			List<MoldMisconfigure> moldMisconfigureList = new ArrayList<>();
			List<MoldEfficiency> moldEfficiencyList = new ArrayList<>();
			List<MoldDataSubmission> moldDataSubmissionList = new ArrayList<>();
			List<MoldCorrective> moldCorrectiveList = new ArrayList<>();
			List<MoldDowntimeEvent> moldDowntimeEventList = new ArrayList<>();

			List<LogUserAlert> notSentAlerts = logUserAlertRepository.findByUserIdAndEmail(user.getId(), false);
			List<LogUserAlert> alertsToSend = new ArrayList<>();

			if(notSentAlerts != null && notSentAlerts.size() > 0) {
				if (alertTypeList.contains(AlertType.RELOCATION) && moldLocations != null) {
					List<LogUserAlert> notSentRelocationAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.RELOCATION)).collect(Collectors.toList());
					if(notSentRelocationAlert != null && notSentRelocationAlert.size() > 0) {
						List<Long> notSentRelocationAlertIds = notSentRelocationAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldLocationList = moldLocations.stream().filter(x -> notSentRelocationAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentRelocationAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentRelocationAlert);
					}
				}
				if (alertTypeList.contains(AlertType.DISCONNECTED)) {
					if (moldDisconnects != null) {
						List<LogUserAlert> notSentMoldDisconnectionAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.DISCONNECTED)).collect(Collectors.toList());
						if (notSentMoldDisconnectionAlert != null && notSentMoldDisconnectionAlert.size() > 0) {
							List<Long> notSentMoldDisconnectionAlertIds = notSentMoldDisconnectionAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
							moldDisconnectList = moldDisconnects.stream().filter(x -> notSentMoldDisconnectionAlertIds.contains(x.getId())).collect(Collectors.toList());
							notSentMoldDisconnectionAlert.forEach(x -> x.setEmail(true));
							alertsToSend.addAll(notSentMoldDisconnectionAlert);
						}
					}
				}
				if (alertTypeList.contains(AlertType.TERMINAL_DISCONNECTED) || alertTypeList.contains(AlertType.DISCONNECTED)) {
					if (terminalDisconnects != null) {
						List<LogUserAlert> notSentTerminalDisconnectionAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.TERMINAL_DISCONNECTED)).collect(Collectors.toList());
						if(notSentTerminalDisconnectionAlert != null && notSentTerminalDisconnectionAlert.size() > 0){
							List<Long> notSentTerminalDisconnectionAlertIds = notSentTerminalDisconnectionAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
							terminalDisconnectList = terminalDisconnects.stream().filter(x -> notSentTerminalDisconnectionAlertIds.contains(x.getId())).collect(Collectors.toList());
							notSentTerminalDisconnectionAlert.forEach(x -> x.setEmail(true));
							alertsToSend.addAll(notSentTerminalDisconnectionAlert);
						}
					}
				}
				if (alertTypeList.contains(AlertType.CYCLE_TIME) && moldCycleTimes != null) {
					List<LogUserAlert> notSentCycleTimeAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.CYCLE_TIME)).collect(Collectors.toList());
					if(notSentCycleTimeAlert != null && notSentCycleTimeAlert.size() > 0){
						List<Long> notSentCycleTimeAlertIds = notSentCycleTimeAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldCycleTimeList = moldCycleTimes.stream().filter(x -> notSentCycleTimeAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentCycleTimeAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentCycleTimeAlert);
					}
				}
				if (alertTypeList.contains(AlertType.MAINTENANCE) && moldMaintenances != null) {
					List<LogUserAlert> notSentMaintenanceAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.MAINTENANCE)).collect(Collectors.toList());
					if(notSentMaintenanceAlert != null && notSentMaintenanceAlert.size() > 0){
						List<Long> notSentMaintenanceAlertIds = notSentMaintenanceAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldMaintenanceList = moldMaintenances.stream().filter(x -> notSentMaintenanceAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentMaintenanceAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentMaintenanceAlert);
					}
				}

				if (alertTypeList.contains(AlertType.CORRECTIVE_MAINTENANCE) && moldCorrectives != null) {
					List<LogUserAlert> notSentCorrectiveAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.CORRECTIVE_MAINTENANCE)).collect(Collectors.toList());
					if(notSentCorrectiveAlert != null && notSentCorrectiveAlert.size() > 0){
						List<Long> notSentCorrectiveAlertIds = notSentCorrectiveAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldCorrectiveList = moldCorrectives.stream().filter(x -> notSentCorrectiveAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentCorrectiveAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentCorrectiveAlert);
					}
				}

				if (alertTypeList.contains(AlertType.MISCONFIGURE) && moldMisconfigures != null) {
					List<LogUserAlert> notSentMisconfigureAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.MISCONFIGURE)).collect(Collectors.toList());
					if(notSentMisconfigureAlert != null && notSentMisconfigureAlert.size() > 0){
						List<Long> notSentMisconfigureAlertIds = notSentMisconfigureAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldMisconfigureList = moldMisconfigures.stream().filter(x -> notSentMisconfigureAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentMisconfigureAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentMisconfigureAlert);
					}
				}
				if (alertTypeList.contains(AlertType.EFFICIENCY) && moldEfficiencies != null) {
					List<LogUserAlert> notSentEfficiencyAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.EFFICIENCY)).collect(Collectors.toList());
					if(notSentEfficiencyAlert != null && notSentEfficiencyAlert.size() > 0){
						List<Long> notSentEfficiencyAlertIds = notSentEfficiencyAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldEfficiencyList = moldEfficiencies.stream().filter(x -> notSentEfficiencyAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentEfficiencyAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentEfficiencyAlert);
					}
				}
				if(alertTypeList.contains(AlertType.DATA_SUBMISSION) && moldDataSubmissions != null){
					List<LogUserAlert> notSentDataSubmissionAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.DATA_SUBMISSION)).collect(Collectors.toList());
					if(notSentDataSubmissionAlert != null && notSentDataSubmissionAlert.size() > 0){
						List<Long> notSentDataSubmissionAlertIds = notSentDataSubmissionAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldDataSubmissionList = moldDataSubmissions.stream().filter(x -> notSentDataSubmissionAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentDataSubmissionAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentDataSubmissionAlert);
					}
				}
				if(alertTypeList.contains(AlertType.DOWNTIME) && moldDowntimeEvents != null){
					List<LogUserAlert> notSentDowntimeAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.DOWNTIME)).collect(Collectors.toList());
					if(notSentDowntimeAlert != null && notSentDowntimeAlert.size() > 0){
						List<Long> notSentDowntimeAlertIds = notSentDowntimeAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldDowntimeEventList = moldDowntimeEvents.stream().filter(x -> notSentDowntimeAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentDowntimeAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentDowntimeAlert);
					}
				}

				if (!moldEfficiencyList.isEmpty())
					notificationService.createAlertNotification(moldEfficiencyList, AlertType.EFFICIENCY, Collections.singletonList(user.getId()), Instant.now(), periodType);
				if (!moldCycleTimeList.isEmpty())
					notificationService.createAlertNotification(moldCycleTimeList, AlertType.CYCLE_TIME, Collections.singletonList(user.getId()), Instant.now(), periodType);
				if (!moldDisconnectList.isEmpty())
					notificationService.createAlertNotification(moldDisconnectList, AlertType.DISCONNECTED, Collections.singletonList(user.getId()), Instant.now(), periodType);
				if (!terminalDisconnectList.isEmpty())
					notificationService.createAlertNotification(terminalDisconnectList, AlertType.TERMINAL_DISCONNECTED, Collections.singletonList(user.getId()), Instant.now(), periodType);
				if (!moldLocationList.isEmpty())
					notificationService.createAlertNotification(moldLocationList, AlertType.RELOCATION, Collections.singletonList(user.getId()), Instant.now(), periodType);

			}
		});
	}

	public void sendMailForEachUser(Map<User, List<AlertType>> userAlertListMap, PeriodType periodType,List<MoldDetachment> moldDetachments){
		userAlertListMap.forEach((user, alertTypeList) -> {
			List<MoldDetachment> moldDetachmentList = new ArrayList<>();
			List<LogUserAlert> notSentAlerts = logUserAlertRepository.findByUserIdAndEmail(user.getId(), false);
			List<LogUserAlert> alertsToSend = new ArrayList<>();
			if(notSentAlerts != null && notSentAlerts.size() > 0) {
				if (alertTypeList.contains(AlertType.DETACHMENT) && moldDetachments != null) {
					List<LogUserAlert> notSentDetachmentAlert = notSentAlerts.stream().filter(x -> x.getAlertType().equals(AlertType.REFURBISHMENT)).collect(Collectors.toList());
					if(notSentDetachmentAlert != null && notSentDetachmentAlert.size() > 0){
						List<Long> notSentCorrectiveAlertIds = notSentDetachmentAlert.stream().map(x -> x.getAlertId()).collect(Collectors.toList());
						moldDetachmentList = moldDetachments.stream().filter(x -> notSentCorrectiveAlertIds.contains(x.getId())).collect(Collectors.toList());
						notSentDetachmentAlert.forEach(x -> x.setEmail(true));
						alertsToSend.addAll(notSentDetachmentAlert);
					}
				}

			}
			if(!moldDetachmentList.isEmpty()){
				notificationService.createAlertNotification(moldDetachmentList, AlertType.DETACHMENT, Collections.singletonList(user.getId()), Instant.now(), periodType);
			}

		});
	}
	public void sendRegistrationMail(List<User> receivers, List<User> registrationList){
		List<String> receiverEmails = receivers.stream().map(x -> x.getEmail()).distinct().collect(Collectors.toList());
		Message message = SystemMailMessage.builder().alertEmailService(alertEmailService)
				.host(host)
				.receivers(receiverEmails)
				.periodType(PeriodType.REAL_TIME)
				.users(registrationList)
				.build();
		send(message);
	}

	public void sendRegisterConfirmMail(User user, Boolean approval){
		Message message = SystemMailMessage.builder().alertEmailService(alertEmailService)
				.host(host)
				.receivers(Arrays.asList(user.getEmail()))
				.periodType(PeriodType.REAL_TIME)
				.confirmRegistration(approval)
				.build();
		send(message);
	}

	public void sendMailByContent(List<String> receivers, String title, String content) {
		Message message = ContentMailMessage.builder()
				.receivers(receivers)
				.title(title)
				.content(content)
				.build();
		send(message);
	}

	public boolean sendResetPWMail(User user, String token) {
		Message message = ResetPWMessage.builder()
				.host(host)
				.receivers(Arrays.asList(user.getEmail()))
				.user(user)
				.token(token)
				.build();
		send(message);
		return true;
	}
	public boolean sendLockAccountMail(User user, String token) {
		Message message = LockAccountMessage.builder()
				.host(host)
				.receivers(Arrays.asList(user.getEmail()))
				.user(user)
				.token(token)
				.build();
		send(message);
		return true;
	}
	public boolean sendPwdExpiredMail(User user, String token) {
		Message message = PwdExpiredMessage.builder()
				.host(host)
				.receivers(Arrays.asList(user.getEmail()))
				.user(user)
				.token(token)
				.build();
		send(message);
		return true;
	}
	public boolean sendPwdExpireReminderMail(User user, String token) {
		Message message = PwdExpireReminderMessage.builder()
				.host(host)
				.receivers(Arrays.asList(user.getEmail()))
				.user(user)
				.token(token)
				.build();
		send(message);
		return true;
	}

	/**
	 * [BATCH_JOB] 이메일 발송 (배치 처리용)
	 */
	public void sendMail() {
		log.info("[BATCH_JOB_EMAIL] JOB START  ------------------------------------");
		List<BatchMessage> batches = batchMessageRepository.findAllByBatchStatusAndMessageType(BatchStatus.PENDING, MessageType.EMAIL);

		if (batches.size() > 0) {
			batches.stream().forEach(b -> {
				b.setBatchStatus(BatchStatus.IN_PROGRESS);
				b.setStartTime(LocalDateTime.now());
			});

			// InProgress 상태로 변경.
			List<BatchMessage> savedBatches = batchMessageRepository.saveAll(batches);
			batchMessageRepository.flush();


			MimeMessage message = BeanUtils.get(NotiEmailService.class).createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

			for (BatchMessage savedBatch : savedBatches) {
				String[] receivers = StringUtils.delimitedListToStringArray(savedBatch.getReceivers(), ",");

				MessageResult result = new MessageResult();
				result.setTotal(receivers.length);


				log.info("[BATCH_JOB_EMAIL] BatchMessage ID #{} START  ------------------------------------", savedBatch.getId());
				for (String email : receivers) {

					try {
						helper.setTo(email);
						helper.setSubject(savedBatch.getTitle());
						helper.setText(savedBatch.getContent(), true);
						helper.setFrom(savedBatch.getSender());

						BeanUtils.get(NotiEmailService.class).sendQuietly(message);

						result.increaseSuccessCount();
					} catch (Exception e) {
						log.error("[BATCH_JOB_EMAIL] {} : {}", email, e.getMessage());
						e.printStackTrace();
						result.increaseFailureCount(e.getMessage());
					}
				}
				log.info("[BATCH_JOB_EMAIL] BatchMessage ID #{} END : {} ---------------------------------------", savedBatch.getId(), result.toString());

				savedBatch.setEndTime(LocalDateTime.now());
				savedBatch.setBatchResult(result.toString());
				savedBatch.setBatchStatus(BatchStatus.COMPLETED);
				batchMessageRepository.saveAndFlush(savedBatch);
			}
		} else {
			log.info("[BATCH_JOB_EMAIL] Not sent. (BatchMessage size is 0)");
		}

		log.info("[BATCH_JOB_EMAIL] JOB END  ------------------------------------");
	}
}
