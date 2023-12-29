package com.emoldino.api.analysis.resource.composite.trscol.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.transfermessage.QTransferMessage;
import com.emoldino.api.analysis.resource.base.data.repository.transfermessage.TransferMessage;
import com.emoldino.api.analysis.resource.base.data.repository.transfermessage.TransferMessage.TransferData;
import com.emoldino.api.analysis.resource.base.data.repository.transfermessage.TransferMessageRepository;
import com.emoldino.api.analysis.resource.base.data.repository.transferresult.TransferResult;
import com.emoldino.api.analysis.resource.base.data.repository.transferresult.TransferResultRepository;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.exception.SysException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.service.transfer.TransferController;
import saleson.service.transfer.TransferPostIn;

@Service
@Transactional
public class TrsColService {

	public TransferMessage saveMessage(String at, String ci, TransferData data) {
		TransferMessage message = new TransferMessage(at, ci, ValueUtils.toJsonStr(data));
		message.setElapsedTimeMillis(ThreadUtils.getExecutionTime().getElapsedTimeMillis());
		BeanUtils.get(TransferMessageRepository.class).save(message);
		return message;
	}

	public void procMessage(Long id) {
		LogicUtils.assertNotNull(id, "id");

		TransferMessage result = TranUtils.doNewTran(() -> BeanUtils.get(TransferMessageRepository.class)//
				.findById(id)//
				.orElseThrow(() -> new LogicException("TRS_MESSAGE_NOT_FOUND", "TRS Message not found:" + id, new Property("id", id))));

		TransferPostIn input = ValueUtils.fromJsonStr(result.getData(), TransferPostIn.class);
		input.setBatch(true);
		String msg = BeanUtils.get(TransferController.class).post(input);
		if (!ObjectUtils.isEmpty(msg) && msg.contains("ERROR_")) {
			throw new SysException("TRS_FAIL", msg);
		}
	}

	public void changeMessageStatusQuietly(Long id, String status) {
		changeMessageStatusQuietly(id, status, null);
	}

	public void changeMessageStatusQuietly(Long id, String status, Throwable t) {
		if (id == null) {
			return;
		}

		Long errorId = t == null ? null : LogUtils.saveErrorQuietly(t);
		try {
			TranUtils.doNewTran(() -> {
				TransferMessage result = BeanUtils.get(TransferMessageRepository.class).findById(id).orElse(null);
				if (result == null) {
					result = new TransferMessage();
					result.setId(id);
				}
				result.setProcStatus(status);
				if (errorId != null) {
					result.setProcErrorId(errorId);
				}
				result.setElapsedTimeMillis(ThreadUtils.procGapTime().getGapTimeMillis());
				BeanUtils.get(TransferMessageRepository.class).save(result);
			});
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(ErrorType.SYS, "TRS_MESSAGE_STATUS_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, "Transfer Message Status Fail: " + id, t);
		}
	}

	public void cleanOldMessageBatch() {
		QTransferMessage table = QTransferMessage.transferMessage;
		TranUtils.doTran(() -> BeanUtils.get(JPAQueryFactory.class)//
				.delete(table)//
				.where(new BooleanBuilder().and(table.procStatus.eq("O"))//
						.and(table.updatedAt.lt(Instant.now().minus(Duration.ofDays(1)))))//
				.execute());
	}

	public void changeStatusQuietly(Long id, String status) {
		changeStatusQuietly(id, status, null);
	}

	public void changeStatusQuietly(Long id, String status, Throwable t) {
		if (id == null) {
			return;
		}

		Long errorId = t == null ? null : LogUtils.saveErrorQuietly(t);
		try {
			TranUtils.doNewTran(() -> {
				TransferResult result = BeanUtils.get(TransferResultRepository.class).findById(id).orElse(null);
				if (result == null) {
					result = new TransferResult();
					result.setId(id);
				}
				result.setProcStatus(status);
				result.setProcErrorId(errorId);
				result.setElapsedTimeMillis(ThreadUtils.procGapTime().getGapTimeMillis());
				BeanUtils.get(TransferResultRepository.class).save(result);
			});
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(ErrorType.SYS, "TRS_STATUS_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, "Transfer Status Fail: " + id, t);
		}
	}

}
