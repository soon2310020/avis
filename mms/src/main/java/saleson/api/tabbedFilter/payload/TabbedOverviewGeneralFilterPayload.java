package saleson.api.tabbedFilter.payload;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.util.Pair;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;

import lombok.Data;
import saleson.common.enumeration.Frequent;
import saleson.common.util.DateUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.model.QCompany;
import saleson.model.QMold;
import saleson.model.QMoldPart;
import saleson.model.QPart;
import saleson.service.util.DateTimeUtils;

@Data
public class TabbedOverviewGeneralFilterPayload {
    private boolean allProduct;
    private boolean allPart;
    private boolean allTooling;
    private boolean allSupplier;

    private List<Long> productIds;
    private List<Long> partIds;
    private List<Long> toolingIds;
    private List<Long> supplierIds;
    private Integer duration; // number of days in time period filter

    @JsonIgnore
    private Double mainRate;

    public BooleanBuilder getPartCommonFilter() {
        QPart part = QPart.part;
        BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(part.id.in(JPQLQueryUtils.getPartIdsSubquery())//
//					.or(part.createdBy.eq(SecurityUtils.getUserId()))//
			);
		}
        builder.and(part.enabled.isTrue());
        return builder;
    }


    public BooleanBuilder getPartFilter() {
        QPart part = QPart.part;
        QMoldPart moldPart = QMoldPart.moldPart;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(getPartCommonFilter());
        BooleanBuilder subBuilder = new BooleanBuilder();

        if (!this.isAllPart() && CollectionUtils.isNotEmpty(this.getPartIds())) {
            subBuilder.or(part.id.in(this.getPartIds()));
        } else if (this.isAllPart()) {
            subBuilder.or(part.id.isNotNull());
        }

        if (!this.isAllProduct() && CollectionUtils.isNotEmpty(this.getProductIds())) {
            subBuilder.or(part.categoryId.in(this.getProductIds()));
        } else if (this.isAllProduct()) {
            subBuilder.or(part.categoryId.isNotNull());
        }

        if (!this.isAllTooling() && CollectionUtils.isNotEmpty(this.getToolingIds())) {
            subBuilder.or(part.id.in(JPAExpressions
                    .select(moldPart.partId).distinct()
                    .from(moldPart)
                    .where(moldPart.moldId.in(this.getToolingIds()))));
        } else if (this.allTooling) {
            subBuilder.or(part.id.in(JPAExpressions
                    .select(moldPart.partId).distinct()
                    .from(moldPart)
                    .where(moldPart.moldId.isNotNull())));
        }

        if (!this.isAllSupplier() && CollectionUtils.isNotEmpty(this.getSupplierIds())) {
            subBuilder.or(part.id.in(JPAExpressions
                    .select(moldPart.partId).distinct()
                    .from(moldPart)
                    .where(moldPart.mold.companyId.in(this.getSupplierIds()))));
        } else if (this.isAllSupplier()) {
            subBuilder.or(part.id.in(JPAExpressions
                    .select(moldPart.partId).distinct()
                    .from(moldPart)
                    .where(moldPart.mold.companyId.isNotNull())));
        }

        if (!this.isAllSupplier() && CollectionUtils.isEmpty(this.getSupplierIds())
                && !this.isAllTooling() && CollectionUtils.isEmpty(this.getToolingIds())
                && !this.isAllProduct() && CollectionUtils.isEmpty(this.getProductIds())
                && !this.isAllPart() && CollectionUtils.isEmpty(this.getPartIds())
        ) {
            builder.and(part.id.isNull());//condition for none
        }

        builder.and(subBuilder);

        return builder;
    }

    public BooleanBuilder getPartFilterWithPeriod(boolean current) {
        QPart part = QPart.part;
        BooleanBuilder builder = new BooleanBuilder();
        Pair<Instant, Instant> startEnd = getStartEndByDuration(current);
        builder.and(getPartFilter()).and(part.createdAt.goe(startEnd.getFirst()).and(part.createdAt.loe(startEnd.getSecond())));
        return builder;
    }

    public BooleanBuilder getMoldFilter() {
        QMold mold = QMold.mold;
        return getMoldFilter(mold);
    }

    public BooleanBuilder getMoldFilter(QMold mold) {
        QMoldPart moldPart = QMoldPart.moldPart;
        QCompany company = QCompany.company;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()));
        builder.and(mold.companyId.notIn(JPAExpressions
                .select(company.id)
                .from(company)
                .where(company.isEmoldino.isTrue())));

        BooleanBuilder subBuilder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			subBuilder.and((mold.id.in(JPQLQueryUtils.getMoldIdsSubquery())));
		}
        if (!this.isAllPart() && CollectionUtils.isNotEmpty(this.getPartIds())) {
            subBuilder.or(mold.id.in(JPAExpressions
                    .select(moldPart.moldId).distinct()
                    .from(moldPart)
                    .where(moldPart.partId.in(this.getPartIds()))));
        } else if (this.isAllPart()) {
            subBuilder.or(mold.id.in(JPAExpressions
                    .select(moldPart.moldId).distinct()
                    .from(moldPart)
                    .where(moldPart.partId.isNotNull())));
        }

        if (!this.isAllProduct() && CollectionUtils.isNotEmpty(this.getProductIds())) {
            subBuilder.or(mold.id.in(JPAExpressions
                    .select(moldPart.moldId).distinct()
                    .from(moldPart)
                    .where(moldPart.part.categoryId.in(this.getProductIds()))));
        } else if (this.isAllProduct()) {
            subBuilder.or(mold.id.in(JPAExpressions
                    .select(moldPart.moldId).distinct()
                    .from(moldPart)
                    .where(moldPart.part.categoryId.isNotNull())));
        }

        if (!this.isAllTooling() && CollectionUtils.isNotEmpty(this.getToolingIds())) {
            subBuilder.or(mold.id.in(this.getToolingIds()));
        } else if (this.isAllTooling()) {
            subBuilder.or(mold.id.isNotNull());
        }

        if (!this.isAllSupplier() && CollectionUtils.isNotEmpty(this.getSupplierIds())) {
            subBuilder.or(mold.companyId.in(this.getSupplierIds()));
        } else if (this.isAllSupplier()) {
            subBuilder.or(mold.companyId.isNotNull());
        }

        if (!this.isAllSupplier() && CollectionUtils.isEmpty(this.getSupplierIds())
                && !this.isAllTooling() && CollectionUtils.isEmpty(this.getToolingIds())
                && !this.isAllProduct() && CollectionUtils.isEmpty(this.getProductIds())
                && !this.isAllPart() && CollectionUtils.isEmpty(this.getPartIds())
        ) {
            builder.and(mold.id.isNull());//condition for none
        }

        builder.and(subBuilder);

        return builder;
    }

    public BooleanBuilder getMoldFilterWithPeriod(boolean current) {
        QMold mold = QMold.mold;
        BooleanBuilder builder = new BooleanBuilder();
        Pair<Instant, Instant> startEnd = getStartEndByDuration(current);
        builder.and(getMoldFilter(mold)).and(mold.createdAt.goe(startEnd.getFirst()).and(mold.createdAt.loe(startEnd.getSecond())));
        return builder;
    }

    public BooleanBuilder getMoldFilterWithPeriod(boolean current, QMold mold) {
        BooleanBuilder builder = new BooleanBuilder();
        Pair<Instant, Instant> startEnd = getStartEndByDuration(current);
        builder.and(getMoldFilter(mold)).and(mold.createdAt.goe(startEnd.getFirst()).and(mold.createdAt.loe(startEnd.getSecond())));
        return builder;
    }

    public Pair<Instant, Instant> getStartEndByDuration(boolean current) {
        Instant end;
        Instant start;
        if (current) {
            end = DateTimeUtils.getEndOfDay(Instant.now());
            start = DateTimeUtils.getStartOfDay(Instant.now().minus(this.getDuration(), ChronoUnit.DAYS));
        } else {
            end = DateTimeUtils.getEndOfDay(Instant.now().minus(this.getDuration(), ChronoUnit.DAYS));
            start = DateTimeUtils.getStartOfDay(end.minus(this.getDuration(), ChronoUnit.DAYS));
        }
        return Pair.of(start, end);
    }

    public Pair<String, String> getStartEndStringByDuration(boolean current) {
        Pair<Instant, Instant> instantPair = getStartEndByDuration(current);
        return Pair.of(DateUtils.getDate(instantPair.getFirst(), DateUtils.DEFAULT_DATE_FORMAT), DateUtils.getDate(instantPair.getSecond(), DateUtils.DEFAULT_DATE_FORMAT));
    }

    public Frequent getFrequentFromDuration() {
        if (duration == 7) return Frequent.DAILY;
        else if (duration == 30) return Frequent.WEEKLY;
        else return Frequent.MONTHLY;
    }

}
