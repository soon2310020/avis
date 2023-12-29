package com.emoldino.api.analysis.resource.base.data;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.base.data.service.DataService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class DataControllerImpl implements DataController {

	@Override
	public SuccessOut cleanBatch() {
		BeanUtils.get(DataService.class).cleanBatch();
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut rebuildBatch() {
		BeanUtils.get(DataService.class).rebuildBatch();
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut adjustBatch() {
		BeanUtils.get(DataService.class).adjustBatch();
		return SuccessOut.getDefault();
	}

	//	@Override
	//	@Transactional
	//	public SuccessOut adjustNewDevice() {
	//		Set<Integer> sn = new HashSet<>();
	//		{
	//			QTransfer table = QTransfer.transfer;
	//			BooleanBuilder builder = new BooleanBuilder();
	//			builder.and(table.at.eq("CDATA"));
	//			builder.and(table.ci.like("SC_%"));
	//			BeanUtils.get(TransferRepository.class).findAll(builder, Sort.by("id")).forEach(item -> {
	//				if (ObjectUtils.isEmpty(item.getCtt())) {
	//					return;
	//				}
	//
	//				boolean hasDot = false;
	//				for (String str : StringUtils.tokenizeToStringArray(item.getCtt(), "/")) {
	//					if (!str.contains(".")) {
	//						continue;
	//					}
	//					hasDot = true;
	//					break;
	//				}
	//				if (!hasDot) {
	//					return;
	//				}
	//
	//				item.setCt(adjustCt(item.getCt()));
	//				item.setLlct(adjustCt(item.getLlct()));
	//				item.setUlct(adjustCt(item.getUlct()));
	//				item.setCtt(adjustCtt(item.getCtt()));
	//
	//				sn.add(item.getSn());
	//			});
	//		}
	//
	//		Set<Long> cdatas = new HashSet<>();
	//		{
	//			QCdata table = QCdata.cdata;
	//			BooleanBuilder builder = new BooleanBuilder();
	//			builder.and(table.ci.like("SC_%"));
	//			BeanUtils.get(CdataRepository.class).findAll(builder, Sort.by("id")).forEach(item -> {
	//				if (!sn.contains(item.getSn())) {
	//					return;
	//				}
	//
	//				cdatas.add(item.getId());
	//
	//				item.setCt(adjustCt(item.getCt()));
	//				item.setLlct(adjustCt(item.getLlct()));
	//				item.setUlct(adjustCt(item.getUlct()));
	//				item.setCtt(adjustCtt(item.getCtt()));
	//
	//				item.setCtVal(adjustCt(item.getCtVal()));
	//				item.setLlctVal(adjustCt(item.getLlctVal()));
	//				item.setUlctVal(adjustCt(item.getUlctVal()));
	//				item.setCttVal(adjustCtt(item.getCttVal()));
	//			});
	//		}
	//
	//		{
	//			QStatistics table = QStatistics.statistics;
	//			BooleanBuilder builder = new BooleanBuilder();
	//			builder.and(table.ci.like("SC_%"));
	//			BeanUtils.get(StatisticsRepository.class).findAll(builder, Sort.by("id")).forEach(item -> {
	//				if (!cdatas.contains(item.getCdataId())) {
	//					return;
	//				}
	//
	//				item.setCt(adjustCt(item.getCt()));
	//				item.setLlct(adjustCt(item.getLlct()));
	//				item.setUlct(adjustCt(item.getUlct()));
	//
	//				item.setCtVal(adjustCt(item.getCtVal()));
	//				item.setLlctVal(adjustCt(item.getLlctVal()));
	//				item.setUlctVal(adjustCt(item.getUlctVal()));
	//			});
	//		}
	//
	//		return SuccessOut.getDefault();
	//	}
	//
	//	private static Double adjustCt(Double ct) {
	//		if (ct == null || ct.equals(0d)) {
	//			return ct;
	//		}
	//		return ct * 10;
	//		//		return (ct == null || ct.equals(0d)) ? ct : (ct * 10);
	//	}
	//
	//	private static String adjustCtt(String ctt) {
	//		if (ObjectUtils.isEmpty(ctt)) {
	//			return ctt;
	//		}
	//		StringBuilder buf = new StringBuilder();
	//		int i = 1;
	//		for (String str : StringUtils.tokenizeToStringArray(ctt, "/")) {
	//			buf.append(i == 1 ? "" : "/").append(i++ % 2 != 1 ? str : ValueUtils.toInteger(ValueUtils.toDouble(str, 0d) * 10, 0));
	//		}
	//		return buf.toString();
	//	}

}
