package saleson.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import saleson.model.BatchMessage;

@Slf4j
@Component
public class SmsSender {

	/**
	 * 임시로 만든 SMS Sender
	 * SMS Vendor Api를 이곳에 적용하여 실제 SMS가 발송되도록 하자!!
	 * @param batchMessage
	 */
	public void send(BatchMessage batchMessage) {
		log.info("[SMS_SENDER] Text message sent!! {}" + batchMessage);
	}


	public void send(String mobileNumber) {
		log.info("[SMS_SENDER] Text message sent!! {}" + mobileNumber);
	}
}
