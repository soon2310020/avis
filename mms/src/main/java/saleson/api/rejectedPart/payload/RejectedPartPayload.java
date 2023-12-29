package saleson.api.rejectedPart.payload;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.RejectedRateStatus;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.QCompany;
import saleson.model.QLocation;
import saleson.model.QMachineOee;
import saleson.model.QMold;
import saleson.model.rejectedPartRate.QProducedPart;
import saleson.model.rejectedPartRate.RejectedPartDetails;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RejectedPartPayload extends SearchParam {
	private List<Long> ids;
	private Long moldId;
	private Long partId;
	private Set<RejectedPartDetails> rejectedParts;
	private RejectedRateStatus rejectedRateStatus;
	private String startDate;
	private String endDate;
	private String hour;
	private String day;
	private String week;
	private String month;
	private Frequent frequent;
	private Boolean forOEE;

	private String start;
	private String end;

	private List<Long> moldIds;

	public Predicate getPredicate() {
		QProducedPart producedPart = QProducedPart.producedPart;
		QMold mold = QMold.mold;
		QLocation location = QLocation.location;
		QCompany company = QCompany.company;
		QMachineOee machineOee = QMachineOee.machineOee;

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(producedPart.mold.deleted.isNull().or(producedPart.mold.deleted.isFalse()));

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(producedPart.moldId.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		if (getIds() != null && getIds().size() > 0) {
			builder.and(producedPart.id.in(getIds()));
		}

		if (getId() != null)
			builder.and(producedPart.id.eq(getId()));

		if (getMoldId() != null)
			builder.and(producedPart.moldId.eq(getMoldId()));

		if (CollectionUtils.isNotEmpty(moldIds))
			builder.and(producedPart.moldId.in(moldIds));
		else if (moldIds != null && moldIds.isEmpty())
			builder.and(producedPart.id.isNull());

		if (getPartId() != null)
			builder.and(producedPart.partId.eq(getPartId()));

		if (getQuery() != null) {
			builder.and(producedPart.mold.equipmentCode.contains(getQuery()).or(producedPart.part.partCode.contains(getQuery())).or(producedPart.part.name.contains(getQuery()))
					.or(producedPart.moldId.in(JPAExpressions.select(mold.id).from(mold).innerJoin(location).on(mold.locationId.eq(location.id)).innerJoin(company)
							.on(location.companyId.eq(company.id))
							.where((company.companyCode.contains(getQuery()).or(company.name.contains(getQuery()))).and(mold.deleted.isNull().or(mold.deleted.isFalse()))))));
		}

		if (!StringUtils.isEmpty(getStartDate()) && !StringUtils.isEmpty(getEndDate()))
			builder.and(producedPart.day.between(getStartDate(), getEndDate()));

		if (!StringUtils.isEmpty(getStart()) && !StringUtils.isEmpty(getEnd())) {
			if (Frequent.MONTHLY.equals(getFrequent()))
				builder.and(producedPart.month.between(getStart(), getEnd()));
			else if (Frequent.WEEKLY.equals(getFrequent()))
				builder.and(producedPart.week.between(getStart(), getEnd()));
			else if (Frequent.HOURLY.equals(getFrequent())) {
				builder.and(producedPart.hour.between(getStart(), getEnd()));
				if (getForOEE() != null && getForOEE())
					builder.and(producedPart.hour.in(JPAExpressions.select(machineOee.hour).from(machineOee).where(machineOee.hour.between(getStart(), getEnd()))));
			} else
				builder.and(producedPart.day.between(getStart(), getEnd()));
		}

		if (getRejectedRateStatus() != null)
			builder.and(producedPart.rejectedRateStatus.eq(getRejectedRateStatus()));

		if (getFrequent() != null)
			builder.and(producedPart.frequent.eq(getFrequent()));
		else
			builder.and(producedPart.frequent.eq(Frequent.DAILY));

		if (!StringUtils.isEmpty(getDay()))
			builder.and(producedPart.day.eq(getDay()));
		if (!StringUtils.isEmpty(getWeek()))
			builder.and(producedPart.week.eq(getWeek()));
		if (!StringUtils.isEmpty(getMonth()))
			builder.and(producedPart.month.eq(getMonth()));
		if (!StringUtils.isEmpty(getHour()))
			builder.and(producedPart.hour.eq(getHour()));

		return builder;
	}
}
