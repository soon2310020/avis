package com.emoldino.api.integration.resource.composite.msghdr.service;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.integration.kafka.message.KafkaMessage;
import com.emoldino.api.integration.resource.composite.mfewrk.dto.AiMfeResultIn;
import com.emoldino.api.integration.resource.composite.mfewrk.service.AiMfeWrkService;
import com.emoldino.api.integration.resource.composite.pcwrk.dto.AiPcResultIn;
import com.emoldino.api.integration.resource.composite.pcwrk.service.AiPcWrkService;
import com.emoldino.api.integration.resource.composite.pqwrk.dto.AiPqResultIn;
import com.emoldino.api.integration.resource.composite.pqwrk.service.AiPqWrkService;
import com.emoldino.api.integration.resource.composite.tsdwrk.dto.AiTsdResultIn;
import com.emoldino.api.integration.resource.composite.tsdwrk.service.AiTsdWrkService;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.ValueUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiMessageHandler {
	private final AiPcWrkService pcWorkService;
	private final AiPqWrkService pqWorkService;
	private final AiMfeWrkService mfeWorkService;
	private final AiTsdWrkService tsdWorkService;

	private static final String MSG_PQ_RESULT = "ai2mms_pq_result";
	private static final String MSG_TSD_RESULT = "ai2mms_tsd_result";
	private static final String MSG_MFE_RESULT = "ai2mms_mfe_result";
	private static final String MSG_PC_RESULT = "ai2mms_pc_result";

	public void receiveMessage(KafkaMessage msg) {
        if (ObjectUtils.isEmpty(msg.getContent())) {
            throw new BizException("KAFKA_MESSAGE_DATA_DOES_NOT_FOUND",
                    "Kafka Message Data does not found", new Property("message", msg.getMessage()));
        } else {
            switch (msg.getMessage()) {
                case MSG_PQ_RESULT:
                    pqWorkService.savePqResult(ValueUtils.fromMap(msg.getContent(), AiPqResultIn.class));
                    break;
                case MSG_TSD_RESULT:
                    tsdWorkService.saveTsdResult(ValueUtils.fromMap(msg.getContent(), AiTsdResultIn.class));
                    break;
                case MSG_MFE_RESULT:
                    mfeWorkService.saveMfeResult(ValueUtils.fromMap(msg.getContent(), AiMfeResultIn.class));
                    break;
                case MSG_PC_RESULT:
                    pcWorkService.savePcResult(ValueUtils.fromMap(msg.getContent(), AiPcResultIn.class));
                    break;
                default:
                    log.warn("Received an unknown message type: {}", msg.getMessage());
            }
        }
	}

}
