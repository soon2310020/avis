package saleson.common.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.MessageType;

import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
public class MailMessage implements Message {

	private List<String> receivers;
	private String title;
	private String content;


	@Override
	public MessageType getMessageType() {
		return MessageType.EMAIL;
	}

	@Override
	public List<String> getReceivers() {
		return this.receivers;
	}

	@Override
	public String getFrom() {
		return "eMoldino<noreply@emoldino.com>";
	}
	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getContent() {
		return content;
	}
}
