package saleson.api.category;

import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.util.ObjectUtils;
import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.api.part.PartRepository;
import saleson.api.tabbedFilter.payload.TabbedOverviewGeneralFilterPayload;
import saleson.common.config.Const;
import saleson.common.util.DashboardGeneralFilterUtils;

import saleson.common.enumeration.StorageType;
import saleson.model.*;
import saleson.model.data.completionRate.CompletionRateData;
import saleson.model.data.dashboard.totalPart.ProductData;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepositoryImpl extends QuerydslRepositorySupport implements CategoryRepositoryCustom {
    public CategoryRepositoryImpl() {
        super(Category.class);
    }

    @Autowired
    private DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

    @Autowired
    private PartRepository partRepository;

    @Override
    public Page<CompletionRateData> getCompletionRateData(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QCategory category = QCategory.category;
        QFileStorage fileStorage = QFileStorage.fileStorage;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(category.enabled.isNull().or(category.enabled.isTrue()));
        builder.and(dashboardGeneralFilterUtils.getCategoryFilter(category));

        NumberExpression<Integer> numberLine =new CaseBuilder().when(category.parentId.isNull()).then(Const.numberLine.CATEGORY).otherwise(Const.numberLine.PROJECT);
        NumberExpression<Integer> numEnteredCategory =
                (new CaseBuilder().when(category.name.isEmpty().or(category.name.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(category.description.isEmpty().or(category.description.isNull())).then(0).otherwise(1));
        NumberExpression<Integer> numEnteredProject =
                (new CaseBuilder().when(category.name.isEmpty().or(category.name.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(category.description.isEmpty().or(category.description.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(category.parentId.isNull()).then(0).otherwise(1))
                        .add(new CaseBuilder().when(category.weeklyProductionDemand.isNull()).then(0).otherwise(1))
                        .add(new CaseBuilder().when(
                                        JPAExpressions
                                                .select(fileStorage.id.min())
                                                .from(fileStorage)
                                                .where(
                                                        fileStorage.refId.eq(category.id)
                                                                .and(fileStorage.storageType.eq(StorageType.PROJECT_IMAGE)))
                                                .isNull())
                                .then(0)
                                .otherwise(1))
                ;
        NumberExpression<Integer> numEntered =new CaseBuilder().when(category.parentId.isNull()).then(numEnteredCategory).otherwise(numEnteredProject);

/*
        NumberExpression<Integer> numEntered =
                (new CaseBuilder().when(category.name.isEmpty().or(category.name.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(category.description.isEmpty().or(category.description.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(category.parent.isNull()).then(0).otherwise(1));
*/

        NumberExpression rateValue;

        if (forAvg){
            rateValue = (numEntered.floatValue().divide(numberLine)).avg();
        } else {
            rateValue = numEntered.floatValue().divide(numberLine);
        }

        if (payload.isUncompletedData()){
            builder.and(rateValue.doubleValue().lt(1));
        }
        if (CollectionUtils.isNotEmpty(payload.getIds())){
            builder.and(category.id.in(payload.getIds()));
        }
        JPQLQuery query = from(category)
                .where(builder);
        if ("rate".equalsIgnoreCase(properties[0])) {
            NumberExpression numberExpression = rateValue;
            OrderSpecifier numberOrder = numberExpression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                numberOrder = numberExpression.asc();
            }
            query.orderBy(numberOrder);
        } else {
            StringExpression expression = category.name;
            OrderSpecifier order = expression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                order = expression.asc();
            }
            query.orderBy(order);
        }
        long total = query.fetchCount();

        if (total > 0) {
            query.select(Projections.constructor(CompletionRateData.class, category.id, category.name, category.name, numEntered, rateValue, category.updatedAt, category));

            if (!forAvg) {
                query.offset(pageable.getOffset());
                query.limit(pageable.getPageSize());
            }
            List<CompletionRateData> list= query.fetch();
            return new PageImpl<>(list, pageable, total);
        } else {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    @Override
    public CompletionRateData getCompanyCompletionRate() {
        QCategory category = QCategory.category;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(category.enabled.isNull().or(category.enabled.isTrue()));

        NumberExpression<Integer> numEntered =
                (new CaseBuilder().when(category.name.isEmpty().or(category.name.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(category.description.isEmpty().or(category.description.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(category.parent.isNull()).then(0).otherwise(1));

        NumberExpression rateValue = (numEntered.floatValue().divide(Const.numberLine.CATEGORY)).avg();
        JPQLQuery query = from(category)
                .where(builder);
        query.select(Projections.constructor(CompletionRateData.class, category.id, rateValue, category.updatedAt));
        return (CompletionRateData) query.fetchFirst();
    }

    @Override
    public Page<ProductData> getProductPartData(TabbedOverviewGeneralFilterPayload payload, Pageable pageable) {
        QCategory category = QCategory.category;
        QPart part = QPart.part;

        JPQLQuery query = from(category)
                .leftJoin(part).on(category.id.eq(part.categoryId))
                .where(category.level.eq(3).and(payload.getPartFilter()));
        NumberExpression<Long> partCount = part.id.countDistinct();
        query.groupBy(category.id);
        long total = query.fetchCount();
        total++;

        StringExpression expression = category.name;
        query.orderBy(expression.asc());
        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        query.select(Projections.constructor(ProductData.class, category.id, category.name, partCount));
        List<ProductData> list = query.fetch();
        Page<ProductData> page = new PageImpl<>(list, pageable, total);

        Long unassignedPartCount = partRepository.countByFilter(payload.getPartFilter().and(QPart.part.categoryId.isNull()));
        if (pageable.getPageNumber() == (page.getTotalPages() - 1)) {
            list.add(new ProductData(null, "Unassigned", null, unassignedPartCount));
        }
        return new PageImpl<>(list, pageable, total);
    }

    @Override
    public Page<Category> findAllProductByPart(Long partId, List<Long> supplierId, Long moldId, Pageable pageable) {
        return findAllProductByPart(partId,supplierId,moldId,null,pageable);
    }

    private JPQLQuery<Category> toQueryByPart(Long partId, List<Long> supplierId, Long moldId, String searchWord) {
        QCategory category = QCategory.category;
        QPart part = QPart.part;


        BooleanBuilder filter = new BooleanBuilder();
        if (partId != null) {
            part.id.eq(partId);
        }

        filter.and(category.enabled.isTrue());

        if (!ObjectUtils.isEmpty(searchWord)) {
            String str = "%" + searchWord + "%";
            filter.and(category.name.likeIgnoreCase(str));
        }

        if (moldId != null || !ObjectUtils.isEmpty(supplierId)) {
            QMoldPart moldPart = QMoldPart.moldPart;
            BooleanBuilder subFilter = new BooleanBuilder();
            if (moldId != null) {
                subFilter.and(moldPart.moldId.eq(moldId));
            }
            if (!ObjectUtils.isEmpty(supplierId)) {
                subFilter.and(moldPart.mold.companyId.in(supplierId));
            }
            filter.and(part.id.in(JPAExpressions.select(moldPart.partId).from(moldPart).where(subFilter)));
        }
        JPQLQuery<Category> query = from(category);
        if(partId!=null || moldId != null || !ObjectUtils.isEmpty(supplierId))
        query = query.join(part).on(category.id.eq(part.categoryId));

        query = query.where(filter);
        return query;
    }

    private Page<Category> findAllProductByPart(Long partId, List<Long> supplierId, Long moldId, String searchWord, Pageable pageable) {
        JPQLQuery<Category> query = toQueryByPart(partId, supplierId, moldId, searchWord);
        long total = query.fetchCount();
        QCategory category = QCategory.category;
        query.orderBy(category.name.asc());
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());
        List<Category> content = query.fetch();
        return new PageImpl<>(content, pageable, total);
    }

}
