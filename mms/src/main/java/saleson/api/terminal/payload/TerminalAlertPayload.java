package saleson.api.terminal.payload;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.api.equipment.payload.EquipmentPayload;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.QLogUserAlert;
import saleson.model.QTerminal;
import saleson.model.QTerminalDisconnect;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TerminalAlertPayload extends EquipmentPayload {
    private String message;
    private NotificationStatus notificationStatus;
    private Boolean lastAlert;

    public Predicate getDisconnectPredicate() {
        QTerminalDisconnect terminalDisconnect = QTerminalDisconnect.terminalDisconnect;
        QTerminal terminal = QTerminal.terminal;

        BooleanBuilder builder = new BooleanBuilder();

        if(getId()!=null){
            builder.and(terminalDisconnect.terminalId.eq(getId()));
        }
        if (getOperatingStatus() != null) {
            builder.and(terminalDisconnect.terminal.operatingStatus.eq(getOperatingStatus()));
        }

        if (getNotificationStatus() != null) {
            builder.and(terminalDisconnect.notificationStatus.eq(getNotificationStatus()));
        }


        if (!StringUtils.isEmpty(getStatus())) {
            if ("alert".equalsIgnoreCase(getStatus())) {
                builder.and(terminalDisconnect.notificationStatus.eq(NotificationStatus.ALERT));
//                QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;
//                JPQLQuery query = JPAExpressions.select(logUserAlert.alertId).from(logUserAlert)
//                        .where(logUserAlert.alertType.eq(AlertType.TERMINAL_DISCONNECTED).and(logUserAlert.userId.eq(SecurityUtils.getUserId())));
//                builder.and(terminalDisconnect.id.in(query));

            } else if ("confirmed".equalsIgnoreCase(getStatus())) {
                builder.and(terminalDisconnect.notificationStatus.eq(NotificationStatus.CONFIRMED)
                        .or(terminalDisconnect.notificationStatus.eq(NotificationStatus.FIXED)));
            }
        }

		BooleanBuilder subbuilder = null;
		if (StringUtils.isEmpty(getQuery())) {
			if (!SecurityUtils.isInHouse()) {
				subbuilder = new BooleanBuilder();
				subbuilder.and(terminal.companyId.eq(SecurityUtils.getCompanyId()));
			}
		} else {
			BooleanBuilder subbuilder2 = new BooleanBuilder();
			subbuilder2.and(terminal.equipmentCode.contains(getQuery()).or(terminal.location.name.contains(getQuery())).or(terminal.location.locationCode.contains(getQuery()))
					.or(terminal.location.company.companyCode.contains(getQuery())).or(terminal.location.company.name.contains(getQuery()))
//	                  .or(terminal.operatingStatus.stringValue().containsIgnoreCase(getQuery()))
//	                  .or(terminal.equipmentStatus.stringValue().containsIgnoreCase(getQuery())))
			);
			if (!SecurityUtils.isInHouse()) {
				subbuilder = new BooleanBuilder();
				subbuilder.and(terminal.companyId.eq(SecurityUtils.getCompanyId()));
				subbuilder.and(subbuilder2);
			} else {
				subbuilder = subbuilder2;
			}
		}
		if (subbuilder != null) {
			builder.and(terminalDisconnect.terminalId.in(JPAExpressions.select(terminal.id).from(terminal).where(subbuilder)));
		}

        if(getLastAlert() != null && getLastAlert() == true){
            builder.and(terminalDisconnect.latest.isTrue());
        }

        return builder;
    }
}
