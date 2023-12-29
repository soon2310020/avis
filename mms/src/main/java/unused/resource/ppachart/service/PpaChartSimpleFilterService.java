//package unused.resource.ppachart.service;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.util.ObjectUtils;
//
//import com.emoldino.api.integration.resource.base.repository.pq.QAiPqResult;
//import com.querydsl.core.BooleanBuilder;
//import com.querydsl.core.types.Projections;
//import com.querydsl.jpa.impl.JPAQuery;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//
//import saleson.common.enumeration.CompanyType;
//import saleson.model.QCategory;
//import saleson.model.QCompany;
//import saleson.model.QMold;
//import saleson.model.QPart;
//import unused.resource.ppachart.dto.FilterDto;
//import unused.resource.ppachart.dto.PpaChartFilterIn;
//
//@Service
//public class PpaChartSimpleFilterService {
//
//	@Autowired
//	private JPAQueryFactory queryFactory;
//
//	public List<FilterDto> getPartList(PpaChartFilterIn filterIn) {
//		QPart part = QPart.part;
//
//		JPAQuery<FilterDto> query = queryFactory.selectDistinct(Projections.constructor(FilterDto.class, part.id, part.name)).from(part).join(QAiPqResult.aiPqResult)
//				.on(QAiPqResult.aiPqResult.part.id.eq(part.id)).orderBy(part.name.asc());
//
//		//where
//		BooleanBuilder b = new BooleanBuilder();
//		if (!ObjectUtils.isEmpty(filterIn.getQuery())) {
//			b.and(part.name.startsWith(filterIn.getQuery()));
//		}
//		query.where(b);
//
//		List<FilterDto> list = query.limit(30).fetch();
//
//		return list;
//	}
//
//	public List<FilterDto> getCompanyList(PpaChartFilterIn filterIn) {
//		QCompany company = QCompany.company;
//
//		JPAQuery<FilterDto> query = queryFactory.selectDistinct(Projections.constructor(FilterDto.class, company.id, company.name)).from(company).join(QAiPqResult.aiPqResult)
//				.on(QAiPqResult.aiPqResult.company.id.eq(company.id)).orderBy(company.name.asc());
//
//		//where
//		BooleanBuilder b = new BooleanBuilder();
//		b.and(company.companyType.eq(CompanyType.SUPPLIER));
//		//where
//		if (!ObjectUtils.isEmpty(filterIn.getQuery())) {
//			b.and(company.name.startsWith(filterIn.getQuery()));
//		}
//		query.where(b);
//		List<FilterDto> list = query.limit(30).fetch();
//
//		return list;
//	}
//
//	public List<FilterDto> getMoldList(PpaChartFilterIn filterIn) {
//		QMold mold = QMold.mold;
//
//		JPAQuery<FilterDto> query = queryFactory.selectDistinct(Projections.constructor(FilterDto.class, mold.id, mold.equipmentCode)).from(mold).join(QAiPqResult.aiPqResult)
//				.on(QAiPqResult.aiPqResult.mold.id.eq(mold.id)).orderBy(mold.equipmentCode.asc());
//		//where
//		BooleanBuilder b = new BooleanBuilder();
//		if (!ObjectUtils.isEmpty(filterIn.getQuery())) {
//			b.and(mold.equipmentCode.startsWith(filterIn.getQuery()));
//		}
//		query.where(b);
//		List<FilterDto> list = query.limit(30).fetch();
//		return list;
//	}
//
//	public List<FilterDto> getCategoryList(PpaChartFilterIn filterIn) {
//		QCategory category = QCategory.category;
//		//where
//		BooleanBuilder b = new BooleanBuilder();
//		b.and(category.level.eq(2));
//		if (!ObjectUtils.isEmpty(filterIn.getQuery())) {
//			b.and(category.name.startsWith(filterIn.getQuery()));
//		}
//
//		List<FilterDto> list = queryFactory.selectDistinct(Projections.constructor(FilterDto.class, category.id, category.name)).from(category).join(QAiPqResult.aiPqResult)
//				.on(QAiPqResult.aiPqResult.category.id.eq(category.id)).where(b).orderBy(category.name.asc()).limit(30).fetch();
//		return list;
//	}
//
//}
