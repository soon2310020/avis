package com.emoldino.api.integration.kafka.producer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.api.integration.kafka.message.KafkaMessage;
import com.emoldino.api.integration.util.KafkaMessageUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import saleson.model.integration.InfMessage;

@Slf4j
@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
public class KafkaMessageProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void sendMessage(String topic, Object message) {

		ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, message);

		future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {

			@Override
			public void onSuccess(SendResult<String, Object> result) {
				//log.info("Sent message =" + message.toString());
				//log.info("offset = " + result.getRecordMetadata().offset());
			}

			@Override
			public void onFailure(Throwable ex) {
				KafkaMessageUtils.createInfMessage("O", (KafkaMessage) message, "AI", "REQ_ERROR", new Exception(ex));
				log.error("Unable to send message= " + message.toString());
				log.error("Exception = " + ex.getMessage());
			}
		});
	}

	public void retrySendMessage(String topic, InfMessage infMessage) {
		KafkaMessage message = ValueUtils.fromJsonStr(infMessage.getReqMessage(), KafkaMessage.class);
		ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, message);

		future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {

			@Override
			public void onSuccess(SendResult<String, Object> result) {
				KafkaMessageUtils.updateInfMessage(infMessage, "COMPLETED", null);
				//log.info("Sent message =" + message.toString());
				//log.info("offset = " + result.getRecordMetadata().offset());
			}

			@Override
			public void onFailure(Throwable ex) {
				KafkaMessageUtils.updateInfMessage(infMessage, "REQ_ERROR", new Exception(ex));
				log.error("Unable to send message= " + infMessage.toString());
				log.error("Exception = " + ex.getMessage());
			}
		});
	}

}
