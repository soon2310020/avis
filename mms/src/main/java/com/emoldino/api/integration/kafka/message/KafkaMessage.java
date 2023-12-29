package com.emoldino.api.integration.kafka.message;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class KafkaMessage {
	String message;
	String topic;
	String returnTopic;
	private Map<String, Object> content;
}
