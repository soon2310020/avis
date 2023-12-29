package com.emoldino.api.integration.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.integration.kafka.message.KafkaMessage;
import com.emoldino.api.integration.resource.composite.msghdr.service.AiMessageHandler;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.RequiredArgsConstructor;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
public class KafkaMessageConsumer {
	
	@KafkaListener(topics = "#{'${mms.topic.name}'}", groupId = "#{'${customer.server.name}'}", containerFactory = "mmsKafkaListenerContainerFactory")	
	public void receiveFromAIServer(@Payload ConsumerRecord<?, ?> consumerRecord, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
		if (!ObjectUtils.isEmpty(consumerRecord)) {			
			KafkaMessage msg = ValueUtils.fromJsonStr((String) consumerRecord.value(), KafkaMessage.class);
			BeanUtils.get(AiMessageHandler.class).receiveMessage(msg);
		}
	}
	
	/*
	// If you would like to view messages from other topics, please add them as below.
	// *** Consumer Example ***	
	@KafkaListener(topics = "#{'${topic.name}'}")
	public void receiveFromOtherSystem(ConsumerRecord<?, ?> consumerRecord) {
		if(!ObjectUtils.isEmpty(consumerRecord)) { 
			// log.info("Receive message=[" + consumerRecord.toString() + "]");			
			// ***
			// The logic of handling kafka message  
			// ***			
		}        
	}	 
	 * */
}


