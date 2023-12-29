package com.emoldino.api.asset.resource.base.counter.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.TranUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.counter.CounterRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.common.enumeration.EquipmentStatus;
import saleson.model.Counter;
import saleson.model.QCdata;
import saleson.model.QCounter;
import saleson.model.QTransfer;
import saleson.model.Terminal;
import saleson.service.transfer.CdataRepository;
import saleson.service.transfer.TransferRepository;

@Service("counterService2")
public class CounterService {

	@Async
	@Transactional(propagation = Propagation.NEVER)
	public void populateBatch() {
		JobUtils.runIfNotRunning("CounterService.lastTerminalId", new JobOptions().setClustered(true), () -> {
			QCounter table = QCounter.counter;
			TranUtils.doNewTran(() -> BeanUtils.get(CounterRepository.class)//
					.findAll(new BooleanBuilder()//
							.and(table.equipmentCode.isNotNull())//
							.and(table.lastTerminalId.isNull())//
							.and(table.lastShotAt.isNotNull()), //
							PageRequest.of(0, 100, Direction.ASC, "id")))//
					.forEach(counter -> TranUtils.doNewTran(() -> saveLastTerminalId(counter)));
		});

		JobUtils.runIfNotRunning("CounterService.installedAt", new JobOptions().setClustered(true), () -> {
			QCounter table = QCounter.counter;
			TranUtils.doNewTran(() -> BeanUtils.get(CounterRepository.class)//
					.findAll(new BooleanBuilder()//
							.and(table.equipmentCode.isNotNull())//
							.and(table.equipmentStatus.eq(EquipmentStatus.INSTALLED))//
							.and(table.mold.isNotNull())//
							.and(table.installedAt.isNull().or(table.installedAt.isEmpty())), //
							PageRequest.of(0, 100, Direction.ASC, "id")))//
					.forEach(counter -> TranUtils.doNewTran(() -> saveInstalledAt(counter)));
		});
	}

	private void saveLastTerminalId(Counter counter) {
		QTransfer table = QTransfer.transfer;
		BeanUtils.get(TransferRepository.class)//
				.findAll(new BooleanBuilder()//
						.and(table.ci.eq(counter.getEquipmentCode()))//
						.and(table.ti.isNotNull()), //
						PageRequest.of(0, 1, Direction.DESC, "id"))//
				.forEach(transfer -> {
					Terminal terminal = BeanUtils.get(TerminalRepository.class).findByEquipmentCode(transfer.getTi()).orElse(null);
					if (terminal == null) {
						return;
					}
					counter.setLastTerminalId(terminal.getId());
					BeanUtils.get(CounterRepository.class).save(counter);
				});
	}

	private void saveInstalledAt(Counter counter) {
		if (!ObjectUtils.isEmpty(counter.getInstalledAt()) || counter.getMold() == null) {
			return;
		}
		QCdata table = QCdata.cdata;
		BeanUtils.get(CdataRepository.class)//
				.findAll(new BooleanBuilder()//
						.and(table.ci.eq(counter.getEquipmentCode()))//
						.and(table.moldId.eq(counter.getMold().getId())) //
						.and(table.tff.isNotNull())//
						.and(table.tff.isNotEmpty()), //
						PageRequest.of(0, 1, Direction.ASC, "id"))//
				.forEach(cdata -> {
					String installedAt = DateUtils2.toOtherPattern(cdata.getTff(), DatePattern.yyyyMMddHHmmss, DatePattern.yyyy_MM_dd);
					counter.setInstalledAt(installedAt);
					BeanUtils.get(CounterRepository.class).save(counter);
				});
	}

}
