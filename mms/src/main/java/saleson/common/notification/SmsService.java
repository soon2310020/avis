package saleson.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import saleson.common.batch.BatchMessageRepository;
import saleson.common.enumeration.BatchStatus;
import saleson.common.enumeration.MessageType;
import saleson.model.BatchMessage;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SmsService {

	@Autowired
	SmsSender smsSender;

	@Autowired
	BatchMessageRepository batchMessageRepository;


	public void send(Message message) {
		BatchMessage batchMessage = BatchMessage.builder()
				.messageType(MessageType.SMS)
				.batchStatus(BatchStatus.PENDING)
				.content(message.getContent())
				.receivers(String.join(",", message.getReceivers()))
				.sender(message.getFrom())
			.build();


		batchMessageRepository.save(batchMessage);
	}

	/**
	 * [BATCH_JOB] SMS 발송 (배치 처리용)
	 */
	public void sendSms() {
		log.info("[BATCH_JOB_SMS] JOB START  ------------------------------------");

		List<BatchMessage> batches = batchMessageRepository.findAllByBatchStatusAndMessageType(BatchStatus.PENDING, MessageType.SMS);

		if (batches.size() > 0) {
			batches.stream().forEach(b -> {
				b.setBatchStatus(BatchStatus.IN_PROGRESS);
				b.setStartTime(LocalDateTime.now());
			});

			// InProgress 상태로 변경.
			List<BatchMessage> savedBatches = batchMessageRepository.saveAll(batches);
			batchMessageRepository.flush();

			for (BatchMessage savedBatch : savedBatches) {
				String[] receivers = StringUtils.delimitedListToStringArray(savedBatch.getReceivers(), ",");

				MessageResult result = new MessageResult();
				result.setTotal(receivers.length);


				log.info("[BATCH_JOB_SMS] BatchMessage ID #{} START  ------------------------------------", savedBatch.getId());
				for (String mobileNumber : receivers) {

					try {
						smsSender.send(mobileNumber);

						result.increaseSuccessCount();
					} catch (Exception e) {
						log.error("[BATCH_JOB_SMS] {} : {}", mobileNumber, e.getMessage());
						e.printStackTrace();
						result.increaseFailureCount();
					}
				}
				log.info("[BATCH_JOB_SMS] BatchMessage ID {} END : {} ---------------------------------------", savedBatch.getId(), result.toString());

				savedBatch.setEndTime(LocalDateTime.now());
				savedBatch.setBatchResult(result.toString());
				savedBatch.setBatchStatus(BatchStatus.COMPLETED);
				batchMessageRepository.saveAndFlush(savedBatch);
			}
		} else {
			log.info("[BATCH_JOB_SMS] Not sent. (BatchMessage size is 0)");
		}

		log.info("[BATCH_JOB_SMS] JOB END  ------------------------------------");
	}
}
