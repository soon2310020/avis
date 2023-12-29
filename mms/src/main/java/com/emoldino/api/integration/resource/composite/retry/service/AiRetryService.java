package com.emoldino.api.integration.resource.composite.retry.service;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.emoldino.api.integration.kafka.producer.KafkaMessageProducer;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.model.integration.InfMessage;
import saleson.model.integration.QInfMessage;

@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
@Service
public class AiRetryService {

	public void retryFailedKafkaMessage() {
		JobUtils.runIfNotRunning("AiRetryFailedMessages", new JobOptions().setClustered(true), () -> {
			procRetryFailedKafkaMessage();
		});
	}

	private void procRetryFailedKafkaMessage() {

		QInfMessage infMessageTbl = QInfMessage.infMessage;
		List<InfMessage> infMessages = BeanUtils.get(JPAQueryFactory.class) //
				.selectFrom(infMessageTbl) //
				.where(infMessageTbl.procStatus.eq("REQ_ERROR") //
						.and(infMessageTbl.infDirection.eq("O"))) //
				.fetch();

		if (infMessages == null) {
			return;
		}

		infMessages.forEach(message -> {
			BeanUtils.get(KafkaMessageProducer.class).retrySendMessage(message.getReqChannelName(), message);
		});

	}
}
