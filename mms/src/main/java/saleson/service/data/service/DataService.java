package saleson.service.data.service;

import static saleson.api.statistics.StatisticsService.getCurrentDefaultDate;
import static saleson.api.statistics.StatisticsService.getMoldStaticsDateFromShotTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.data.Data;
import com.emoldino.api.analysis.resource.base.data.repository.data.DataRepository;
import com.emoldino.api.analysis.resource.base.data.repository.data.QData;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAccelerationRepository;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounterRepository;
import com.emoldino.api.analysis.resource.composite.datcol.util.DatColUtils;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.TranUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import saleson.api.counter.CounterRepository;
import saleson.model.Counter;
import saleson.model.Transfer;
import saleson.model.data.MoldStatisticsDate;
import saleson.service.transfer.TransferService;

@Service
@RequiredArgsConstructor
public class DataService {
	private final DataRepository dataRepository;
	private final DataCounterRepository dataCounterRepository;
	private final DataAccelerationRepository dataAccelerationRepository;
	private final DataMapper dataMapper;
	private final TransferService transferService;

	@Transactional(propagation = Propagation.NEVER)
	public void refine() {
		JobUtils.runIfNotRunning("DataService.refine", new JobOptions().setClustered(true), () -> {
			List<Data> dataList;
			int counter = 0;
			while (counter++ < 100 && !(dataList = next()).isEmpty()) {
				dataList.stream().forEach(d -> refine(d));
			}
		});
	}

	public void refine(Long id) {
		Data data = TranUtils.doNewTran(() -> BeanUtils.get(DataRepository.class).findById(id).orElse(null));
		refine(data, null);
	}

	public void refine(Data data) {
		refine(data, null);
	}

	public void refine(Data data, Closure1ParamNoReturn<List<DataCounter>> transfer) {
		LogicUtils.assertNotEmpty(data, "data");

		try {
			// Parse HDATA/.../CDATA/.../ADATA/.../TDATA/.../TEST/...
			Map<String, String> map = DatColUtils.separates(data.getRawData());

			String[] ci = { null };

			List<DataCounter> dataCounters = new ArrayList<>();
			{
				DataCounter item = null;
				if (!map.get(DataMapper.CDATA).isEmpty()) {
					item = dataMapper.toDataCounter2(map.get(DataMapper.CDATA));
				} else if (!map.get(DataMapper.HDATA).isEmpty()) {
					item = dataMapper.toDataCounter2(map.get(DataMapper.HDATA));
				}
				if (item != null) {
					if (ci[0] == null) {
						ci[0] = item.getCounterId();
					}
					item.setDataId(data.getId());
					item.setTerminalId(data.getTerminalId());
					item.setReadTime(data.getReadTime());
					item.setRawdataCreatedAt(data.getUpdatedDate());
					dataCounters.add(item);
				}
			}

			List<DataAcceleration> dataAccelerations = new ArrayList<>();
			if (!map.get(DataMapper.ADATA).isEmpty()) {
				List<DataAcceleration> list = dataMapper.toDataAccelerationList(map.get(DataMapper.ADATA));
				list.forEach(item -> {
					if (ci[0] == null) {
						ci[0] = item.getCounterId();
					}

					Optional<Counter> counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(item.getCounterId());
					if (counter.isPresent()) {
						Long moldId = BeanUtils.get(JPAQueryFactory.class) //
								.select(Q.mold.id) //
								.from(Q.mold) //
								.where(Q.mold.counterId.eq(counter.get().getId())) //
								.fetchFirst();
						if (moldId != null) {
							item.setMoldId(moldId);
						}
					}

					item.setDataId(data.getId());
					item.setTerminalId(data.getTerminalId());
					item.setReadTime(data.getReadTime());
					item.setRawdataCreatedAt(data.getUpdatedDate());
					item.setProcStatus("CREATED");
					dataAccelerations.add(item);
				});
			}

			TranUtils.doNewTran(() -> {
				// Process Accelerations
				if (!ObjectUtils.isEmpty(dataAccelerations)) {
					List<DataAcceleration> sortedDataAccelerations = dataAccelerations.stream()//
							.sorted(Comparator.comparing(DataAcceleration::getMeasurementDate))//
							.collect(Collectors.toList());
					dataAccelerationRepository.saveAll(sortedDataAccelerations);
				}

				// Process DataCounters
				if (!ObjectUtils.isEmpty(dataCounters)) {
					List<DataCounter> list = dataCounters.stream()//
							.sorted(Comparator.comparing(DataCounter::getShotStartTime))//
							.collect(Collectors.toList());
					dataCounterRepository.saveAll(list);

					if (transfer != null) {
						transfer.execute(list);
					} else {
						List<Transfer> transfers = DatColUtils.toTransfers(list);
						transferService.saveLog(transfers);
						transferService.procCdata(transfers);
					}
				}

				data.setExecute(true);
				changeStatus(data, 0);
			});

		} catch (Exception e) {
			TranUtils.doNewTran(() -> changeStatus(data, -1));
			AbstractException ae = LogUtils.saveErrorQuietly(ErrorType.SYS, "DATA_REFINE_FAIL", null, "Data " + data.getId() + " Refine Failure", e);
			if (ae != null) {
				throw ae;
			}
			throw e;
		}
	}

	private void changeStatus(Data data, int status) {
		if (data == null) {
			return;
		}
		data.setDuplicateCount(status);
		data.setUpdatedDate(DateUtils2.newInstant());
		dataRepository.save(data);
	}

	private List<Data> next() {
		return TranUtils.doNewTran(() -> {
			QData table = QData.data;
			BooleanBuilder filter = new BooleanBuilder();
			filter.and(table.execute.eq(false));
			filter.and(table.duplicateCount.eq(0));
			List<Data> list = dataRepository.findAll(filter, PageRequest.of(0, 100, Direction.ASC, "id")).getContent();
			return list;
		});
	}

	public List<MoldStatisticsDate> getDataTimeRange(String counterCode) {
		List<MoldStatisticsDate> result = new ArrayList<>();
		if (counterCode != null) {
			DataAcceleration first = dataAccelerationRepository.findFirstByCounterIdOrderByMeasurementDateAsc(counterCode).orElse(null);
			if (first == null) {
				MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
//				moldStatisticsDate.setMoldId(counterCode);
				result.add(moldStatisticsDate);
			} else {
				String firstShotTime = first.getMeasurementDate();
				if (firstShotTime != null) {
					MoldStatisticsDate moldStatisticsDate = getMoldStaticsDateFromShotTime(firstShotTime);
//					moldStatisticsDate.setMoldId(counterCode);
					result.add(moldStatisticsDate);
				} else {
					MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
//					moldStatisticsDate.setMoldId(counterCode);
					result.add(moldStatisticsDate);
				}
			}

			DataAcceleration last = dataAccelerationRepository.findFirstByCounterIdOrderByMeasurementDateDesc(counterCode).orElse(null);
			if (last == null) {
				MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
//				moldStatisticsDate.setMoldId(counterCode);
				result.add(moldStatisticsDate);
			} else {
				String lastShotTime = last.getMeasurementDate();
				if (lastShotTime != null) {
					MoldStatisticsDate moldStatisticsDate = getMoldStaticsDateFromShotTime(lastShotTime);
//					moldStatisticsDate.setMoldId(counterCode);
					result.add(moldStatisticsDate);
				} else {
					MoldStatisticsDate moldStatisticsDate = getCurrentDefaultDate();
//					moldStatisticsDate.setMoldId(counterCode);
					result.add(moldStatisticsDate);
				}
			}

			return result;
		}
		return result;
	}

}
