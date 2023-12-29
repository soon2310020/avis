package com.emoldino.api.analysis.resource.composite.datcol.service.dist;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.QDeviceCommand;
import com.emoldino.api.analysis.resource.base.data.repository.data.Data;
import com.emoldino.api.analysis.resource.base.data.repository.data.DataRepository;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3Collected;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3CollectedRepository;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.QData3Collected;
import com.emoldino.api.analysis.resource.composite.datcol.util.DatColUtils;
import com.emoldino.api.asset.resource.base.terminal.util.TerminalUtils;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobCall;
import com.emoldino.framework.util.JobUtils.JobCallParam;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.counter.CounterRepository;
import saleson.api.mold.MoldRepository;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.service.data.service.DataMapper;
import saleson.service.data.service.DataService;

@Service
public class DatColDistService {

	@Transactional(propagation = Propagation.NEVER)
	public void dist() {
		JobUtils.runIfNotRunning("Data3.dist", new JobOptions().setClustered(true), () -> {
			DataUtils.runBatch(//
					Data3CollectedRepository.class, //
					Qs.data3Collected.procStatus.in(Arrays.asList("CREATED", "RECREATED", "START", "REFINE_READY", "REFINE_WAIT")), //
					Sort.by("id"), 100, false, //
					item -> dist(item)//
			);
		});
	}

	private static final String LOREAL = "lf0408";
	private static final String PNG = "p&g";

	public void forward() {
		if (!ConfigUtils.isProdMode()) {
			return;
		}
		String serverName = ConfigUtils.getServerName();
		if (!LOREAL.equals(serverName) && !PNG.equals(serverName)) {
			return;
		}
		List<String> deviceIds = SENSOR_BY_SERVER.get(LOREAL.equals(serverName) ? PNG : LOREAL);
		if (ObjectUtils.isEmpty(deviceIds)) {
			return;
		}
		JobUtils.runIfNotRunning("Data3.forward", new JobOptions().setClustered(true), () -> {
			DataUtils.runBatch(//
					Data3CollectedRepository.class, //
					new BooleanBuilder()//
							.and(Qs.data3Collected.dataType.eq("DATA"))//
							.and(Qs.data3Collected.deviceId.in(deviceIds))//
							.and(Qs.data3Collected.procStatus.notIn(Arrays.asList("CREATE_ERROR", "FORWARDED", "FORWARD_SKIPPED"))), //
					Sort.by("id"), 100, false, //
					item -> {
						String targetUrl = getForwardServerUrl(item.getDeviceId());
						forward(item, targetUrl);
					}//
			);
		});
	}

	private void forward(Data3Collected item, String targetUrl) {
		try {
			DatColUtils.forward(item, targetUrl);
			TranUtils.doNewTran(() -> changeStatus(item, "FORWARDED", 0));
		} catch (Throwable t) {
			TranUtils.doNewTran(() -> changeStatus(item, "FORWARD_ERROR", 0, t));
		}
	}

	private static final Map<String, String> SERVER_BY_SENSOR = new MapBuilder<String, String>()//
			// LOREAL
			.put("EMA2252M10401", LOREAL)//
			.put("EMA2252M10402", LOREAL)//
			.put("EMA2252M10403", LOREAL)//
			// P&G
//			.put("EMA2233M10287", PNG)//
			.build();
	private static final MultiValueMap<String, String> SENSOR_BY_SERVER;
	static {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		SERVER_BY_SENSOR.forEach((sersorCode, serverName) -> map.add(serverName, sersorCode));
		SENSOR_BY_SERVER = map;
	}

	private static final Map<String, String> CLIENT_URL = new MapBuilder<String, String>()//
			// LOREAL
			.put(LOREAL, "https://loreal.emoldino.com")//
			// P&G
			.put(PNG, "https://pg.emoldino.com")//
			.build();

	private static String getForwardServerUrl(String deviceId) {
		if (!SERVER_BY_SENSOR.containsKey(deviceId)) {
			return null;
		}
		String serverName = SERVER_BY_SENSOR.get(deviceId);
		if (serverName.equals(ConfigUtils.getServerName())) {
			return null;
		}
		return CLIENT_URL.get(serverName);
	}

	private void dist(Data3Collected item) {
		String deviceId = item.getDeviceId();

		// Forward
		String forwardUrl = getForwardServerUrl(deviceId);
		if (!ObjectUtils.isEmpty(forwardUrl)) {
			if (ConfigUtils.isProdMode()) {
				forward(item, forwardUrl);
			} else {
				TranUtils.doNewTran(() -> changeStatus(item, "FORWARD_SKIPPED", 0));
			}
			return;
		}

		Counter sensor = TranUtils.doNewTran(() -> ObjectUtils.isEmpty(deviceId) ? null : //
				BeanUtils.get(CounterRepository.class).findByEquipmentCode(deviceId).orElse(null));

		// Sensor Disabled
		if (sensor != null && !sensor.isEnabled()) {
			changeStatus(item, "SENSOR_DISABLED", 0);
			return;
		}

		// Check Data Duplication
		{
			Integer sn = item.getSn();
			if (sn != null && !ObjectUtils.isEmpty(deviceId)) {
				QData3Collected table = QData3Collected.data3Collected;
				Page<Data3Collected> sameSnPage = TranUtils.doNewTran(() -> BeanUtils.get(Data3CollectedRepository.class).findAll(//
						new BooleanBuilder()//
								.and(table.deviceId.eq(deviceId))//
								.and(table.sn.eq(sn))//
								.and(table.id.lt(item.getId())), //
						PageRequest.of(0, 5, Direction.DESC, "id")//
				));
				for (Data3Collected oldData : sameSnPage.getContent()) {
					if (ValueUtils.equals(item.getData(), oldData.getData())) {
						TranUtils.doNewTran(() -> changeStatus(item, "DUP", 0));
						return;
					}

					// TO-DO: Open
					// 2023.07.31 Mickey Park
					// Adjust RawData's shotCount Case 1: Less than 3.0.1-RELEASE
					/* Adjust RawData's shotCount Case 2: 3.0.1-RELEASE or later 
					   For 3.0.1-RELEASE and later versions, it will be reset to the previous day's SN, ShotCount
					   LastShotCount = shotCount of the latest data + the shotCount of the current data. */
//					else if ((sn == 0 && ValueUtils.toVersionNo(item.getDeviceSwVersion()) < 3000199L) //
//							|| ValueUtils.toVersionNo(item.getDeviceSwVersion()) >= 3000199L) {
//
//						QData3Collected d3Table = QData3Collected.data3Collected;
//						String prevData = BeanUtils.get(JPAQueryFactory.class)//
//								.select(d3Table.data) //
//								.from(d3Table) //
//								.where(d3Table.deviceId.eq(deviceId) //
//										.and(d3Table.occurredAt.lt(item.getOccurredAt()))) //
//								.orderBy(d3Table.occurredAt.desc()) //
//								.fetchFirst();
//
//						if (prevData != null) {
//							Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(item.getDeviceId()).orElse(null);
//							Mold mold = BeanUtils.get(MoldRepository.class).findByCounterId(counter.getId());
//							if (mold == null) {
//								return;
//							}
//							int maxShotCount = DatColUtils.getMaxTffAndMaxShotCount(deviceId, mold.getEquipmentCode()).getSecond();
//							int adjustShotCount = maxShotCount + DatColUtils.getTotalShotCount(item.getData());
//							item.setData(DatColUtils.modifyRawDataAtIndex(item.getData(), 4, String.valueOf(adjustShotCount)));
//							TranUtils.doNewTran(() -> {
//								BeanUtils.get(Data3CollectedRepository.class).saveAndFlush(item);
//							});
//
//							// Reset
//							DatColUtils.generateResetCommand(deviceId, adjustShotCount);
//						}
//						break;
//					}
				}
			}
		}

		// Pre-process
		boolean process = false;
		if ("DATA".equals(item.getDataType())) {
			process = TranUtils.doNewTran(() -> {
				if (ObjectUtils.isEmpty(item.getData())) {
					changeStatus(item, "EMPTY", 0);
					return false;
				} else {
					changeStatus(item, "START", 0);
					return true;
				}
			});
		} else if ("HEARTBEAT".equals(item.getDataType())) {
			String ip = ValueUtils.abbreviate(item.getData(), 50);
			try {
				TranUtils.doNewTran(() -> TerminalUtils.setTerminalOperated(deviceId, ip));
				TranUtils.doNewTran(() -> changeStatus(item, "DONE", 0));
			} catch (Exception e) {
				AbstractException ae = LogUtils.saveErrorQuietly(ErrorType.SYS, "TERMINAL_STATUS_FAIL", null, "Failed to save terminal status, ti:" + deviceId + ", ip:" + ip, e);
				TranUtils.doNewTran(() -> {
					if (ae != null) {
						item.setProcErrorId(ae.getId());
					}
					changeStatus(item, "HEARTBEAT_ERROR", 0);
				});
			}
		} else {
			TranUtils.doNewTran(() -> changeStatus(item, "UNKNOWN", 0));
		}

		// If all processed
		if (!process) {
			return;
		}

		// Data Refine
		try {
			Map<String, String> map = DatColUtils.separates(item.getData());
			boolean empty = true;
			for (String value : map.values()) {
				if (!ObjectUtils.isEmpty(value)) {
					empty = false;
					break;
				}
			}
			if (empty) {
				TranUtils.doNewTranQuietly(() -> {
					if (ObjectUtils.isEmpty(deviceId) || sensor == null) {
						return;
					}

					Mold mold = sensor.getMold();
					if (mold != null) {
						mold = BeanUtils.get(MoldRepository.class).findWithPessimisticLockById(mold.getId()).orElse(null);
					}

					Instant rt = item.getOccurredAt();

					if (sensor.getOperatedAt() == null) {
						sensor.setOperatingStatus(OperatingStatus.WORKING);
					}
					sensor.setOperatedAt(ValueUtils.max(rt, sensor.getOperatedAt()));
					if (mold != null) {
						sensor.setEquipmentStatus(EquipmentStatus.INSTALLED);
					} else {
						sensor.setEquipmentStatus(EquipmentStatus.AVAILABLE);
					}
					BeanUtils.get(CounterRepository.class).save(sensor);

					if (mold != null) {
						mold.setOperatedAt(ValueUtils.max(rt, mold.getOperatedAt()));
						mold.setEquipmentStatus(EquipmentStatus.INSTALLED);
						BeanUtils.get(MoldRepository.class).save(mold);
					}
				});
				TranUtils.doNewTran(() -> changeStatus(item, "EMPTY", 0));
				return;
			}

			// Data Validation - Cdata
			if (!map.get(DataMapper.CDATA).isEmpty()) {
				boolean validData = DatColUtils.isValidCdata(map.get(DataMapper.CDATA), item.getId());

				if (!validData) {
					TranUtils.doNewTran(() -> changeStatus(item, "REFINE_ERROR", 0));
					return;
				}
			}

			// TO-DO Data Validation - Adata

			Data data;
			{
				String rawData;
				if (ObjectUtils.isEmpty(deviceId)) {
					rawData = item.getData();
				} else {
					for (String key : map.keySet()) {
						String value = map.get(key);

						// Put Device ID into RawData
						{
							if (ObjectUtils.isEmpty(value) || value.contains(deviceId) || value.length() < key.length() + 2) {
								continue;
							}
							value = value.substring(0, key.length() + 1) + deviceId + value.substring(key.length());
							map.put(key, value);
						}
					}
					rawData = DatColUtils.toRawdata(map);
				}
				data = TranUtils.doNewTran(() -> {
					Data _data;
					if (item.getDataId() != null) {
						_data = BeanUtils.get(DataRepository.class).findById(item.getDataId()).orElse(null);
						if (_data == null) {
							_data = new Data();
						}
					} else {
						_data = new Data();
					}
					_data.setTerminalId(item.getBrokerId());
					_data.setReadTime(DateUtils2.format(item.getOccurredAt(), DatePattern.yyyy_MM_dd_HH_mm_ss_SSS, Zone.GMT));
					_data.setRawData(rawData);
					BeanUtils.get(DataRepository.class).save(_data);
					item.setDataId(_data.getId());
					changeStatus(item, "REFINE_READY", 0);
					return _data;
				});
			}

			enqueue(item, data);

		} catch (Throwable t) {
			TranUtils.doNewTran(() -> changeStatus(item, "REFINE_ERROR", 0, t));
		}
	}

	private void enqueue(Data3Collected item, Data data) {
		TranUtils.doNewTran(() -> {
			JobCall call = new JobCall();
			call.setLogicName(ReflectionUtils.toName(DataService.class, "refine"));
			call.getParams().add(new JobCallParam("id", Long.class, data.getId()));

			JobCall beforeCall = new JobCall();
			beforeCall.setLogicName(ReflectionUtils.toName(DatColDistService.class, "changeStatus"));
			beforeCall.getParams().add(new JobCallParam("id", Long.class, item.getId()));
			beforeCall.getParams().add(new JobCallParam("status", String.class, "REFINING"));
			beforeCall.getParams().add(new JobCallParam("step", Integer.class, 0));

			JobCall afterCall = new JobCall();
			afterCall.setLogicName(ReflectionUtils.toName(DatColDistService.class, "changeStatus"));
			afterCall.getParams().add(new JobCallParam("id", Long.class, item.getId()));
			afterCall.getParams().add(new JobCallParam("status", String.class, "REFINED"));
			afterCall.getParams().add(new JobCallParam("step", Integer.class, 0));

			JobCall throwsCall = new JobCall();
			throwsCall.setLogicName(ReflectionUtils.toName(DatColDistService.class, "changeStatus"));
			throwsCall.getParams().add(new JobCallParam("id", Long.class, item.getId()));
			throwsCall.getParams().add(new JobCallParam("status", String.class, "REFINE_ERROR"));
			throwsCall.getParams().add(new JobCallParam("step", Integer.class, 0));
			throwsCall.getParams().add(new JobCallParam("t", Throwable.class, null));

			JobUtils.enqueue("dataTaskExecutor", item.getDeviceId(), false, call, beforeCall, afterCall, throwsCall);

			changeStatus(item, "DISTRIBUTED", 0);
		});
	}

	public void changeStatus(Long id, String status, Integer step, Throwable t) {
		TranUtils.doNewTran(() -> {
			Data3Collected data3 = BeanUtils.get(Data3CollectedRepository.class).findById(id).orElse(null);
			changeStatus(data3, status, step, t);
		});
	}

	public void changeStatus(Data3Collected data3, String status, Integer step, Throwable t) {
		Long errorId = LogUtils.saveErrorQuietly(t);
		if (data3 == null) {
			return;
		}
		TranUtils.doNewTran(() -> {
			data3.setProcErrorId(errorId);
			changeStatus(data3, status, 0);
		});
	}

	public void changeStatus(Long id, String status, Integer step) {
		Data3Collected data3 = TranUtils.doNewTran(() -> BeanUtils.get(Data3CollectedRepository.class).findById(id).orElse(null));
		changeStatus(data3, status, step);
	}

	public void changeStatus(Data3Collected data3, String status, Integer step) {
		if (data3 == null) {
			return;
		}
		data3.setProcStatus(status);
		data3.setProcStep(step);
		data3.setUpdatedAt(DateUtils2.newInstant());
		if ("DISTRIBUTED".equals(status) || "FORWARDED".equals(status)) {
			data3.setDistributedAt(data3.getUpdatedAt());
		} else if ("REFINED".equals(status)) {
			data3.setAnalyzedAt(data3.getUpdatedAt());
		}
		BeanUtils.get(Data3CollectedRepository.class).save(data3);
	}
}
