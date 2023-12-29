package saleson.common.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.MessageType;

import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
public class SmsMessage implements Message {

	private List<String> receivers;
	private String title;
	private String content;


	@Override
	public MessageType getMessageType() {
		return MessageType.SMS;
	}

	@Override
	public List<String> getReceivers() {
		return this.receivers;
	}

	@Override
	public String getFrom() {
		return "+82 01091099252";
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public String getContent() {
		return content;
	}
}
