package com.emoldino.api.integration.kafka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class KafkaConfig {	
	@Value(value = "${kafka.enabled:false}")
	private boolean kafkaEnabled;
	
	@Value(value = "${spring.kafka.bootstrap-servers}")	
	private String bootstrapServers;
	
	@Value(value = "${spring.kafka.consumer.group-id}")		
	private String groupId;

	@Value(value = "${customer.server.name:emoldino-customer}")	
	private String serverName;
	
	/** If you want to use a topic, add it below. **/
	@Value(value = "${ai.topic.name:ai.emoldino.ai-analysis}") 	
	private String aiFetchTopic;
		
	@Value(value = "${mms.topic.name:mms.dev.ai-result}") 
	private String mmsTopic;
}
