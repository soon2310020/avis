//package unused.resource.ppachart.service;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.util.ObjectUtils;
//
//import com.emoldino.api.integration.resource.base.repository.pq.AiPqResult;
//import com.emoldino.api.integration.resource.base.repository.pq.AiPqResultRepository;
//import com.emoldino.api.integration.resource.base.repository.pq.QAiPqResult;
//import com.emoldino.api.integration.resource.composite.pqwrk.dto.CtStatus;
//import com.emoldino.api.integration.resource.composite.pqwrk.dto.PpaStatus;
//import com.emoldino.framework.util.BeanUtils;
//import com.emoldino.framework.util.TranUtils;
//import com.querydsl.core.BooleanBuilder;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//
//import lombok.extern.slf4j.Slf4j;
//import unused.resource.ppachart.dto.ChartDataOut;
//import unused.resource.ppachart.dto.ChartDto;
//import unused.resource.ppachart.dto.LabelContent;
//import unused.resource.ppachart.dto.PpaChartData;
//import unused.resource.ppachart.dto.PpaChartIn;
//import unused.resource.ppachart.dto.PpaChartList;
//import unused.resource.ppachart.dto.QdChartData;
//import unused.resource.ppachart.dto.QdChartList;
//import unused.resource.ppachart.repository.PpaChartRepostory;
//
//@Service
//@Slf4j
//public class PpaChartService {
//
//	@Autowired
//	private JPAQueryFactory queryFactory;
//
//	final static List<LabelContent> LABEL_CONTENTS_PPA = new ArrayList<>();
//	final static List<LabelContent> LABEL_CONTENTS_QD = new ArrayList<>();
//	final static List<String> dd = new ArrayList<>();
//	static {
//		LABEL_CONTENTS_PPA.add(LabelContent.builder().displayName("Processing Time").key("processingTime").color("#7B3AAD").build());
//		LABEL_CONTENTS_PPA.add(LabelContent.builder().displayName("Warmup Time").key("warmupTime").color("#C25FD3").build());
//		LABEL_CONTENTS_PPA.add(LabelContent.builder().displayName("Cool Down Time").key("cooldownTime").color("#C0ADF2").build());
//
//		LABEL_CONTENTS_QD.add(LabelContent.builder().displayName("Good Shots").key("goodShots").color("#4EBCD5").build());
//		LABEL_CONTENTS_QD.add(LabelContent.builder().displayName("Bad Shots").key("badShots").color("#BCE2C7").build());
//
//		for (int i = 0; i < 90; i++) {
//			dd.add("STABLE");
//		}
//		for (int i = 0; i < 7; i++) {
//			dd.add("WARM_UP");
//		}
//		for (int i = 0; i < 3; i++) {
//			dd.add("COOL_DOWN");
//		}
//	}
//
//	public ChartDataOut get(PpaChartIn chartIn) {
//
//		List<ChartDto> ppaData = BeanUtils.get(PpaChartRepostory.class).getPpaData(chartIn);
//		List<ChartDto> qdData = BeanUtils.get(PpaChartRepostory.class).getQdData(chartIn);
//
//		Map<String, PpaChartData> map = new HashMap<>();
//		Map<String, QdChartData> qdMap = new HashMap<>();
//		if (!ObjectUtils.isEmpty(chartIn.getIndicatorNames())) {
//			for (String indicator : chartIn.getIndicatorNames()) {
//				map.put(indicator, PpaChartData.builder().category(indicator).build());
//				qdMap.put(indicator, QdChartData.builder().category(indicator).build());
//			}
//		}
//
//		//Parse from return
//		for (ChartDto dto : ppaData) {
//			if (ObjectUtils.isEmpty(dto.getPpaStatus()))
//				continue;
//
//			PpaChartData build = map.getOrDefault(dto.getIndicator(), PpaChartData.builder().category(dto.getIndicator()).build());
//			if (dto.getPpaStatus().equals(PpaStatus.WARM_UP.getValue())) {
//				build.setWarmupTime(dto.getCount());
//			} else if (dto.getPpaStatus().equals(PpaStatus.COOL_DOWN.getValue())) {
//				build.setCooldownTime(dto.getCount());
//			} else if (dto.getPpaStatus().equals(PpaStatus.NO_PRODUCTION.getValue())) {
//				//build.setProgressingTime(null);
//			} else if (dto.getPpaStatus().equals(PpaStatus.STABLE.getValue())) {
//				build.setProcessingTime(dto.getCount());
//			}
//
//			if (build.getCategory() == null && map.size() == 1) {
//				build.setCategory("All");
//			}
//
//			map.put(dto.getIndicator(), build);
//		}
//
//		for (ChartDto dto : qdData) {
//			QdChartData build = qdMap.getOrDefault(dto.getIndicator(), QdChartData.builder().category(dto.getIndicator()).build());
//
//			if (build.getCategory() == null && map.size() == 1) {
//				build.setCategory("All");
//			}
//
//			double goodCountRate = dto.getGoodCount().doubleValue() / (dto.getGoodCount().doubleValue() + dto.getBadCount().doubleValue()) * 100;
//			double badCountRate = dto.getBadCount().doubleValue() / (dto.getGoodCount().doubleValue() + dto.getBadCount().doubleValue()) * 100;
//
//			if (Double.isNaN(goodCountRate))
//				goodCountRate = 0.0;
//			if (Double.isNaN(badCountRate))
//				badCountRate = 0.0;
//
//			build.setGoodShots(Double.parseDouble(String.format("%.1f", goodCountRate)));
//			build.setBadShots(Double.parseDouble(String.format("%.1f", badCountRate)));
//
//			qdMap.put(dto.getIndicator(), build);
//		}
//
//		List<PpaChartData> ppaDataList = new ArrayList<>();
//		ppaDataList.addAll(map.values());
//
//		List<QdChartData> qdDataList = new ArrayList<>();
//		qdDataList.addAll(qdMap.values());
//
//		PpaChartList ppaChartList = new PpaChartList();
//		ppaChartList.setCategoryContent(ppaDataList);
//		ppaChartList.setLabelContent(LABEL_CONTENTS_PPA);
//
//		QdChartList qdChartList = new QdChartList();
//		qdChartList.setCategoryContent(qdDataList);
//		qdChartList.setLabelContent(LABEL_CONTENTS_QD);
//
//		ChartDataOut output = new ChartDataOut();
//		output.setPpa(ppaChartList);
//		output.setQd(qdChartList);
//
//		log.info("label end");
//
//		return output;
//	}
//
//	public void testRandomData() {
//		BooleanBuilder b = new BooleanBuilder();
//		b.and(QAiPqResult.aiPqResult.ctStatus.eq(""));
//
//		for (int i = 0; i < 2000; i++) {
//			TranUtils.doNewTran(() -> {
//				List<Long> ids = queryFactory.select(QAiPqResult.aiPqResult.id).from(QAiPqResult.aiPqResult).where(QAiPqResult.aiPqResult.qdStatus.eq(""))
//						.orderBy(QAiPqResult.aiPqResult.id.asc()).limit(100).fetch();
//				if (!ObjectUtils.isEmpty(ids)) {
//					queryFactory.update(QAiPqResult.aiPqResult).where(QAiPqResult.aiPqResult.id.in(ids)).set(QAiPqResult.aiPqResult.qdStatus, "TEMP1").execute();
//				}
//
//				List<AiPqResult> findAll = queryFactory.select(QAiPqResult.aiPqResult).from(QAiPqResult.aiPqResult).where(QAiPqResult.aiPqResult.ctStatus.eq("TEMP1"))
//						.orderBy(QAiPqResult.aiPqResult.id.asc()).limit(100).fetch();
//
//				Random ran = new Random(System.currentTimeMillis());
//				for (AiPqResult s : findAll) {
//					s.setCtStatus(CtStatus.values()[ran.nextInt(4)].getValue());
//					s.setPpaStatus(dd.get(ran.nextInt(99)));
//					BeanUtils.get(AiPqResultRepository.class).saveAndFlush(s);
//				}
//			});
//
//		}
//	}
//
//}