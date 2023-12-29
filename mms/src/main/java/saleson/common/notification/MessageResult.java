package saleson.common.notification;

import lombok.Data;

@Data
public class MessageResult {
	private int total;
	private int success;
	private int failure;
	private String messages = "";


	public void increaseSuccessCount() {
		this.success++;
	}

	public void increaseFailureCount() {
		this.failure++;
	}

	public void increaseFailureCount(String errorMessage) {
		if (failure > 0) {
			this.messages += ", ";
		}
		this.messages += errorMessage;
		this.failure++;
	}
}
