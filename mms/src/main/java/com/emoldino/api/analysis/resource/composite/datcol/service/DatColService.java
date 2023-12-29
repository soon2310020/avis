package com.emoldino.api.analysis.resource.composite.datcol.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.DeviceCommand;
import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.DeviceCommandRepository;
import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.QDeviceCommand;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3Collected;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.QData3Collected;
import com.emoldino.api.analysis.resource.base.data.repository.statisticsext.QStatisticsExt;
import com.emoldino.api.analysis.resource.base.data.repository.statisticsext.StatisticsExt;
import com.emoldino.api.analysis.resource.base.data.repository.statisticsext.StatisticsExtRepository;
import com.emoldino.api.analysis.resource.base.data.service.data3.Data3Service;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColCommand;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColCommandGetIn;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColCommandGetOut;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColPostIn;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColPostOut;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColPostShotCountCttIn;
import com.emoldino.api.analysis.resource.composite.datcol.util.DatColUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.api.counter.CounterRepository;
import saleson.common.enumeration.PresetStatus;
import saleson.model.Cdata;
import saleson.model.Counter;
import saleson.model.QStatistics;
import saleson.service.transfer.CdataRepository;

@Service
public class DatColService {

	@Autowired
	private JPAQueryFactory queryFactory;

	@Transactional
	public DatColPostOut post(DatColPostIn input) {
		List<Data3Collected> list = new ArrayList<>();

		int[] i = { 0 };

		String requestId;
		if (!ObjectUtils.isEmpty(input.getRequestId())) {
			requestId = input.getRequestId();
		} else {
			requestId = DatColUtils.newRequestId();
		}

		Instant now = DateUtils2.newInstant();

		// Broker Data
		if (!ObjectUtils.isEmpty(input.getDataType()) || !ObjectUtils.isEmpty(input.getData())) {
			Data3Collected data = new Data3Collected();
			data.setRequestId(requestId);
			data.setPosition(++i[0]);
			data.setOccurredAt(DateUtils2.toInstant(input.getBrokerTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT, now));
			data.setSentAt(DateUtils2.toInstant(input.getBrokerTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT, now));
			data.setDataType(input.getDataType());
			data.setData(input.getData());
			data.setDeviceId(input.getBrokerId());
			data.setDeviceType(input.getBrokerType());
			data.setDeviceSwVersion(input.getBrokerSwVersion());
			data.setBrokerId(input.getBrokerId());
			data.setBrokerType(input.getBrokerType());
			data.setBrokerSwVersion(input.getBrokerSwVersion());
			list.add(data);
		}

		// Device Data
		if (!ObjectUtils.isEmpty(input.getContent())) {
			Map<String, Integer> releaseCommands = new LinkedHashMap<>();

			input.getContent().forEach(item -> {
				Integer sn;
				String procStatus = null;
				try {
					sn = getSn(item.getData());
				} catch (Exception e) {
					sn = null;
					procStatus = "CREATE_ERROR";
				}

				if ("ABNORMAL_DATA".equals(item.getDataType())) {
					procStatus = "CREATE_ERROR";
				}

				String deviceId = item.getDeviceId();

				Data3Collected data = new Data3Collected();
				data.setRequestId(requestId);
				data.setProcStatus(procStatus);
				data.setPosition(++i[0]);
				data.setOccurredAt(DateUtils2.toInstant(item.getDeviceTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT, now));
				data.setSentAt(DateUtils2.toInstant(input.getBrokerTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT, now));
				data.setDataType(item.getDataType());
				data.setData(item.getData());
				data.setSn(sn);
				data.setDeviceId(deviceId);
				data.setDeviceType(item.getDeviceType());
				data.setDeviceSwVersion(item.getDeviceSwVersion());
				data.setBrokerId(input.getBrokerId());
				data.setBrokerType(input.getBrokerType());
				data.setBrokerSwVersion(input.getBrokerSwVersion());

				// Release Command
				if (item.getLastCommandIndexNo() > 0) {
					releaseCommands.put(item.getDeviceId(), item.getLastCommandIndexNo());
				}

				list.add(data);
			});

			releaseCommands.forEach((deviceId, indexNo) -> releaseCommands(deviceId, indexNo));
		}

		BeanUtils.get(Data3Service.class).postList(list);

		DatColPostOut output = new DatColPostOut();
		if ("HEARTBEAT".equals(input.getDataType())) {
			DatColCommandGetIn regin = new DatColCommandGetIn(input.getBrokerId());
			DatColCommandGetOut reqout = getCommands(regin);
			output.setCommands(reqout.getCommands());
		}
		return output;
	}

	private static Integer getSn(String data) {
		if (ObjectUtils.isEmpty(data)) {
			return null;
		}
		String str = data.trim();

		if (!str.startsWith("SN=")) {
			return null;
		}

		if (str.contains("/")) {
			str = str.substring(0, str.indexOf("/")).trim();
		}
		if (str.length() == 3) {
			return null;
		}
		str = str.substring(3);
		return ValueUtils.toInteger(str, null);
	}

	@Transactional
	public DatColCommandGetOut getCommands(DatColCommandGetIn input) {
		Instant since = DateUtils2.getInstant().minus(Duration.ofDays(10L));

		QDeviceCommand table = QDeviceCommand.deviceCommand;
		BooleanBuilder filter = new BooleanBuilder()//
				.and(table.status.eq("CREATED"))//
				.and(table.createdAt.gt(since));
		if (!ObjectUtils.isEmpty(input.getBrokerId())) {
			QData3Collected subtable = QData3Collected.data3Collected;
			filter.and(table.deviceId.in(//
					JPAExpressions.select(subtable.deviceId).distinct()//
							.from(subtable)//
							.where(//
									subtable.brokerId.eq(input.getBrokerId())//
											.and(subtable.occurredAt.after(since))//
							)//
			));
		}

		Map<String, List<DatColCommand>> commands = new LinkedHashMap<>();
		BeanUtils.get(DeviceCommandRepository.class).findAll(filter, Sort.by("deviceId", "createdAt")).forEach(item -> {
			List<DatColCommand> l;
			if (commands.containsKey(item.getDeviceId())) {
				l = commands.get(item.getDeviceId());
			} else {
				l = new ArrayList<>();
				commands.put(item.getDeviceId(), l);
			}
			l.add(new DatColCommand(item.getCommand(), item.getIndexNo(), item.getData()));
		});
		DatColCommandGetOut output = new DatColCommandGetOut();
		output.setCommands(commands);
		return output;
	}

	@Transactional
	public void releaseCommands(String deviceId, int lastIndexNo) {
		if (lastIndexNo <= 0) {
			return;
		}

		LogicUtils.assertNotEmpty(deviceId, "deviceId");

		DeviceCommand command = BeanUtils.get(DeviceCommandRepository.class)//
				.findFirstByDeviceIdAndIndexNoAndStatusInOrderByIdAsc(deviceId, lastIndexNo, Arrays.asList("CREATED")).orElse(null);
		if (command == null) {
			return;
		}

		List<DeviceCommand> list = new ArrayList<DeviceCommand>();
		command.setStatus("RELEASED");
		list.add(command);
		QDeviceCommand table = QDeviceCommand.deviceCommand;
		BooleanBuilder filter = new BooleanBuilder();
		filter.and(table.deviceId.eq(deviceId));
		filter.and(table.id.lt(command.getId()));
		filter.and(table.status.eq("CREATED"));
		BeanUtils.get(DeviceCommandRepository.class).findAll(filter, Sort.by("id")).forEach(item -> {
			item.setStatus("RELEASED");
			list.add(item);
		});
		BeanUtils.get(DeviceCommandRepository.class).saveAll(list);

		Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(deviceId).orElse(null);
		if (counter != null) {
			counter.setPresetStatus(PresetStatus.APPLIED);
			BeanUtils.get(CounterRepository.class).save(counter);
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ShotCountCttItem {
		private Long id;
		private Long cdataId;
		private Double ct;
		private Instant createdAt;
		private Instant updatedAt;
		private Integer shotCountCtt;
		private Integer shotCountCttVal;
	}

	@Async
	@Transactional(propagation = Propagation.NEVER, timeout = 7200)
	public void postShotCountCtt(DatColPostShotCountCttIn input) {
		JobUtils.runIfNotRunning("datCol.postShotCountCtt", new JobOptions().setClustered(true), () -> {
			QStatistics table = QStatistics.statistics;
			QStatisticsExt tableExt = QStatisticsExt.statisticsExt;
			int counter = 0;
			List<ShotCountCttItem> page;
			BooleanBuilder filter = new BooleanBuilder();
			filter.and(table.cdataId.isNotNull());
			filter.and(table.createdAt.lt(DateUtils2.getInstant().minus(Duration.ofDays(1L))));
			filter.and(tableExt.shotCountCtt.isNull());

			JPAQuery<ShotCountCttItem> query = queryFactory//
					.select(Projections.constructor(//
							ShotCountCttItem.class, //
							table.id, //
							table.cdataId, //
							table.ct, //
							tableExt.createdAt, //
							tableExt.updatedAt, //
							tableExt.shotCountCtt, //
							tableExt.shotCountCttVal))//
					.from(table)//
					.leftJoin(tableExt).on(tableExt.id.eq(table.id))//
					.where(filter)//
					.orderBy(table.id.asc());

			while (counter++ < 100000 //
					&& !(page = TranUtils.doNewTran(() -> query.limit(100).fetch())).isEmpty()//
			) {
				List<ShotCountCttItem> _page = page;
				List<StatisticsExt> list = new ArrayList<>();
				try {
					TranUtils.doNewTran(() -> {
						_page.forEach(item -> {
							Cdata cdata = BeanUtils.get(CdataRepository.class).findById(item.getCdataId()).orElse(null);
							if (cdata == null) {
								return;
							}

							int i = 0;
							int incrShotCount = 0;
							if (!ObjectUtils.isEmpty(cdata.getCtt())) {
								for (String str : StringUtils.tokenizeToStringArray(cdata.getCtt(), "/")) {
									if (i++ % 2 != 1) {
										continue;
									}
									if (!NumberUtils.isCreatable(str)) {
										continue;
									}
									incrShotCount += ValueUtils.toInteger(str, 0);
								}
							}

							StatisticsExt data = BeanUtils.get(StatisticsExtRepository.class).findById(item.getId()).orElse(new StatisticsExt());
							data.setId(item.getId());
							data.setCdataId(item.getCdataId());
							data.setShotCountCtt(incrShotCount);
							if (item.getCt() == null || item.getCt() * incrShotCount > 72000) {
								data.setShotCountCttVal(0);
							} else {
								data.setShotCountCttVal(incrShotCount);
							}
							list.add(data);
						});
						BeanUtils.get(StatisticsExtRepository.class).saveAll(list);
					});
				} catch (Exception e) {
					continue;
				}
			}
		});
	}

}
