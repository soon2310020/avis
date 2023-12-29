package saleson.common.notification;

import lombok.*;
import saleson.common.enumeration.AlertType;
import saleson.common.util.DateUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper=false)
public class AlertMailMessage extends MailMessage {

	private AlertType alertType;
	private String toolingId;
	private String dateTime;

	@Builder
	private AlertMailMessage(List<String> receivers, String title, String content, AlertType alertType, String toolingId) {
		super(receivers, title, content);
		this.alertType = alertType;
		this.toolingId = toolingId;
	}

	@Builder
	private AlertMailMessage(List<String> receivers, String title, String content, AlertType alertType, String toolingId, Instant dateTime) {
		super(receivers, title, content);
		this.alertType = alertType;
		this.toolingId = toolingId;
		this.dateTime = DateUtils.getDateTime(dateTime);
	}


	@Override
	public String getTitle() {
		return "[eShotLink] " + alertType.getTitle() + " alert occurs !";
	}

	@Override
	public String getContent() {
		String content = "" +
				"<div style=\"border: 1px solid #ccc; border-radius: 7px; max-width: 660px; padding: 20px; font-size: 16px; background: #fbfbfb; min-height: 85px; overflow: hidden;\">\n" +
				"	<img src=\"http://skc.coding.onlinepowers.com/icon-alert.png\" style=\"width: 100px; height: 85px; float: left; margin-right: 15px;\">\n" +
				"	<p style=\"padding: 0; margin:0; padding-top: 15px; line-height: 1.7\">eShotLink is notifying <i style=\"font-weight: bold; color: #2B91AF; text-decoration: underline\">\"" + alertType.getTitle() + "\" alert</i> at " + dateTime + "<br />\n" +
				"   Tooling ID <b>" + toolingId + "</b> needs to be checked now.</p>\n" +
				"</div>";
		return content;
	}
}
