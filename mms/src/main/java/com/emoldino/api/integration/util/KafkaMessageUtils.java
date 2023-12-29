package com.emoldino.api.integration.util;

import java.util.Map;
import java.util.UUID;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.api.integration.kafka.message.KafkaMessage;
import com.emoldino.api.integration.kafka.producer.KafkaMessageProducer;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.SysException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.model.integration.InfMessage;
import saleson.model.integration.InfMessageRepository;
import saleson.model.integration.QInfMessage;

public class KafkaMessageUtils {

	@SuppressWarnings("unchecked")
	public static void sendMessage(String messageName, Object obj, String topic, String returnTopic) {
		try {
			Map<String, Object> content = ValueUtils.toRequiredType(obj, Map.class);
			KafkaMessage message = KafkaMessage //
					.builder() //
					.message(messageName)//
					.topic(topic)//
					.returnTopic(returnTopic)//
					.content(content).build();

			BeanUtils.get(KafkaMessageProducer.class).sendMessage(topic, message);
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(e);
		}
	}

	public static InfMessage createInfMessage(String direction, KafkaMessage kafkaMessage, String targetInfo, String procStatus, Exception e) {
		String inputStr = ValueUtils.toJsonStr(kafkaMessage);

		InfMessage infMessage = new InfMessage();
		infMessage.setRequestId(UUID.randomUUID().toString());
		infMessage.setInfDirection(direction); // I(INBOUND), O(OUTBOUND)	
		infMessage.setReqChannelName(kafkaMessage.getTopic());
		infMessage.setReqMessageClassName(kafkaMessage.getClass().getName());
		infMessage.setReqTargetInfo(targetInfo);
		infMessage.setReqMessage(inputStr);

		if (e != null) {
			if ("I".equals(direction)) {
				procStatus = "RET_ERROR";
			} else if ("O".equals(direction)) {
				procStatus = "REQ_ERROR";
			}
			AbstractException ae = e instanceof AbstractException ? (AbstractException) e : new SysException(procStatus, e);
			Long errorId = LogUtils.saveErrorQuietly(ae);
			infMessage.setProcErrorId(errorId);
		}
		infMessage.setProcStatus(procStatus);

		TranUtils.doNewTran(() -> BeanUtils.get(InfMessageRepository.class).save(infMessage));
		return infMessage;
	}

	public static void updateInfMessage(InfMessage infMessage, String procStatus, Exception e) {
		infMessage.setProcStatus(procStatus);
		if (e != null) {
			AbstractException ae = e instanceof AbstractException ? (AbstractException) e : new SysException("REQ_ERROR", e);
			Long errorId = LogUtils.saveErrorQuietly(ae);
			infMessage.setProcErrorId(errorId);
		}
		TranUtils.doNewTran(() -> BeanUtils.get(InfMessageRepository.class).save(infMessage));
	}

	public static InfMessage getInProgress(String requestId) {
		if (ObjectUtils.isEmpty(requestId)) {
			return null;
		}
		InfMessage req = BeanUtils.get(InfMessageRepository.class).findOne(getFilterInProgress(requestId)).orElse(null);
		return req;
	}

	private static BooleanBuilder getFilterInProgress(String requestId) {
		QInfMessage table = QInfMessage.infMessage;
		return new BooleanBuilder().and(table.requestId.eq(requestId)).and(table.procStatus.in("REQUESTED", "REQ_ERROR", "INVOKATION_ERROR", "RET_ERROR"));
	}
}
