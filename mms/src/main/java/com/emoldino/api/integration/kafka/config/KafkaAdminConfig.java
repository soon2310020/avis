package com.emoldino.api.integration.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import lombok.RequiredArgsConstructor;

@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
public class KafkaAdminConfig {	

	private final KafkaConfig kafkaConfig;
	
	@Bean
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> cfg = new HashMap<>();
		cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
		cfg.put("security.protocol", "SASL_SSL");
		cfg.put("sasl.mechanism", "AWS_MSK_IAM");
		cfg.put("sasl.jaas.config", "software.amazon.msk.auth.iam.IAMLoginModule required;");
		cfg.put("ssl.endpoint.identification.algorithm", "https");
		cfg.put("client.id", kafkaConfig.getServerName());
		cfg.put("sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.IAMClientCallbackHandler");
		return new KafkaAdmin(cfg);
	}

	/** If you want to create a new topic for use with the MMS, add it below. **/
	// [Emoldino Topic Policy] -> Default Partitions : 3, Default Replication Factor : 2
	@Bean
	public NewTopic mmsTopic() {
		return new NewTopic(kafkaConfig.getMmsTopic(), 3, (short) 2);
	}
}
