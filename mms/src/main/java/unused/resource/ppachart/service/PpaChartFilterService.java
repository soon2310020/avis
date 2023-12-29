//package unused.resource.ppachart.service;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.util.ObjectUtils;
//
//import com.querydsl.core.BooleanBuilder;
//import com.querydsl.core.types.Projections;
//import com.querydsl.core.types.dsl.BooleanExpression;
//import com.querydsl.jpa.impl.JPAQuery;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//
//import saleson.common.enumeration.CompanyType;
//import saleson.model.QCategory;
//import saleson.model.QCompany;
//import saleson.model.QMold;
//import saleson.model.QMoldPart;
//import saleson.model.QPart;
//import unused.resource.ppachart.dto.FilterDto;
//import unused.resource.ppachart.dto.PpaChartFilterIn;
//
//@Service
//public class PpaChartFilterService {
//
//	@Autowired
//	private JPAQueryFactory queryFactory;
//
//	/*
//		-- PART QUERY
//		SELECT 
//			* 
//		FROM part P
//			INNER JOIN mold_part MP ON (MP.PART_ID = P.ID AND MP.MOLD_ID IN (11,1)) -- IF HAVE MOLD_ID, COMPANY_ID
//			INNER JOIN mold M ON (M.ID = MP.MOLD_ID AND M.COMPANY_ID IN ()) -- IF HAVE COMPANY_ID
//		WHERE 
//			1= 1
//			AND P.CATEGORY_ID = '' -- IF HAVE CATEGORY ID
//	*/
//
//	// Part Filter (Selected or not : Product, Tooling, Supplier)
//	public List<FilterDto> getPartList(PpaChartFilterIn filterIn) {
//		QPart part = QPart.part;
//
//		JPAQuery<FilterDto> query = queryFactory.select(Projections.constructor(FilterDto.class, part.id, part.name)).from(part);
//
//		//join
//		if (!ObjectUtils.isEmpty(filterIn.getToolingId()) || !ObjectUtils.isEmpty(filterIn.getCompanyId())) {
//			query.join(QMoldPart.moldPart);
//			query.on(onConditionPartToMoldPart(filterIn));
//			if (!ObjectUtils.isEmpty(filterIn.getCompanyId())) {
//				query.join(QMold.mold).on(QMoldPart.moldPart.moldId.eq(QMold.mold.id).and(QMold.mold.companyId.in(filterIn.getCompanyId())));
//			}
//		}
//
//		//where
//		BooleanBuilder b = new BooleanBuilder();
//		if (!ObjectUtils.isEmpty(filterIn.getProductionId())) {
//			b.and(part.categoryId.in(filterIn.getProductionId()));
//		}
//		query.where(b);
//
//		List<FilterDto> list = query.limit(30).fetch();
//
//		return list;
//	}
//
//	/*
//	    -- COMPANY QUERY
//		SELECT 
//			* 
//		FROM COMPANY C
//			INNER JOIN mold M ON (C.ID = M.SUPPLIER_COMPANY_ID AND M.ID IN ('')) -- IF HAVE MOLD PART CATEGORY
//			INNER JOIN mold_part MP ON ( M.ID = MP.MOLD_ID AND M.PART_ID IN ('')) -- IF HAVE PART CATEGORY
//			INNER JOIN part P ON ( MP.PART_ID = P.ID AND P.CATEGORY_ID IN ('')) -- IF HAVE CATEGORY
//		WHERE 
//		1 = 1
//		AND C.COMPANY_TYPE = 'SUPPLIER'		
//		;
//	*/
//
//	// Supplier Filter (Selected or not : Product, Tooling, Part)
//	public List<FilterDto> getCompanyList(PpaChartFilterIn filterIn) {
//		QCompany company = QCompany.company;
//
//		JPAQuery<FilterDto> query = queryFactory.select(Projections.constructor(FilterDto.class, company.id, company.name)).from(company);
//
//		//join
//		if (!ObjectUtils.isEmpty(filterIn.getToolingId()) || !ObjectUtils.isEmpty(filterIn.getPartId()) || !ObjectUtils.isEmpty(filterIn.getProductionId())) {
//
//			// mold join
//			query.join(QMold.mold);
//			query.on(onConditionCompanyToMold(filterIn));
//			if (!ObjectUtils.isEmpty(filterIn.getPartId()) || !ObjectUtils.isEmpty(filterIn.getProductionId())) {
//				// moldpart join
//				query.join(QMoldPart.moldPart);
//				query.on(onConditionMoldpartToMoldInPartId(filterIn));
//				if (!ObjectUtils.isEmpty(filterIn.getProductionId())) {
//
//					// part join
//					query.join(QPart.part);
//					query.on(QMoldPart.moldPart.partId.eq(QPart.part.id).and(QPart.part.categoryId.in(filterIn.getProductionId())));
//				}
//			}
//		}
//
//		//where
//		BooleanBuilder b = new BooleanBuilder();
//		b.and(company.companyType.eq(CompanyType.SUPPLIER));
//
//		query.where(b);
//		List<FilterDto> list = query.limit(30).fetch();
//
//		return list;
//	}
//
//	//	
//	/*	
//	    -- MOLD QUERY	
//	    SELECT 
//			* 
//		FROM MOLD M
//			INNER JOIN mold_part MP ON (MP.MOLD_ID = M.ID AND MP.PART_ID IN (100667,101418,101417)) -- IF HAVE PART_ID, CATEGORY_ID
//			INNER JOIN PART P ON (P.ID = MP.PART_ID AND P.CATEGORY_ID IN (3821,17334)) -- IF HAVE CATEGORY_ID
//		WHERE 
//			1= 1
//			AND M.COMPANY_ID IN (3633) -- IF HAVE COMPANY ID
//	*/
//
//	// Mold Filter (Selected or not : Product, Supplier, Part)
//	public List<FilterDto> getMoldList(PpaChartFilterIn filterIn) {
//		QMold mold = QMold.mold;
//
//		JPAQuery<FilterDto> query = queryFactory.select(Projections.constructor(FilterDto.class, mold.id, mold.equipmentCode)).from(mold);
//
//		//join
//		if (!ObjectUtils.isEmpty(filterIn.getPartId()) || !ObjectUtils.isEmpty(filterIn.getProductionId())) {
//			// moldpart join
//			query.join(QMoldPart.moldPart);
//			query.on(onConditionMoldpartToMoldInPartId(filterIn));
//			if (!ObjectUtils.isEmpty(filterIn.getProductionId())) {
//
//				// part join
//				query.join(QPart.part);
//				query.on(QMoldPart.moldPart.partId.eq(QPart.part.id).and(QPart.part.categoryId.in(filterIn.getProductionId())));
//			}
//		}
//
//		if (!ObjectUtils.isEmpty(filterIn.getCompanyId())) {
//			query.where(mold.companyId.in(filterIn.getCompanyId()));
//		}
//
//		List<FilterDto> list = query.limit(30).fetch();
//		return list;
//	}
//
//	/*	
//	 --CATEGORY QUERY
//	   SELECT 
//	   * 
//	   FROM category CATE
//		  INNER JOIN part P ON (CATE.ID = P.CATEGORY_ID AND P.ID IN (100667,101418,101417)) -- IF HAVE PART, MOLD , COMPANY
//		  INNER JOIN mold_part MP ON (P.ID = MP.PART_ID AND MP.MOLD_ID IN (100675,257318))  -- IF HAVE MOLD , COMPANY
//		  INNER JOIN mold M ON (MP.MOLD_ID =  M.ID AND M.COMPANY_ID IN (3633))              -- IF HAVE COMPANY	
//	   WHERE CATE.LEVEL = 2
//	*/
//
//	// Category Filter (Selected or not : Tooling, Supplier, Part)
//	public List<FilterDto> getCategoryList(PpaChartFilterIn filterIn) {
//
//		QCategory category = QCategory.category;
//
//		JPAQuery<FilterDto> query = queryFactory.select(Projections.constructor(FilterDto.class, category.id, category.name)).from(category);
//
//		// join
//		if (!ObjectUtils.isEmpty(filterIn.getPartId()) || !ObjectUtils.isEmpty(filterIn.getToolingId()) || !ObjectUtils.isEmpty(filterIn.getCompanyId())) {
//			query.join(QPart.part);
//			query.on(onConditionCategoryToPart(filterIn));
//			if (!ObjectUtils.isEmpty(filterIn.getToolingId()) || !ObjectUtils.isEmpty(filterIn.getCompanyId())) {
//				// moldPart join
//				query.join(QMoldPart.moldPart);
//				query.on(onConditionMoldpartToMoldInMoldId(filterIn));
//				if (!ObjectUtils.isEmpty(filterIn.getCompanyId())) {
//					// part join
//					query.join(QMold.mold);
//					query.on(QMoldPart.moldPart.moldId.eq(QMold.mold.id).and(QMold.mold.companyId.in(filterIn.getCompanyId())));
//				}
//			}
//		}
//
//		// Where		
//		BooleanExpression filter = category.level.eq(2);
//		query.where(filter);
//
//		return query.limit(30).fetch();
//	}
//
//	private BooleanExpression onConditionPartToMoldPart(PpaChartFilterIn filterIn) {
//		BooleanExpression eq = QPart.part.id.eq(QMoldPart.moldPart.partId);
//		if (!ObjectUtils.isEmpty(filterIn.getToolingId())) {
//			return eq.and(QMoldPart.moldPart.moldId.in(filterIn.getToolingId()));
//		}
//		return eq;
//	}
//
//	private BooleanExpression onConditionCompanyToMold(PpaChartFilterIn filterIn) {
//		BooleanExpression eq = QCompany.company.id.eq(QMold.mold.supplierCompanyId);
//		if (!ObjectUtils.isEmpty(filterIn.getToolingId())) {
//			return eq.and(QMold.mold.id.in(filterIn.getToolingId()));
//		}
//		return eq;
//	}
//
//	private BooleanExpression onConditionMoldpartToMoldInPartId(PpaChartFilterIn filterIn) {
//		BooleanExpression eq = QMoldPart.moldPart.moldId.eq(QMold.mold.id);
//		if (!ObjectUtils.isEmpty(filterIn.getPartId())) {
//			return eq.and(QMoldPart.moldPart.partId.in(filterIn.getPartId()));
//		}
//		return eq;
//	}
//
//	private BooleanExpression onConditionMoldpartToMoldInMoldId(PpaChartFilterIn filterIn) {
//		BooleanExpression eq = QMoldPart.moldPart.partId.eq(QPart.part.id);
//		if (!ObjectUtils.isEmpty(filterIn.getToolingId())) {
//			return eq.and(QMoldPart.moldPart.moldId.in(filterIn.getToolingId()));
//		}
//		return eq;
//	}
//
//	private BooleanExpression onConditionCategoryToPart(PpaChartFilterIn filterIn) {
//		BooleanExpression eq = QCategory.category.id.eq(QPart.part.categoryId);
//		if (!ObjectUtils.isEmpty(filterIn.getPartId())) {
//			return eq.and(QPart.part.id.in(filterIn.getPartId()));
//		}
//		return eq;
//	}
//
//}
