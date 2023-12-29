package saleson.common.notification;

import saleson.common.enumeration.MessageType;

import java.util.List;

public interface Message {
	MessageType getMessageType();
	List<String> getReceivers();
	String getFrom();
	String getTitle();
	String getContent();
}
