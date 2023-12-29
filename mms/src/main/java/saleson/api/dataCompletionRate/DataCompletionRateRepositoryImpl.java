package saleson.api.dataCompletionRate;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPQLQuery;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.common.enumeration.CompanyType;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.Company;
import saleson.model.DataCompletionRate;
import saleson.model.QDataCompletionRate;
import saleson.model.QCompany;
import saleson.model.data.completionRate.CompletionRateData;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataCompletionRateRepositoryImpl extends QuerydslRepositorySupport implements DataCompletionRateRepositoryCustom {
    public DataCompletionRateRepositoryImpl() {
        super(DataCompletionRate.class);
    }

    @Autowired
    private DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

    @Override
    public Page<CompletionRateData> getCompanyCompletionRate(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });

        QDataCompletionRate dataCompletionRate = QDataCompletionRate.dataCompletionRate;
        QCompany company = QCompany.company;
        BooleanBuilder builder = new BooleanBuilder();
        if (CollectionUtils.isNotEmpty(payload.getCompanyId())) {
            builder.and(dataCompletionRate.companyId.in(payload.getCompanyId()));
        }
        if (AccessControlUtils.isAccessFilterRequired()) {
            if((!SecurityUtils.getCompanyType().equals(CompanyType.TOOL_MAKER))) {
                builder.and(company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())
                        .or(company.createdBy.eq(SecurityUtils.getUserId())));
            }else if((SecurityUtils.getCompanyType().equals(CompanyType.TOOL_MAKER))){
                builder.and(company.id.eq(SecurityUtils.getCompanyId()));
            }
        }

        builder.and(company.isEmoldino.isFalse());

        List<Long> companyIds = dashboardGeneralFilterUtils.getCompanyIds();
        if (CollectionUtils.isNotEmpty(companyIds)) {
            builder.and(dataCompletionRate.companyId.in(companyIds));
        } else {
            //trick for no company
            builder.and(dataCompletionRate.companyId.isNull());
        }

        NumberExpression<Double> totalPercent =
                (new CaseBuilder().when(dataCompletionRate.companyRate.isNull()).then(0D).otherwise(dataCompletionRate.companyRate))
                        .add(new CaseBuilder().when(dataCompletionRate.locationRate.isNull()).then(0D).otherwise(dataCompletionRate.locationRate))
                        .add(new CaseBuilder().when(dataCompletionRate.categoryRate.isNull()).then(0D).otherwise(dataCompletionRate.categoryRate))
                        .add(new CaseBuilder().when(dataCompletionRate.partRate.isNull()).then(0D).otherwise(dataCompletionRate.partRate))
                        .add(new CaseBuilder().when(dataCompletionRate.moldRate.isNull()).then(0D).otherwise(dataCompletionRate.moldRate))
                        .add(new CaseBuilder().when(dataCompletionRate.machineRate.isNull()).then(0D).otherwise(dataCompletionRate.machineRate));

        NumberExpression avgRate;

        if (forAvg) {
            avgRate = (totalPercent.divide(6)).avg();
        } else {
            avgRate = totalPercent.divide(6);
        }
        JPQLQuery query = from(dataCompletionRate).leftJoin(company).on(dataCompletionRate.companyId.eq(company.id)).where(builder);
        if ("rate".equalsIgnoreCase(properties[0])) {
            NumberExpression numberExpression = avgRate;
            OrderSpecifier numberOrder = numberExpression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                numberOrder = numberExpression.asc();
            }
            query.orderBy(numberOrder);
        } else {
            StringExpression expression = dataCompletionRate.companyName;
            OrderSpecifier order = expression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                order = expression.asc();
            }
            query.orderBy(order);
        }
        long total = query.fetchCount();

        if (total > 0) {
            query.select(Projections.constructor(CompletionRateData.class,
                    dataCompletionRate.companyId,
                    company.companyCode,
                    company.name,
                    avgRate.intValue(),
                    avgRate,
                    dataCompletionRate.updatedAt,
                    company)
            );
            if (!forAvg) {
                query.offset(pageable.getOffset());
                query.limit(pageable.getPageSize());
            }

            List<CompletionRateData> list = query.fetch();
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.CEILING);
            list.forEach(i -> i.setRate(Double.parseDouble(df.format(i.getRate() / 100))));
            return new PageImpl<>(list, pageable, total);
        } else {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }
}
