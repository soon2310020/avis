//package unused.resource.ppachart.repository;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Repository;
//import org.springframework.util.ObjectUtils;
//
//import com.emoldino.api.integration.resource.base.repository.pq.QAiPqResult;
//import com.emoldino.api.integration.resource.composite.pqwrk.dto.QdStatus;
//import com.querydsl.core.types.Expression;
//import com.querydsl.core.types.NullExpression;
//import com.querydsl.core.types.Order;
//import com.querydsl.core.types.OrderSpecifier;
//import com.querydsl.core.types.Predicate;
//import com.querydsl.core.types.Projections;
//import com.querydsl.core.types.QBean;
//import com.querydsl.core.types.dsl.BooleanExpression;
//import com.querydsl.core.types.dsl.CaseBuilder;
//import com.querydsl.core.types.dsl.Expressions;
//import com.querydsl.jpa.impl.JPAQuery;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//
//import saleson.model.QCategory;
//import saleson.model.QCompany;
//import saleson.model.QMold;
//import saleson.model.QPart;
//import unused.resource.ppachart.dto.ChartDto;
//import unused.resource.ppachart.dto.PpaChartIn;
//import unused.resource.ppachart.dto.PpaChartIndicator;
//
//@Repository
//public class PpaChartRepostory {
//
//	@Autowired
//	private JPAQueryFactory queryFactory;
//
//	public List<ChartDto> getPpaData(PpaChartIn chartIn) {
//		QAiPqResult qAqm = QAiPqResult.aiPqResult;
//		JPAQuery<ChartDto> query = queryFactory.select(projector(chartIn.getIndicator())).from(qAqm).where(part(chartIn, qAqm), tooling(chartIn, qAqm), product(chartIn, qAqm),
//				supplier(chartIn, qAqm), between(chartIn, qAqm));
//
//		if (!ObjectUtils.isEmpty(chartIn.getIndicator())) {
//			joinTable(query, chartIn);
//		}
//
//		if (ObjectUtils.isEmpty(chartIn.getIndicator())) {
//			query.groupBy(qAqm.ppaStatus);
//		} else {
//			query.groupBy(indicator(chartIn, qAqm));
//		}
//		query.orderBy(OrderByNull.DEFAULT);
//
//		//List<ChartDto> fetch = query.fetch();
//
//		return query.fetch();
//	}
//
//	// TODO QD Data
//	public List<ChartDto> getQdData(PpaChartIn chartIn) {
//		QAiPqResult qAqm = QAiPqResult.aiPqResult;
//
//		JPAQuery<ChartDto> query = queryFactory.select(projectorToQd(chartIn.getIndicator())).from(qAqm).where(part(chartIn, qAqm), tooling(chartIn, qAqm), product(chartIn, qAqm),
//				supplier(chartIn, qAqm), between(chartIn, qAqm));
//
//		if (!ObjectUtils.isEmpty(chartIn.getIndicator())) {
//			joinTableAndGroupByToQdData(query, chartIn);
//		}
//
//		query.orderBy(OrderByNull.DEFAULT);
//
//		return query.fetch();
//	}
//
//	private void joinTable(JPAQuery<ChartDto> query, PpaChartIn chartIn) {
//		//QAqm qAqm = QAqm.aqm;
//		PpaChartIndicator indicator = chartIn.getIndicator();
//		if (PpaChartIndicator.PART.equals(indicator)) {
//			query.join(QPart.part).on(QAiPqResult.aiPqResult.part.id.eq(QPart.part.id));
//		}
//		if (PpaChartIndicator.PRODUCT.equals(indicator)) {
//			query.join(QCategory.category).on(QAiPqResult.aiPqResult.category.id.eq(QCategory.category.id));
//		}
//		if (PpaChartIndicator.SUPPLIER.equals(indicator)) {
//			query.join(QCompany.company).on(QAiPqResult.aiPqResult.company.id.eq(QCompany.company.id));
//		}
//		if (PpaChartIndicator.TOOLING.equals(indicator)) {
//			query.join(QMold.mold).on(QAiPqResult.aiPqResult.mold.id.eq(QMold.mold.id));
//		}
//
//	}
//
//	private Predicate between(PpaChartIn chartIn, QAiPqResult qAqm) {
//		if (ObjectUtils.isEmpty(chartIn.getFromDate()) || ObjectUtils.isEmpty(chartIn.getToDate()))
//			return null;
//		return qAqm.hour.between(chartIn.getFromDate(), chartIn.getToDate());
//	}
//
//	@SuppressWarnings("rawtypes")
//	private Expression[] indicator(PpaChartIn chartIn, QAiPqResult qAqm) {
//		PpaChartIndicator indicator = chartIn.getIndicator();
//		if (indicator.equals(PpaChartIndicator.PART)) {
//			return asArray(qAqm.part.id, QPart.part.name, qAqm.ppaStatus);
//		}
//		if (indicator.equals(PpaChartIndicator.PRODUCT)) {
//			return asArray(qAqm.category.id, QCategory.category.name, qAqm.ppaStatus);
//		}
//		if (indicator.equals(PpaChartIndicator.SUPPLIER)) {
//			return asArray(qAqm.company.id, QCompany.company.name, qAqm.ppaStatus);
//		}
//		if (indicator.equals(PpaChartIndicator.TOOLING)) {
//			return asArray(qAqm.mold.id, QMold.mold.equipmentCode, qAqm.ppaStatus);
//		}
//		return null;
//	}
//
//	@SuppressWarnings("rawtypes")
//	private Expression[] asArray(Expression... ex) {
//		List<Expression> list = new ArrayList<>();
//		for (Expression e : ex) {
//			list.add(e);
//		}
//		return list.toArray(new Expression[ex.length]);
//	}
//
//	private BooleanExpression supplier(PpaChartIn chartIn, QAiPqResult qAqm) {
//		if (ObjectUtils.isEmpty(chartIn.getSupplierId()))
//			return null;
//		return qAqm.company.id.in(chartIn.getSupplierId());
//	}
//
//	private BooleanExpression product(PpaChartIn chartIn, QAiPqResult qAqm) {
//		if (ObjectUtils.isEmpty(chartIn.getProductId()))
//			return null;
//		return qAqm.category.id.in(chartIn.getProductId());
//	}
//
//	private BooleanExpression tooling(PpaChartIn chartIn, QAiPqResult qAqm) {
//		if (ObjectUtils.isEmpty(chartIn.getToolingId()))
//			return null;
//		return qAqm.mold.id.in(chartIn.getToolingId());
//	}
//
//	private BooleanExpression part(PpaChartIn chartIn, QAiPqResult qAqm) {
//		if (ObjectUtils.isEmpty(chartIn.getPartId()))
//			return null;
//		return qAqm.part.id.in(chartIn.getPartId());
//	}
//
//	private QBean<ChartDto> projector(PpaChartIndicator indicator) {
//		QAiPqResult qAqm = QAiPqResult.aiPqResult;
//		if (ObjectUtils.isEmpty(indicator))
//			return Projections.fields(ChartDto.class, qAqm.ppaStatus, qAqm.id.count().as("count"));
//		else {
//			if (indicator.equals(PpaChartIndicator.PART)) {
//				return Projections.fields(ChartDto.class, qAqm.part.id, QPart.part.name.as("indicator"), qAqm.ppaStatus, qAqm.id.count().as("count"));
//			}
//			if (indicator.equals(PpaChartIndicator.PRODUCT)) {
//				return Projections.fields(ChartDto.class, qAqm.category.id, QCategory.category.name.as("indicator"), qAqm.ppaStatus, qAqm.id.count().as("count"));
//			}
//			if (indicator.equals(PpaChartIndicator.SUPPLIER)) {
//				return Projections.fields(ChartDto.class, qAqm.company.id, QCompany.company.name.as("indicator"), qAqm.ppaStatus, qAqm.id.count().as("count"));
//			}
//			if (indicator.equals(PpaChartIndicator.TOOLING)) {
//				return Projections.fields(ChartDto.class, qAqm.mold.id, QMold.mold.equipmentCode.as("indicator"), qAqm.ppaStatus, qAqm.id.count().as("count"));
//			}
//			return Projections.fields(ChartDto.class, qAqm.ppaStatus, qAqm.id.count().as("count"));
//		}
//	}
//
//	private QBean<ChartDto> projectorToQd(PpaChartIndicator indicator) {
//		QAiPqResult qAqm = QAiPqResult.aiPqResult;
//		if (ObjectUtils.isEmpty(indicator)) {
//			return Projections.fields(ChartDto.class,
//					new CaseBuilder().when(qAqm.qdStatus.eq(QdStatus.GOOD.toString())).then(1).otherwise(Expressions.nullExpression()).count().as("goodCount"),
//					new CaseBuilder().when(qAqm.qdStatus.eq(QdStatus.BAD.toString())).then(1).otherwise(Expressions.nullExpression()).count().as("badCount"));
//		} else {
//			if (indicator.equals(PpaChartIndicator.PART)) {
//				return Projections.fields(ChartDto.class, qAqm.part.id, QPart.part.name.as("indicator"),
//						new CaseBuilder().when(qAqm.qdStatus.eq(QdStatus.GOOD.toString())).then(1).otherwise(Expressions.nullExpression()).count().as("goodCount"),
//						new CaseBuilder().when(qAqm.qdStatus.eq(QdStatus.BAD.toString())).then(1).otherwise(Expressions.nullExpression()).count().as("badCount"));
//			}
//			if (indicator.equals(PpaChartIndicator.PRODUCT)) {
//				return Projections.fields(ChartDto.class, qAqm.category.id, QCategory.category.name.as("indicator"),
//						new CaseBuilder().when(qAqm.qdStatus.eq(QdStatus.GOOD.toString())).then(1).otherwise(Expressions.nullExpression()).count().as("goodCount"),
//						new CaseBuilder().when(qAqm.qdStatus.eq(QdStatus.BAD.toString())).then(1).otherwise(Expressions.nullExpression()).count().as("badCount"));
//			}
//			if (indicator.equals(PpaChartIndicator.SUPPLIER)) {
//				return Projections.fields(ChartDto.class, qAqm.company.id, QCompany.company.name.as("indicator"),
//						new CaseBuilder().when(qAqm.qdStatus.eq(QdStatus.GOOD.toString())).then(1).otherwise(Expressions.nullExpression()).count().as("goodCount"),
//						new CaseBuilder().when(qAqm.qdStatus.eq(QdStatus.BAD.toString())).then(1).otherwise(Expressions.nullExpression()).count().as("badCount"));
//			}
//			if (indicator.equals(PpaChartIndicator.TOOLING)) {
//				return Projections.fields(ChartDto.class, qAqm.mold.id, QMold.mold.equipmentCode.as("indicator"),
//						new CaseBuilder().when(qAqm.qdStatus.eq(QdStatus.GOOD.toString())).then(1).otherwise(0).count().as("goodCount"),
//						new CaseBuilder().when(qAqm.qdStatus.eq(QdStatus.BAD.toString())).then(1).otherwise(Expressions.nullExpression()).count().as("badCount"));
//			}
//			return Projections.fields(ChartDto.class,
//					new CaseBuilder().when(qAqm.qdStatus.eq(QdStatus.GOOD.toString())).then(1).otherwise(Expressions.nullExpression()).count().as("goodCount"),
//					new CaseBuilder().when(qAqm.qdStatus.eq(QdStatus.BAD.toString())).then(1).otherwise(Expressions.nullExpression()).count().as("badCount"));
//		}
//	}
//
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public static class OrderByNull extends OrderSpecifier {
//		public static final OrderByNull DEFAULT = new OrderByNull();
//
//		private OrderByNull() {
//			super(Order.ASC, NullExpression.DEFAULT, NullHandling.Default);
//		}
//	}
//
//	private void joinTableAndGroupByToQdData(JPAQuery<ChartDto> query, PpaChartIn chartIn) {
//		PpaChartIndicator indicator = chartIn.getIndicator();
//		if (PpaChartIndicator.PART.equals(indicator)) {
//			query.join(QPart.part).on(QAiPqResult.aiPqResult.part.id.eq(QPart.part.id));
//			query.groupBy(QPart.part.id);
//		}
//		if (PpaChartIndicator.PRODUCT.equals(indicator)) {
//			query.join(QCategory.category).on(QAiPqResult.aiPqResult.category.id.eq(QCategory.category.id));
//			query.groupBy(QCategory.category.id);
//		}
//		if (PpaChartIndicator.SUPPLIER.equals(indicator)) {
//			query.join(QCompany.company).on(QAiPqResult.aiPqResult.company.id.eq(QCompany.company.id));
//			query.groupBy(QCompany.company.id);
//		}
//		if (PpaChartIndicator.TOOLING.equals(indicator)) {
//			query.join(QMold.mold).on(QAiPqResult.aiPqResult.mold.id.eq(QMold.mold.id));
//			query.groupBy(QMold.mold.id);
//		}
//	}
//}