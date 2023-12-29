package saleson.common.event;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;

import lombok.extern.slf4j.Slf4j;
import saleson.api.user.UserAlertRepository;
import saleson.common.event.alert.AlertEvent;
import saleson.common.notification.AlertMailMessage;
import saleson.common.notification.MailService;
import saleson.common.notification.Message;
import saleson.common.notification.SmsService;
import saleson.model.UserAlert;

@Slf4j
@Component
public class AlertEventListener {

	@Autowired
	UserAlertRepository userAlertRepository;

	@Autowired
	MailService mailService;

	@Autowired
	SmsService smsService;

	@Async
	@Transactional
	@EventListener
	public void handleAlertEvent(AlertEvent event) {
		log.info("[ALERT_EVENT_HANDLER] Alert event is occurs! ({})", event);

		// 1. 메세지 발송 대상자 조회 (AlertType)
		List<UserAlert> userAlerts = userAlertRepository.findAllByAlertType(event.getAlertType());

		// 2. 이메일 발송
		String moldCode = null;
		try {
			moldCode = event.getMold() == null ? null : event.getMold().getEquipmentCode();

			// 2.1. 메일 발송 대상자 추출
			List<String> emailReceivers = userAlerts.stream()//
					.filter(ua -> (ua.getAlertOn() != null && ua.getAlertOn()) //
							&& (ua.getEmail() != null && ua.getEmail()) //
							&& !ObjectUtils.isEmpty(ua.getUser().getEmail()))//
					.map(ua -> ua.getUser().getEmail())//
					.collect(Collectors.toList());

			// 2-2. 메일 메세지 작성 및 발송
			log.info("[ALERT_EVENT_HANDLER] Email alert count: ({})", emailReceivers.size());
			if (emailReceivers.size() > 0) {
				Message message = AlertMailMessage.builder()//
						.receivers(emailReceivers)//
						.alertType(event.getAlertType())//
						.toolingId(moldCode)//
						.build();

				// 메일 발송
				mailService.send(message);
			}
		} catch (Exception e) {
			log.error("[ALERT_EVENT_HANDLER] EMAIL SEND ERROR!! ({})", e.getMessage());
			LogUtils.saveErrorQuietly(ErrorType.SYS, "MAIL_ALERT_FAIL", HttpStatus.INTERNAL_SERVER_ERROR,
					"Sending Mail Fail. alertType:" + event.getAlertType() + ", moldCode:" + moldCode, e);
		}

		// 3. SMS 발송
//		try {
//			// 3.1. SMS 발송 대상자 추출
//			List<String> smsReceivers = userAlerts.stream()
//					.filter(ua -> ua.getSms() == true)
//					.filter(ua -> ua.getUser().getMobileNumber() != null && !ua.getUser().getMobileNumber().isEmpty()
//							&& ua.getUser().getMobileDialingCode() != null && !ua.getUser().getMobileDialingCode().isEmpty()
//					)
//					.map(ua -> "+" + ua.getUser().getMobileDialingCode() + " " + ua.getUser().getMobileNumber())
//					.collect(Collectors.toList());
//
//
//			// 3-2. SMS 메세지 작성 및 발송
//			log.info("[ALERT_EVENT_HANDLER] SMS alert count: ({})", smsReceivers.size());
//			if (smsReceivers.size() > 0) {
//				Message message = AlertSmsMessage.builder()
//						.receivers(smsReceivers)
//						.alertType(event.getAlertType())
//						.build();
//
//				// SMS 발송
//				smsService.send(message);
//			}
//		} catch (Exception e) {
//			log.error("[ALERT_EVENT_HANDLER] SMS SEND ERROR!! ({})", e.getMessage());
//			e.printStackTrace();
//		}

		log.info("[ALERT_EVENT_HANDLER] END (good job!) -------------------------", event);
	}

	//@Async
	//@EventListener(condition = "#event.success")
	/*@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, classes = AlertEvent.class)
	public void handleEvent(AlertEvent<MoldCorrective> event) {
		System.out.println("Handle Event1111 : " + event);
	}

	@EventListener(condition = "#event.alertType == T(saleson.common.enumeration.AlertType).EFFICIENCY")
	public void handleEvent1(AlertEvent<MoldEfficiency> event) {
		System.out.println("Handle Event333 : " + event);
	}

	@EventListener(condition = "#event.alertType == T(saleson.common.enumeration.AlertType).MISCONFIGURE")
	public void handleEvent22(AlertEvent<MoldCorrective> event) {
		System.out.println("Handle Event333 : " + event);
	}*/

	/*@Async
	//@EventListener(classes = AlertEvent.class, condition = )
	@EventListener(condition = "#event.alertType == T(saleson.common.enumeration.AlertType).MISCONFIGURE")
	public void handleEvent2(AlertEvent event) {
		System.out.println("###############################Handle Event11111 : " + event);
	}*/

	/*@Async
	@EventListener(AlertEvent2.class)
	public void handleEvent2(AlertEvent2 event) {
		System.out.println("###############################Handle Event22222 : " + event);
	}*/
}
