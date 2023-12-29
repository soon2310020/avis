package saleson.common.scheduling;

import java.time.Duration;
import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.ReflectionUtils;

import saleson.api.data.registration.DataRegistrationService;
import saleson.api.dataCompletionRate.DataCompletionRateService;
import saleson.api.dataRequest.DataRequestService;
import saleson.api.endLifeCycle.MoldEndLifeCycleService;
import saleson.api.machine.MachineStatisticsService;
import saleson.api.mold.MoldService;
import saleson.common.notification.MailService;
import saleson.common.notification.SmsService;
import saleson.common.util.DateUtils;

@Profile("!developer")
@Component
@EnableScheduling
@ConditionalOnProperty(prefix = "saleson.mms.scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ScheduledTask {

	/**
	 *  장비 OperationStatus 상태 변경 (터미널, 카운트, 몰드)
	 *  DisconnectedAlertEvent
	 */
	@Scheduled(fixedDelay = 120000, initialDelay = 30000)
	public void updateOperatingStatus() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(ScheduledTask.class, "updateOperStatus"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true),
				() -> BeanUtils.get(BatchJob.class).updateOperatingStatus());
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(ScheduledTask.class, "procDowntimeAlert"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true),
				() -> BeanUtils.get(BatchJob.class).procDowntimeAlerts());
	}

	/**
	 * 메시지 발송 (Email, SMS)
	 */
	@Scheduled(fixedDelay = 60000, initialDelay = 60000)
	public void sendMessage() {
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(ScheduledTask.class, "sendMessage"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
			BeanUtils.get(MailService.class).sendMail();
			BeanUtils.get(SmsService.class).sendSms();
		});
	}

	@Scheduled(cron = "0 0 7 * * *")
	public void sendAlert() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(ScheduledTask.class, "sendAlert"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
			BeanUtils.get(MailService.class).sendSystemAlert();
			BeanUtils.get(MailService.class).sendReminder();
			BeanUtils.get(MailService.class).sendReminderInviteUser();
		});
	}

	@Scheduled(cron = "0 0 6 * * *")
	public void procAlerts() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(ScheduledTask.class, "ALERTS"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
			// Generate (Daily/Weekly/Monthly) Efficiency and CycleTime Alerts by Molds
			try {
				BeanUtils.get(MoldService.class).procEfficiencyAndCycleTimeAlerts();
			} catch (Exception e) {
				LogUtils.saveErrorQuietly(ErrorType.SYS, "MOLD_ALERT_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
			}

			try {
				BeanUtils.get(MoldService.class).procAlertsToUsers();
			} catch (Exception e) {
				LogUtils.saveErrorQuietly(ErrorType.SYS, "USER_ALERT_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
			}

			// Update mold end of life cycle
			try {
				BeanUtils.get(MoldEndLifeCycleService.class).procAll();
			} catch (Exception e) {
				LogUtils.saveErrorQuietly(ErrorType.SYS, "MOLD_ELC_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
			}

			// Update accumulating shot
			try {
				BeanUtils.get(MoldEndLifeCycleService.class).procAccumulativeShotAll();
			} catch (Exception e) {
				LogUtils.saveErrorQuietly(ErrorType.SYS, "ACC_SHOT_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
			}

			// Update due date of Mold maintenance
			try {
				BeanUtils.get(MoldService.class).procMaintenanceDueDateAll();
			} catch (Exception e) {
				LogUtils.saveErrorQuietly(ErrorType.SYS, "MOLD_MAINT_DATE_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
			}

			// Update Mold passed days
			try {
				BeanUtils.get(MoldService.class).updatePassedDays();
			} catch (Exception e) {
				LogUtils.saveErrorQuietly(ErrorType.SYS, "MOLD_PASSED_DAY_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
			}
		});
	}

	@Scheduled(cron = "0 10 7 * * *")
	public void sendRemindMailAfter3Days() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(ScheduledTask.class, "mailAfter3Days"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
			Instant instant = Instant.now().minus(Duration.ofDays(3));
			String date = DateUtils2.format(instant, DatePattern.yyyyMMdd, Zone.SYS);
			BeanUtils.get(DataRegistrationService.class).sendRemindMail(date, false);
		});
	}

	@Scheduled(cron = "0 20 7 * * *")
	public void sendRemindMailAfter7Days() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(ScheduledTask.class, "mailAfter7Days"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
			Instant instant = Instant.now().minus(Duration.ofDays(7));
			String date = DateUtils2.format(instant, DatePattern.yyyyMMdd, Zone.SYS);
			BeanUtils.get(DataRegistrationService.class).sendRemindMail(date, true);
		});
	}

	@Scheduled(cron = "0 30 7 * * *")
	public void sendRemindMailDataCompletionAfterDueDay() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(ScheduledTask.class, "mailDCAfterDueDay"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true),
				() -> {
					String yesterday = DateUtils.getYesterday("yyyyMMdd");
					BeanUtils.get(DataCompletionRateService.class).sendRemindMail(yesterday, false);
				});
	}

	@Scheduled(cron = "0 40 7 * * *")
	public void sendRemindMailDataCompletion3DaysAfterDueDay() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(ScheduledTask.class, "mailDC3DaysAfterDueDay"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true),
				() -> {
					Instant instant = Instant.now().minus(Duration.ofDays(4));
					String date = DateUtils2.format(instant, DatePattern.yyyyMMdd, Zone.SYS);
					BeanUtils.get(DataCompletionRateService.class).sendRemindMail(date, true);
				});
	}

	@Scheduled(cron = "0 10 6 * * *")
	public void updateDailyOEE() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(ScheduledTask.class, "updateDailyOEE"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
			String dayPattern = "yyyyMMdd";
			String yesterday = DateUtils.getYesterday(dayPattern);
			BeanUtils.get(MachineStatisticsService.class).updateOEEByDay(yesterday);
		});
	}

	@Scheduled(cron = "0 0 8 * * ?")
	public void pushNotificationDataRequest() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(ScheduledTask.class, "pushNotificationDataRequest"),
				new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
					BeanUtils.get(DataRequestService.class).pushNotificationOverdue();
				});
	}

	@Scheduled(cron = "0 0 8 * * ?")
	public void scheduleMaintainMoldByTime() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(ScheduledTask.class, "scheduleMaintainMoldByTime"),
				new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
					BeanUtils.get(MoldService.class).scheduleMaintainMoldByTime();
				});
	}
}
