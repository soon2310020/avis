package saleson.api.notification.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.NotificationType;
import saleson.common.enumeration.PageType;
import saleson.common.util.SecurityUtils;
import saleson.model.QNotification;
import saleson.model.QSystemNote;

import java.util.Arrays;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationPayload {
    private Boolean isRead;

    public Predicate getPredicate() {
        QNotification notification = QNotification.notification;
        QSystemNote systemNote = QSystemNote.systemNote;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(notification.userTarget.id.eq(SecurityUtils.getUserId()));

        if (isRead != null) {
            builder.and(notification.isRead.eq(isRead));
        }
        builder.and(notification.systemNoteFunction.eq(PageType.SUPPORT_CENTER).
                or(notification.notificationType.in(Arrays.asList(NotificationType.INVITE_USER, NotificationType.DATA_COMPLETION,
                                NotificationType.WORK_ORDER, NotificationType.WORK_ORDER_REQUEST_CHANGE,
                                NotificationType.WORK_ORDER_CM_REQUEST_APPROVAL, NotificationType.WORK_ORDER_COMPLETE_REQUEST_APPROVAL,
                                NotificationType.WORK_ORDER_CM_REQUEST_APPROVAL_APPROVED, NotificationType.WORK_ORDER_CM_REQUEST_APPROVAL_DECLINED,
                                NotificationType.WORK_ORDER_APPROVAL_APPROVED, NotificationType.WORK_ORDER_APPROVAL_CANCELLED, NotificationType.WORK_ORDER_APPROVAL_REJECTED,
                                NotificationType.WORK_ORDER_DECLINED, NotificationType.WORK_ORDER_REJECTED_CHANGE,
                                NotificationType.WORK_ORDER_APPROVED_CHANGE, NotificationType.WORK_ORDER_CANCELLED,
                        NotificationType.DATA_REQUEST_ASSIGN, NotificationType.DATA_COMPLETION_ASSIGN,
                        NotificationType.DATA_REQUEST_3DAY_BEFORE_DUE_DATE,  NotificationType.DATA_REQUEST_OVERDUE_REQUESTEE,
                        NotificationType.DATA_REQUEST_CANCEL,  NotificationType.DATA_COMPLETION_CANCEL,
                        NotificationType.DATA_REQUEST_DECLINE,  NotificationType.DATA_REQUEST_COMPLETED,
                        NotificationType.DATA_REQUEST_OVERDUE_REQUESTER,  NotificationType.DATA_REQUEST_REOPEN)))
                        .or(notification.systemNoteId.in(JPAExpressions.select(systemNote.id).from(systemNote).where(systemNote.deleted.isFalse()))));

        return builder;

    }

}
