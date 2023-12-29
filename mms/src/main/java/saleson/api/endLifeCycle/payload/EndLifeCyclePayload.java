package saleson.api.endLifeCycle.payload;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.EndOfLifeCycleStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.PriorityType;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.QMoldEndLifeCycle;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EndLifeCyclePayload extends SearchParam {
	private String toolingId;
	private PriorityType priorityType;
	private EndOfLifeCycleStatus endOfLifeCycleStatus;
	private String year;
	private Boolean checkWorking;
	private String accumulatedShotFilter;

	public Predicate getPredicate() {
		QMoldEndLifeCycle moldEndLifeCycle = QMoldEndLifeCycle.moldEndLifeCycle;

		BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldEndLifeCycle.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		builder.and(moldEndLifeCycle.mold.equipmentStatus.eq(EquipmentStatus.INSTALLED));
		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(moldEndLifeCycle.mold.equipmentCode.like('%' + getQuery() + '%'));
		}
		if (priorityType != null) {
			builder.and(moldEndLifeCycle.priority.eq(priorityType));
		}
		if (endOfLifeCycleStatus != null) {
			builder.and(moldEndLifeCycle.status.eq(endOfLifeCycleStatus));
		} else {
			builder.and(moldEndLifeCycle.status.isNull().or(moldEndLifeCycle.status.ne(EndOfLifeCycleStatus.DISMISS)));
		}
		if (!StringUtils.isEmpty(year)) {
			builder.and(moldEndLifeCycle.year.eq(year));
		}
		//fill for 30 days
		/*if (checkWorking) {
			Instant last30Day = Instant.now().minus(30, ChronoUnit.DAYS);
			builder.and(moldEndLifeCycle.mold.operatedStartAt.before(last30Day));
		}*/

		if (getDeleted() == null) {
			builder.and(moldEndLifeCycle.mold.deleted.isFalse().or(moldEndLifeCycle.mold.deleted.isNull()));
		} else if (getDeleted()) {
			builder.and(moldEndLifeCycle.mold.deleted.isTrue());
		}

		return builder;
	}
}
