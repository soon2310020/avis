package saleson.service.transfer;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;

import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.common.enumeration.EquipmentType;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.model.QMold;
import saleson.model.data.LogDisconnectionData;
import saleson.model.logs.LogDisconnection;
import saleson.model.logs.QLogDisconnection;

public class LogDisconnectionRepositoryImpl extends QuerydslRepositorySupport implements LogDisconnectionRepositoryCustom {
	public LogDisconnectionRepositoryImpl() {
		super(LogDisconnection.class);
	}

	@Autowired
	DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

	@Override
	public List<LogDisconnectionData> findLogDisconnectionData(DashboardFilterPayload payload) {
		QMold mold = QMold.mold;
		QLogDisconnection logDisconnection = QLogDisconnection.logDisconnection;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
		if (payload.getStartTime() != null && payload.getEndTime() != null) {
			builder.and(logDisconnection.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}
		builder.and(logDisconnection.equipmentType.eq(EquipmentType.MOLD));

		JPQLQuery query = from(logDisconnection).innerJoin(mold).on(logDisconnection.equipmentId.eq(mold.id)).where(builder.and(mold.deleted.isNull().or(mold.deleted.isFalse())))
				.orderBy(logDisconnection.createdAt.desc())
				.select(Projections.constructor(LogDisconnectionData.class, logDisconnection.createdAt, logDisconnection.equipmentId, logDisconnection.event));
		return query.fetch();
	}
}
