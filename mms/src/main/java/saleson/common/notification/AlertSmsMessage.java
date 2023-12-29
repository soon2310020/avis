package saleson.common.notification;

import lombok.*;
import saleson.common.enumeration.AlertType;

import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor  @EqualsAndHashCode(callSuper=false)
public class AlertSmsMessage extends SmsMessage {

	private AlertType alertType;


	@Builder
	private AlertSmsMessage(List<String> receivers, String title, String content, AlertType alertType) {
		super(receivers, title, content);
		this.alertType = alertType;
	}

	@Override
	public String getContent() {
		// [eShotLink] [Alert name] alert occurs ! [short URL]
		// [eShotLink] Cycle time alert occurs ! https://bit.ly/2T6mlMP
		return new StringBuilder()
				.append("[eShotLink] ")
				.append(alertType.getTitle())
				.append(" alert occurs! ")
				.toString();
	}
}
