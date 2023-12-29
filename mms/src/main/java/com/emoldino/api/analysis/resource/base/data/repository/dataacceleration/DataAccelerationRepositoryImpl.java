package com.emoldino.api.analysis.resource.base.data.repository.dataacceleration;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.LogicUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;

import saleson.model.QCounter;
import saleson.model.QMold;

public class DataAccelerationRepositoryImpl extends QuerydslRepositorySupport implements DataAccelerationRepositoryCustom {
	public DataAccelerationRepositoryImpl() {
		super(DataAcceleration.class);
	}

	@Override
	public List<DataAcceleration> findAllByMoldIdAndMeasurementTime(Long moldId, String fromDateStr, String toDateStr) {
		LogicUtils.assertNotNull(moldId, "moldId is required!!");
		LogicUtils.assertNotEmpty(fromDateStr, "fromDateStr is required!!");
		LogicUtils.assertNotEmpty(toDateStr, "toDateStr is required!!");

//		String zoneId = MoldUtils.getZoneIdByMoldId(moldId);

		Instant fromDate = DateUtils2.toInstant(fromDateStr, DatePattern.yyyyMMddHHmmss, Zone.SYS);
		Instant toDate = DateUtils2.toInstant(toDateStr, DatePattern.yyyyMMddHHmmss, Zone.SYS);

		long period = toDate.getEpochSecond() - fromDate.getEpochSecond();
		if (period < 0) {
			return Collections.emptyList();
		} else if (period > 3600) {
			LogicUtils.newOverPeriodException("AccelerationChart", "1 Hour", fromDateStr, toDateStr);
		}

		String _fromDateStr = DateUtils2.toOtherZone(fromDateStr, DatePattern.yyyyMMddHHmmss, Zone.SYS, Zone.GMT);
		String _toDateStr = DateUtils2.toOtherZone(toDateStr, DatePattern.yyyyMMddHHmmss, Zone.SYS, Zone.GMT);

		QDataAcceleration table = QDataAcceleration.dataAcceleration;
		QMold mld = QMold.mold;
		QCounter cnt = QCounter.counter;

		JPQLQuery<DataAcceleration> query = from(table);
		query.innerJoin(cnt).on(cnt.equipmentCode.eq(table.counterId));
		query.innerJoin(mld).on(mld.counterId.eq(cnt.id));
		query.where(new BooleanBuilder()//
				.and(mld.id.eq(moldId))//
				.and(table.measurementDate.goe(_fromDateStr))//
				.and(table.measurementDate.lt(_toDateStr))//
		);
		query.orderBy(table.measurementDate.asc());

//		if (mockEnabled && query.fetchCount() == 0) {
//			Mold mold = BeanUtils.get(MoldRepository.class).findById(moldId).orElse(null);
//			if (mold != null && mold.getCounter() != null) {
//				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault());
//				int count = 1;
//				do {
//					Instant time = fromDate.plus(5L * count++, ChronoUnit.MINUTES);
//					String timeStr = formatter.format(time);
//					if (timeStr.compareTo(toDateStr) >= 0) {
//						break;
//					}
//					DataAcceleration data = new DataAcceleration();
//					data.setDataId(0L);
//					data.setReadTime(null);
//					data.setCounterId(mold.getCounterCode());
//					data.setMeasurementDate(timeStr);
//					data.setRawdataCreatedAt(time);
//					data.setCreatedAt(Instant.now());
//					Acceleration acc;
//					acc = newAcceleration(0, 1.9, 0.01, 0.03);
//					data.addAcceleration(acc);
//					acc = newAcceleration(10d, 15d, Math.max(ValueUtils.toDouble(acc.getValue(), 0d), 0.04d), 0.06);
//					data.addAcceleration(acc);
//					acc = newAcceleration(30d, 40d, Math.max(ValueUtils.toDouble(acc.getValue(), 0d), 0.05d), 0.07);
//					data.addAcceleration(acc);
//					acc = newAcceleration(70d, 73d, 0.005, 0.007);
//					data.addAcceleration(acc);
//					BeanUtils.get(DataAccelerationRepository.class).save(data);
//				} while (true);
//			}
//		}

		Pageable pageable = PageRequest.of(0, 300);
		getQuerydsl().applyPagination(pageable, query);

		return query.fetch();
	}

	private static Acceleration newAcceleration(double min1, double max1, double min2, double max2) {
		Acceleration acc = new Acceleration();
		String time = Math.random() * (max1 - min1) + min1 + "";
		if (time.length() > 4) {
			time = time.substring(0, 4);
		}
		String value = Math.random() * (max2 - min2) + min2 + "";
		if (value.length() > 5) {
			value = value.substring(0, 5);
		}
		acc.setTime(time);
		acc.setValue(value);
		return acc;
	}
}
