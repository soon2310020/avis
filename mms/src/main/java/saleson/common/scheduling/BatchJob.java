package saleson.common.scheduling;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.statuscontrol.service.StatusControlService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.TranUtils;

import saleson.api.mold.MoldDisconnectRepository;
import saleson.api.mold.MoldDowntimeEventRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.notification.NotificationService;
import saleson.api.terminal.TerminalDisconnectRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.api.user.UserAlertRepository;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.DowntimeStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.Event;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.PeriodType;
import saleson.common.notification.MailService;
import saleson.model.LogUserAlert;
import saleson.model.Mold;
import saleson.model.MoldDisconnect;
import saleson.model.MoldDowntimeEvent;
import saleson.model.Terminal;
import saleson.model.TerminalDisconnect;
import saleson.model.User;
import saleson.model.UserAlert;
import saleson.service.transfer.LogDisconnectionService;
import saleson.service.transfer.LogUserAlertRepository;

// @Slf4j
@Component
public class BatchJob {

	@Value("${app.batch-job.mold-disconnect.enabled:true}")
	private boolean moldDisconnectEnabled;
	@Value("${app.batch-job.terminal-disconnect.enabled:true}")
	private boolean terminalDisconnectEnabled;

	@Autowired
	MoldRepository moldRepository;

	@Autowired
	MoldDisconnectRepository moldDisconnectRepository;

	@Autowired
	TerminalRepository terminalRepository;

	@Autowired
	TerminalDisconnectRepository terminalDisconnectRepository;

	@Autowired
	UserAlertRepository userAlertRepository;

	@Autowired
	LogUserAlertRepository logUserAlertRepository;

	@Autowired
	LogDisconnectionService logDisconnectionService;

	@Autowired
	MailService mailService;

	@Lazy
	@Autowired
	MoldService moldService;

	@Autowired
	MoldDowntimeEventRepository moldDowntimeEventRepository;

	@Autowired
	ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	NotificationService notificationService;

	/**
	 * [BATCH_JOB] MOLD operation 상태를 업데이트 하고 disconnected alert 이벤트를 발생한다.
	 *
	 * - 장비 OperationStatus 상태 변경 (터미널, 카운트, 몰드)
	 * - MoldRepository.updateMoldOperatingStatus();
	 * - DisconnectedAlertEvent
	 */
	@Transactional(propagation = Propagation.NEVER)
	public void updateOperatingStatus() {
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(BatchJob.class, "moldOperStatus"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true),
				() -> BeanUtils.get(StatusControlService.class).updateBatch());

		List<UserAlert> userAlertList = new ArrayList<>();
		Map<User, List<AlertType>> userAlertMap = new LinkedHashMap<>();
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(BatchJob.class, "findUserAlert"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
			userAlertList.addAll(TranUtils.doNewTran(() -> userAlertRepository.findByAlertTypeIn(Arrays.asList(AlertType.DISCONNECTED, AlertType.TERMINAL_DISCONNECTED))));
			userAlertMap.putAll(mailService.getUserAlertListMap(userAlertList));
		});

		if (moldDisconnectEnabled) {
			List<Mold> molds = new ArrayList<>();
			List<Long> moldIds = new ArrayList<>();
			List<MoldDisconnect> moldDisconnects = new ArrayList<>();

			JobUtils.runIfNotRunning(ReflectionUtils.toShortName(BatchJob.class, "moldDiscon"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
				TranUtils.doNewTran(() -> {
					// disconnect Mold 조회.
					// 1. DISCONNECTED인 Mold 조회
					molds.addAll(moldRepository.findAllByOperatingStatus(OperatingStatus.DISCONNECTED));
					moldIds.addAll(molds.stream().map(x -> x.getId()).collect(Collectors.toList()));
					// 2. MOLD_DISCONNECT NotificationStatus.ALERT인 데이터 조회
					moldDisconnects.addAll(moldDisconnectRepository.findAllByNotificationStatus(NotificationStatus.ALERT));
				});
				moldDisconnects.forEach(moldDisconnect -> {
					if (!moldIds.contains(moldDisconnect.getMoldId())) {
						TranUtils.doNewTranQuietly(() -> logDisconnectionService.save(moldDisconnect.getId(), moldDisconnect.getMold(), Event.RECONNECT));

						List<MoldDisconnect> moldDisconnectList = TranUtils
								.doNewTran(() -> moldDisconnectRepository.findByMoldIdAndNotificationStatusIsInAndLatest(moldDisconnect.getMoldId(),
										Arrays.asList(NotificationStatus.FIXED, NotificationStatus.CONFIRMED), true));
						if (!ObjectUtils.isEmpty(moldDisconnectList)) {
							moldDisconnectList.forEach(td -> {
								td.setLatest(false);
								TranUtils.doNewTranQuietly(() -> moldDisconnectRepository.save(td));
							});
						}
						moldDisconnect.setNotificationStatus(NotificationStatus.FIXED);
						moldDisconnect.setNotificationAt(Instant.now());
						moldDisconnect.setLatest(true);

						TranUtils.doNewTranQuietly(() -> moldDisconnectRepository.save(moldDisconnect));
					}
				});
			});

			// 3. MOLD_DISCONNECT에 ALERT 으로 등록되지 않은 MOLD 정보를 MOLD_DISCONNECT에 등록한 후 ALERT EVENT
			JobUtils.runIfNotRunning(ReflectionUtils.toShortName(BatchJob.class, "moldDisconAlert"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
				molds.stream().filter(m -> {
					long matchedCount = moldDisconnects.stream().filter(md -> md.getMold().getId().equals(m.getId())).count();
					return matchedCount <= 0 ? true : false;
				}).forEach(m -> {
					MoldDisconnect moldDisconnected = new MoldDisconnect();
					moldDisconnected.setMold(m);
					moldDisconnected.setNotificationAt(m.getOperatedAt());
					moldDisconnected.setNotificationStatus(NotificationStatus.ALERT);
					moldDisconnected.setLatest(true);

					TranUtils.doNewTranQuietly(() -> moldDisconnectRepository.save(moldDisconnected));
					TranUtils.doNewTranQuietly(() -> logDisconnectionService.save(moldDisconnected.getId(), m, Event.DISCONNECT));
					//notification
//					notificationService.createAlertNotification(moldDisconnected, AlertType.DISCONNECTED);

					List<LogUserAlert> logUserAlertList = TranUtils
							.doNewTran(() -> moldService.buildLogUserAlert(userAlertMap, null, Arrays.asList(moldDisconnected), null, null, null, null, null, null, null, true));
					logUserAlertList.forEach(item -> TranUtils.doNewTranQuietly(() -> logUserAlertRepository.save(item)));

					// Send mails to users with activated Email Notification
					List<UserAlert> toSendMailUserAlertList = userAlertList.stream().filter(x -> (x.getAlertOn() != null && x.getAlertOn()) && x.getEmail() == true)
							.collect(Collectors.toList());
					Map<User, List<AlertType>> toSendMailUserAlertMap = mailService.getUserAlertListMap(toSendMailUserAlertList);
					TranUtils.doNewTranQuietly(() -> mailService.sendMailForEachUser_New(toSendMailUserAlertMap, PeriodType.REAL_TIME, null, Arrays.asList(moldDisconnected), null,
							null, null, null, null, null, null, null));

					// Set status true for sent email users
//					List<Long> sentMailUserIds = toSendMailUserAlertList.stream().map(x -> x.getUser().getId()).collect(Collectors.toList());
//					List<Long> sentEmailAlertIds = logUserAlertList.stream().map(x -> x.getAlertId()).distinct().collect(Collectors.toList());
//					List<LogUserAlert> sentMailLogUserAlertList = TranUtils
//							.doNewTran(() -> logUserAlertRepository.findByUserIdInAndAlertTypeAndAlertIdIn(sentMailUserIds, AlertType.DISCONNECTED, sentEmailAlertIds));
//					sentMailLogUserAlertList.forEach(item -> {
//						item.setEmail(true);
//						TranUtils.doNewTranQuietly(() -> logUserAlertRepository.save(item));
//					});

					// [AlertEevent] 메세지 발송
					/*AlertEvent alertEvent = new DisconnectAlertEvent(moldDisconnected);
					applicationEventPublisher.publishEvent(alertEvent);
					
					log.info("[ALERT EVENT] Mold is disconnected!!");*/
				});
			});
		}

		if (terminalDisconnectEnabled) {
			List<Terminal> terms = new ArrayList<>();
			List<Long> termAlertedIds = new ArrayList<>();
			// 4. TERMINAL_DISCONNECT
			JobUtils.runIfNotRunning(ReflectionUtils.toShortName(BatchJob.class, "termDiscon"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
				List<TerminalDisconnect> termDisconAlertedList = new ArrayList<>();
				TranUtils.doNewTran(() -> {
					terms.addAll(terminalRepository.findAllByOperatingStatusAndEquipmentStatus(OperatingStatus.NOT_WORKING, EquipmentStatus.INSTALLED));
					termDisconAlertedList.addAll(terminalDisconnectRepository.findAllByNotificationStatus(NotificationStatus.ALERT));
				});

				List<Long> terminalIds = terms.stream().map(x -> x.getId()).collect(Collectors.toList());
				List<Terminal> termAlerted = termDisconAlertedList.stream().map(x -> x.getTerminal()).collect(Collectors.toList());
				termAlertedIds.addAll(termAlerted.stream().map(x -> x.getId()).collect(Collectors.toList()));

				// Remove all alert of terminal that's reconnected
				// Disconnected terminals are alerted but not exist the status any more -> reconnected -> remove from alert
				List<Terminal> inactiveTerminalsAlerted = termAlerted.stream().filter(x -> !terminalIds.contains(x.getId())).collect(Collectors.toList());
				List<TerminalDisconnect> termDisconToRemoveList = termDisconAlertedList.stream().filter(x -> inactiveTerminalsAlerted.contains(x.getTerminal()))
						.collect(Collectors.toList());

				termDisconToRemoveList.forEach(item -> {
					TranUtils.doNewTranQuietly(() -> logDisconnectionService.save(item.getId(), item.getTerminal(), Event.RECONNECT));
					List<TerminalDisconnect> list = TranUtils.doNewTran(() -> terminalDisconnectRepository.findByTerminalIdAndNotificationStatusIsInAndLatest(item.getTerminalId(),
							Arrays.asList(NotificationStatus.FIXED, NotificationStatus.CONFIRMED), true));
					if (!ObjectUtils.isEmpty(list)) {
						list.forEach(td -> {
							td.setLatest(false);
							TranUtils.doNewTranQuietly(() -> terminalDisconnectRepository.save(td));
						});
					}
					item.setNotificationStatus(NotificationStatus.FIXED);
					item.setNotificationAt(Instant.now());
					item.setLatest(true);
					TranUtils.doNewTranQuietly(() -> terminalDisconnectRepository.save(item));
				});
			});

			JobUtils.runIfNotRunning(ReflectionUtils.toShortName(BatchJob.class, "termDisconAlert"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
				// New disconnections
				List<Terminal> inactiveTermNotAlertedList = terms.stream().filter(x -> !termAlertedIds.contains(x.getId())).collect(Collectors.toList());
//						List<UserAlert> userAlertList = userAlertRepository.findByAlertTypeAndPeriodType(AlertType.DISCONNECTED, PeriodType.REAL_TIME);
				inactiveTermNotAlertedList.forEach(term -> {
					TerminalDisconnect termDiscon = new TerminalDisconnect();
					termDiscon.setTerminal(term);
					termDiscon.setNotificationAt(term.getOperatedAt());
					termDiscon.setNotificationStatus(NotificationStatus.ALERT);
					termDiscon.setLatest(true);
					TranUtils.doNewTranQuietly(() -> terminalDisconnectRepository.save(termDiscon));

					TranUtils.doNewTranQuietly(() -> logDisconnectionService.save(termDiscon.getId(), term, Event.DISCONNECT));

//					userAlertList.forEach(userAlert -> {
//						userAlert.setAlertType(AlertType.TERMINAL_DISCONNECTED);
//					});
					List<TerminalDisconnect> terminalDisconnectList = Arrays.asList(termDiscon);
					//notification
//					notificationService.createAlertNotification(termDiscon, AlertType.TERMINAL_DISCONNECTED);
//							Map<User, List<AlertType>> userAlertMap = mailService.getUserAlertListMap(userAlertList);
					List<LogUserAlert> logUserAlertList = TranUtils
							.doNewTran(() -> moldService.buildLogUserAlert(userAlertMap, null, null, terminalDisconnectList, null, null, null, null, null, null, true));
					logUserAlertList.forEach(item -> logUserAlertRepository.save(item));

					// Send mails to users with activated Email Notification
					List<UserAlert> toSendMailUserAlertList = userAlertList.stream().filter(x -> (x.getAlertOn() != null && x.getAlertOn()) && x.getEmail() == true)
							.collect(Collectors.toList());
					Map<User, List<AlertType>> toSendMailUserAlertMap = mailService.getUserAlertListMap(toSendMailUserAlertList);
					/*
					//add TERMINAL_DISCONNECTED because not config for TERMINAL_DISCONNECTED
					toSendMailUserAlertMap.keySet().stream().forEach(key->{
						List<AlertType> alertTypes = toSendMailUserAlertMap.get(key);
						if(alertTypes.contains(AlertType.DISCONNECTED) && !alertTypes.contains(AlertType.TERMINAL_DISCONNECTED)){
							alertTypes.add(AlertType.TERMINAL_DISCONNECTED);
						}
					});
					*/
					TranUtils.doNewTranQuietly(() -> mailService.sendMailForEachUser_New(toSendMailUserAlertMap, PeriodType.REAL_TIME, null, null, terminalDisconnectList, null, null,
							null, null, null, null, null));

					// Set status true for sent email users
//					List<Long> sentMailUserIds = toSendMailUserAlertList.stream().map(x -> x.getUser().getId()).collect(Collectors.toList());
//					List<Long> sentEmailAlertIds = logUserAlertList.stream().map(x -> x.getAlertId()).distinct().collect(Collectors.toList());
//					List<LogUserAlert> sentMailLogUserAlertList = TranUtils
//							.doNewTran(() -> logUserAlertRepository.findByUserIdInAndAlertTypeAndAlertIdIn(sentMailUserIds, AlertType.TERMINAL_DISCONNECTED, sentEmailAlertIds));
//					sentMailLogUserAlertList.forEach(item -> {
//						item.setEmail(true);
//						TranUtils.doNewTranQuietly(() -> logUserAlertRepository.save(item));
//					});

					/*DisconnectTerminalAlertEvent alertEvent = new DisconnectTerminalAlertEvent(terminalDisconnected);
					applicationEventPublisher.publishEvent(alertEvent);
					log.info("[ALERT EVENT] Mold is disconnected!!");*/
				});
			});
		}

//		log.info("[BATCH_JOB_OPERATIONAL_STATUS] JOB END  ------------------------------------");
	}

	@Transactional(propagation = Propagation.NEVER)
	public void procDowntimeAlerts() {
		List<MoldDowntimeEvent> list = TranUtils.doNewTran(() -> {
			List<Mold> moldDownList = moldService.findListMoldDown();
			List<Long> moldDownIds = moldDownList.stream().map(x -> x.getId()).collect(Collectors.toList());

			// Handle current alerts, update downtime
			List<MoldDowntimeEvent> events = moldService.findMoldDowntimeEventByMoldIdInAndDowntimeStatusAndLatest(moldDownIds, DowntimeStatus.DOWNTIME, true);
			events.forEach(event -> {
				Long downtimeSeconds = Instant.now().getEpochSecond() - event.getLastUptime().getEpochSecond();
				event.setDowntimeSeconds(downtimeSeconds);
			});

			// Handle new alert
			List<Long> moldIdsInEvents = events.stream()//
					.map(x -> x.getMoldId())//
					.collect(Collectors.toList());
			List<Mold> moldDownListNew = moldDownList.stream()//
					.filter(x -> !moldIdsInEvents.contains(x.getId()))//
					.collect(Collectors.toList());
			moldDownListNew.forEach(mold -> {
				Instant lastUptime = mold.getLastShotMadeAt() != null ? mold.getLastShotMadeAt() : (mold.getCreatedAt() != null ? mold.getCreatedAt() : Instant.now());
				MoldDowntimeEvent event = new MoldDowntimeEvent();
				event.setMold(mold);
				event.setLastUptime(lastUptime);
				event.setDowntimeSeconds(Instant.now().getEpochSecond() - lastUptime.getEpochSecond());
				event.setDowntimeStatus(DowntimeStatus.DOWNTIME);
				event.setLatest(true);
				events.add(event);
			});

			return events;
		});

		list.forEach(event -> TranUtils.doNewTran(() -> {
			Long moldId = event.getMoldId() == null ? event.getMold().getId() : event.getMoldId();
			BeanUtils.get(MoldRepository.class).findWithPessimisticLockById(moldId);
			if (moldRepository.findByIdAndLastShotMadeAtBeforeAndDeletedIsFalse(moldId, DateUtils2.getInstant().minus(Duration.ofHours(1))).isPresent()) {
				moldDowntimeEventRepository.save(event);
			}
		}));
	}
}
