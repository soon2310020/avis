package com.emoldino.api.analysis.resource.composite.cdtisp.service.statistics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspGetPageIn;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspHourly;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspStatistics;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.statistics.StatisticsRepository;
import saleson.model.QCdata;
import saleson.model.QStatistics;
import saleson.model.QTransfer;
import saleson.model.Statistics;
import saleson.model.Transfer;
import saleson.service.transfer.CdataRepository;
import saleson.service.transfer.TransferRepository;

@Service
public class CdtIspStatisticsService {
	public ListOut<CdtIspHourly> getPage(CdtIspGetPageIn input, Pageable pageable) {
		if (ObjectUtils.isEmpty(input.getZoneId())) {
			input.setZoneId(Zone.SYS);
		}

		Map<String, CdtIspHourly> list = new LinkedHashMap<>();

		/**
		 * Log Transfer
		 */
		{
			QCdata table = QCdata.cdata;
			BooleanBuilder filter = new BooleanBuilder();
//			QueryUtils.eq(filter, table.id, input.getId());
			QueryUtils.eq(filter, table.ci, input.getCi());
			QueryUtils.startsWith(filter, table.ci, input.getCiStartsWith());
			QueryUtils.eq(filter, table.ti, input.getTi());
			QueryUtils.startsWith(filter, table.ti, input.getTiStartsWith());
			QueryUtils.eq(filter, table.moldCode, input.getMoldCode());
			QueryUtils.startsWith(filter, table.tff, input.getTimeStartsWith());
			if (!ObjectUtils.isEmpty(input.getTimeFrom())) {
				filter.and(table.tff.goe(input.getTimeFrom()));
			}
			if (!ObjectUtils.isEmpty(input.getTimeTo())) {
				filter.and(table.tff.loe(input.getTimeTo()));
			}

			pageable = QueryUtils.applySortDefault(pageable, Direction.ASC, "id");

			BeanUtils.get(CdataRepository.class).findAll(filter, pageable).forEach(from -> {
				String hour = ValueUtils.abbreviate(from.getTff(), 10);
				String key = from.getCi() + "," + from.getTi() + "," + from.getMoldCode() + hour;
				CdtIspHourly data;
				if (list.containsKey(key)) {
					data = list.get(key);
				} else {
					data = new CdtIspHourly();
					data.setCi(from.getCi());
					data.setTi(from.getTi());
					data.setMoldCode(from.getMoldCode());
					data.setHour(hour);
					list.put(key, data);
				}
				Page<Statistics> stats;
				stats = BeanUtils.get(StatisticsRepository.class).findAll(//
						new BooleanBuilder()//
								.and(QStatistics.statistics.cdataId.eq(from.getId())), //
						PageRequest.of(0, 1));
				Statistics stat = stats.isEmpty() ? null : stats.getContent().get(0);

				Page<Transfer> trans;
				trans = BeanUtils.get(TransferRepository.class).findAll(//
						new BooleanBuilder()//
								.and(QTransfer.transfer.ci.eq(from.getCi())) //
								.and(QTransfer.transfer.sn.eq(from.getSn())), //
						PageRequest.of(0, 1));
				Transfer tran = trans.isEmpty() ? null : trans.getContent().get(0);

				CdtIspStatistics item = new CdtIspStatistics();
				item.setTff(from.getTff());
				item.setRt(from.getRt());
				item.setSc(from.getSc());
				item.setShotCount(stat == null ? null : stat.getShotCount());
				item.setShotCountVal(stat == null ? null : stat.getShotCountVal());
				item.setCt(from.getCt());
				item.setCtVal(from.getCtVal());
				item.setCtt(from.getCtt());
				item.setCttVal(from.getCttVal());

				item.setTransferId(tran == null ? null : tran.getId());
				item.setTransferCount(trans.getTotalElements());
				item.setCdataId(from.getId());
				item.setStatisticsId(stat == null ? null : stat.getId());
				item.setStatisticsCreatedAt(stat == null ? null : DateUtils2.format(stat.getCreatedAt(), DatePattern.yyyyMMddHHmmss, input.getZoneId()));
				item.setStatisticsCount(stats.getTotalElements());
//				data.addItem(item);

				if (item.getTff() != null && (data.getTffFrom() == null || data.getTffFrom().compareTo(item.getTff()) > 0)) {
					data.setTffFrom(item.getTff());
				}
				if (item.getTff() != null && (data.getTffTo() == null || data.getTffTo().compareTo(item.getTff()) < 0)) {
					data.setTffTo(item.getTff());
				}
				if (item.getRt() != null && (data.getRtFrom() == null || data.getRtFrom().compareTo(item.getRt()) > 0)) {
					data.setRtFrom(item.getRt());
				}
				if (item.getRt() != null && (data.getRtTo() == null || data.getRtTo().compareTo(item.getRt()) < 0)) {
					data.setRtTo(item.getRt());
				}

				if (item.getSc() != null) {
					data.setSc(Math.max(item.getSc(), data.getSc()));
				}
				if (item.getShotCount() != null) {
					data.setShotCount(data.getShotCount() + item.getShotCount());
				}
				if (item.getShotCountVal() != null) {
					data.setShotCountVal(data.getShotCountVal() + item.getShotCountVal());
				}

				data.setTransferCount(data.getTransferCount() + item.getTransferCount());
				data.setStatisticsCount(data.getStatisticsCount() + item.getStatisticsCount());
			});
		}
		return new ListOut<>(new ArrayList<>(list.values()));
	}
}
