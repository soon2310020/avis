package com.emoldino.api.analysis.resource.composite.cdtisp.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspGetPageIn;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspItem;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspItem.TrsViwCdata;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspItem.TrsViwStatistics;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspItem.TrsViwTransfer;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspPostIn;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;

import lombok.extern.slf4j.Slf4j;
import saleson.api.statistics.StatisticsRepository;
import saleson.model.QLogTransfer;
import saleson.model.QStatistics;
import saleson.model.QTransfer;
import saleson.service.transfer.CdataRepository;
import saleson.service.transfer.LogTransferRepository;
import saleson.service.transfer.TransferController;
import saleson.service.transfer.TransferRepository;

@Transactional
@Service
@Slf4j
public class CdtIspService {

	@Transactional(propagation = Propagation.NEVER)
	public void post(CdtIspPostIn input) {
		String result = BeanUtils.get(TransferController.class).post(input.getContent());
		log.info(result);
	}

	public Page<CdtIspItem> getPage(CdtIspGetPageIn input, Pageable pageable) {
		if (ObjectUtils.isEmpty(input.getZoneId())) {
			input.setZoneId(Zone.SYS);
		}

		Page<CdtIspItem> page;

		/**
		 * Log Transfer
		 */
		{
			QLogTransfer table = QLogTransfer.logTransfer;
			BooleanBuilder filter = new BooleanBuilder()//
					.and(table.at.eq("CDATA"));
			QueryUtils.eq(filter, table.id, input.getId());
			QueryUtils.eq(filter, table.ci, input.getCi());
			QueryUtils.startsWith(filter, table.ci, input.getCiStartsWith());
			QueryUtils.eq(filter, table.ti, input.getTi());
			QueryUtils.startsWith(filter, table.ti, input.getTiStartsWith());
			BooleanExpression exp = null;
//			if (input.isCheck1970()) {
//				exp = or(exp, table.ds.like("%&time=%/1970%").or(table.ds.contains("&lst=1970")).or(table.ds.contains("&time=1970")));
//			}
//			if (input.isCheck2036()) {
//				exp = or(exp, table.ds.like("%&time=%/2036%").or(table.ds.contains("&lst=2036")).or(table.ds.contains("&time=2036")));
//			}
//			if (!ObjectUtils.isEmpty(input.getRtStartsWith())) {
//				exp = or(exp, table.ds.like("%&time=%/" + input.getRtStartsWith() + "%"));
//			}
//			if (!ObjectUtils.isEmpty(input.getLstStartsWith())) {
//				exp = or(exp, table.ds.like("%&lst=" + input.getLstStartsWith() + "%"));
//			}
			if (!ObjectUtils.isEmpty(input.getTimeStartsWith())) {
				exp = or(exp, table.ds.like("%&time=" + input.getTimeStartsWith() + "%"));
			}
			if (exp != null) {
				filter.and(exp);
			}

			pageable = QueryUtils.applySortDefault(pageable, Direction.DESC, "id");

			page = BeanUtils.get(LogTransferRepository.class).findAll(filter, pageable).map(from -> {
				CdtIspItem to = CdtIspItem.builder()//
						.id(from.getId())//
						.createdAtStr(DateUtils2.format(from.getCreatedAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, input.getZoneId()))//
						.ci(from.getCi())//
						.ti(from.getTi())//
						.tv(from.getTv())//
						.ds(from.getDs())//
						.build();
				setSn(to);
				setTransfer(to, input.getZoneId());
				return to;
			});
		}
		return page;
	}

	private BooleanExpression or(BooleanExpression exp1, BooleanExpression exp2) {
		if (exp1 == null) {
			return exp2;
		} else if (exp2 == null) {
			return exp1;
		} else {
			return exp1.or(exp2);
		}
	}

	private void setSn(CdtIspItem item) {
		String ds = item.getDs();
		if (ObjectUtils.isEmpty(ds) || !ds.contains("&sn=")) {
			return;
		}
		try {
			int sn = Integer.parseInt(ds.substring(ds.lastIndexOf("&sn=") + 4));
			item.setSn(sn);
		} catch (Exception e) {
			//SKIP
		}
	}

	private void setTransfer(CdtIspItem item, String zoneId) {
		QTransfer table = QTransfer.transfer;
		BeanUtils.get(TransferRepository.class).findAll(new BooleanBuilder()//
				.and(table.at.eq("CDATA"))//
				.and(table.ci.eq(item.getCi()))//
				.and(table.sn.eq(item.getSn()))//
		).forEach(from -> {
			TrsViwTransfer to = TrsViwTransfer.builder()//
					.id(from.getId())//
					.createdAtStr(DateUtils2.format(from.getCreatedAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId))//
					.cdata(getCdata(from.getId(), zoneId))//
					.build();
			item.addTransfer(to);
		});
	}

	private TrsViwCdata getCdata(Long id, String zoneId) {
		TrsViwCdata data = BeanUtils.get(CdataRepository.class).findById(id)//
				.map(from -> {
					TrsViwCdata to = TrsViwCdata.builder()//
							.rt(from.getRt())//
							.lst(from.getLst())//
							.tff(from.getTff())//
							.moldId(from.getMoldId())//
							.sc(from.getSc())//
							.build();
					setStatistics(id, to, zoneId);
					return to;
				}).orElse(null);
		return data;
	}

//	private void setCdata(CdtIspItem item, String zoneId) {
//		if (ObjectUtils.isEmpty(item.getCi()) || item.getSn() <= 0) {
//			return;
//		}
//		QCdata table = QCdata.cdata;
//		BeanUtils.get(CdataRepository.class).findAll(new BooleanBuilder()//
//				.and(table.ci.eq(item.getCi()))//
//				.and(table.sn.eq(item.getSn()))//
//		).forEach(from -> {
//			TrsViwCdata to = TrsViwCdata.builder()//
//					.id(from.getId())//
//					.createdAtStr(DateUtils2.format(from.getCreatedAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId))//
//					.rt(from.getRt())//
//					.lst(from.getLst())//
//					.tff(from.getTff())//
//					.moldId(from.getMoldId())//
//					.sc(from.getSc())//
//					.build();
//			setStatistics(to, zoneId);
//		});
//	}

	private void setStatistics(Long cdataId, TrsViwCdata cdata, String zoneId) {
		if (ObjectUtils.isEmpty(cdataId)) {
			return;
		}
		QStatistics table = QStatistics.statistics;
		BeanUtils.get(StatisticsRepository.class).findAll(new BooleanBuilder()//
				.and(table.cdataId.eq(cdataId))//
		).forEach(from -> {
			TrsViwStatistics to = TrsViwStatistics.builder()//
					.id(from.getId())//
					.createdAtStr(DateUtils2.format(from.getCreatedAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, zoneId))//
					.lst(from.getLst())//
					.fsc(from.getFsc())//
					.shotCount(from.getShotCount())//
					.build();
			cdata.addStatistics(to);
		});
	}
}
