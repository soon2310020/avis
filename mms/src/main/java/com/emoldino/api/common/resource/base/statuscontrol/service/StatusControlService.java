package com.emoldino.api.common.resource.base.statuscontrol.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.option.dto.OPConfigItem;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.DateTimePath;

import saleson.api.counter.CounterRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.common.enumeration.EquipmentType;
import saleson.common.enumeration.OperatingStatus;

@Service
public class StatusControlService {

	@Transactional(propagation = Propagation.NEVER)
	public void updateBatch() {

		Map<EquipmentType, Map<OperatingStatus, OPConfigItem>> configs = TranUtils
				.doNewTran(() -> OptionUtils.getContent("OP_CONFIG", new TypeReference<Map<EquipmentType, Map<OperatingStatus, OPConfigItem>>>() {
				}));
		Map<OperatingStatus, OPConfigItem> moldCofig = configs.containsKey(EquipmentType.MOLD) ? configs.get(EquipmentType.MOLD) : Collections.emptyMap();
		Map<OperatingStatus, OPConfigItem> terminalConfig = configs.containsKey(EquipmentType.TERMINAL) ? configs.get(EquipmentType.TERMINAL) : Collections.emptyMap();

		Instant activeTime = getInstant(moldCofig.get(OperatingStatus.WORKING), Duration.ofDays(3));
		Instant idleTime = getInstant(moldCofig.get(OperatingStatus.IDLE), Duration.ofDays(10));
		Instant disconTime = getInstant(moldCofig.get(OperatingStatus.DISCONNECTED), Duration.ofHours(6));
		Instant disconTimeTerminal = getInstant(terminalConfig.get(OperatingStatus.DISCONNECTED), Duration.ofHours(1));

		DateTimePath<Instant> moldLastShotAt = Q.mold.lastShotAt;
		DateTimePath<Instant> counterLastShotAt = Q.counter.lastShotAt;
//		boolean dataFilterEnabled = OptionUtils.isEnabled(ConfigCategory.DATA_FILTER);
//		DateTimePath<Instant> moldLastShotAt = dataFilterEnabled ? Q.mold.lastShotAtVal : Q.mold.lastShotAt;
//		DateTimePath<Instant> counterLastShotAt = dataFilterEnabled ? Q.counter.lastShotAtVal : Q.counter.lastShotAt;

		/**
		 * Terminal Operating Status
		 */
		// TODO Not Working -> Disconnected
		updateTerminalsBatch(new BooleanBuilder()//
				.and(Q.terminal.operatingStatus.isNull().or(Q.terminal.operatingStatus.eq(OperatingStatus.WORKING)))//
				.and(Q.terminal.operatedAt.isNotNull())//
				.and(Q.terminal.operatedAt.lt(disconTimeTerminal)), //
				OperatingStatus.NOT_WORKING);

		/**
		 * Mold & Counter Operating Status
		 */

		// 1. ACTIVE(WORKING)
		updateCountersBatch(new BooleanBuilder()//
				.and(Q.counter.operatingStatus.isNull().or(Q.counter.operatingStatus.ne(OperatingStatus.WORKING)))//
				.and(Q.counter.operatedAt.isNotNull())//
				.and(Q.counter.operatedAt.gt(disconTime))//
				.and(counterLastShotAt.goe(activeTime)), //
				OperatingStatus.WORKING);
		updateMoldsBatch(new BooleanBuilder()//
				.and(Q.mold.deleted.isNull().or(Q.mold.deleted.isFalse()))//
				.and(Q.mold.operatingStatus.isNull().or(Q.mold.operatingStatus.ne(OperatingStatus.WORKING))//
						.or(Q.mold.toolingStatus.isNull()))//
				.and(Q.mold.operatedAt.isNotNull())//
				.and(Q.mold.operatedAt.gt(disconTime))//
				.and(moldLastShotAt.goe(activeTime)), //
				OperatingStatus.WORKING);

		// 2. IDLE
		updateCountersBatch(new BooleanBuilder()//
				.and(Q.counter.operatingStatus.isNull().or(Q.counter.operatingStatus.ne(OperatingStatus.IDLE)))//
				.and(Q.counter.operatedAt.isNotNull())//
				.and(Q.counter.operatedAt.gt(disconTime))//
				.and(counterLastShotAt.goe(idleTime))//
				.and(counterLastShotAt.lt(activeTime)), //
				OperatingStatus.IDLE);
		updateMoldsBatch(new BooleanBuilder()//
				.and(Q.mold.deleted.isNull().or(Q.mold.deleted.isFalse()))//
				.and(Q.mold.operatingStatus.isNull().or(Q.mold.operatingStatus.ne(OperatingStatus.IDLE))//
						.or(Q.mold.toolingStatus.isNull()))//
				.and(Q.mold.operatedAt.isNotNull())//
				.and(Q.mold.operatedAt.gt(disconTime))//
				.and(moldLastShotAt.goe(idleTime))//
				.and(moldLastShotAt.lt(activeTime)), //
				OperatingStatus.IDLE);

		// 3. INACTIVE(NOT_WORKING)
		updateCountersBatch(new BooleanBuilder()//
				.and(Q.counter.operatingStatus.isNull().or(Q.counter.operatingStatus.ne(OperatingStatus.NOT_WORKING)))//
				.and(Q.counter.operatedAt.isNotNull())//
				.and(Q.counter.operatedAt.gt(disconTime))//
				.and(counterLastShotAt.isNull().or(counterLastShotAt.lt(idleTime))), //
				OperatingStatus.NOT_WORKING);
		updateMoldsBatch(new BooleanBuilder()//
				.and(Q.mold.deleted.isNull().or(Q.mold.deleted.isFalse()))//
				.and(Q.mold.operatingStatus.isNull().or(Q.mold.operatingStatus.ne(OperatingStatus.NOT_WORKING))//
						.or(Q.mold.toolingStatus.isNull()))//
				.and(Q.mold.operatedAt.isNotNull())//
				.and(Q.mold.operatedAt.gt(disconTime))//
				.and(moldLastShotAt.isNull().or(moldLastShotAt.lt(idleTime))), //
				OperatingStatus.NOT_WORKING);

		// 4. DISCONNECTED
		updateCountersBatch(new BooleanBuilder()//
				.and(Q.counter.operatingStatus.isNull().or(Q.counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))//
						.or(Q.mold.toolingStatus.isNull()))//
				.and(Q.counter.operatedAt.isNull().or(Q.counter.operatedAt.loe(disconTime))), //
				OperatingStatus.DISCONNECTED);
		updateMoldsBatch(new BooleanBuilder()//
				.and(Q.mold.deleted.isNull().or(Q.mold.deleted.isFalse()))//
				.and(Q.mold.operatingStatus.isNull().or(Q.mold.operatingStatus.ne(OperatingStatus.DISCONNECTED)))//
				.and(Q.mold.operatedAt.isNull().or(Q.mold.operatedAt.loe(disconTime))), //
				OperatingStatus.DISCONNECTED);

	}

	private static Instant getInstant(OPConfigItem config, Duration defaultDuration) {
		Duration duration;
		if (config == null || config.getTime() == null) {
			duration = defaultDuration;
		} else if (ChronoUnit.DAYS.equals(config.getTimeUnit())) {
			duration = Duration.ofDays(ValueUtils.toLong(config.getTime(), 0L));
		} else {
			duration = Duration.ofHours(ValueUtils.toLong(config.getTime(), 0L));
		}
		return duration == null ? null : DateUtils2.getInstant().minus(duration);
	}

	private void updateTerminalsBatch(Predicate filter, OperatingStatus status) {
		DataUtils.runBatch(TerminalRepository.class, //
				filter, //
				Sort.by("id"), 100, false, //
				terminal -> {
					TranUtils.doNewTranQuietly(() -> {
						terminal.setOperatingStatus(status);
						BeanUtils.get(TerminalRepository.class).save(terminal);
					});
				});
	}

	private void updateCountersBatch(Predicate filter, OperatingStatus status) {
		DataUtils.runBatch(CounterRepository.class, //
				filter, //
				Sort.by("id"), 100, false, //
				counter -> {
					TranUtils.doNewTranQuietly(() -> {
						counter.setOperatingStatus(status);
						BeanUtils.get(CounterRepository.class).save(counter);
					});
				});
	}

	private void updateMoldsBatch(Predicate filter, OperatingStatus status) {
		DataUtils.runBatch(MoldRepository.class, //
				filter, //
				Sort.by("id"), 100, false, //
				mold -> {
					TranUtils.doNewTranQuietly(() -> {
						BeanUtils.get(MoldRepository.class).findWithPessimisticLockById(mold.getId());
						mold.setOperatingStatus(status);
						BeanUtils.get(MoldRepository.class).save(mold);
					});
				});
	}

}
