package saleson.api.broadcastNotification.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.QBroadcastNotification;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BroadcastNotificationPayload {
    private Boolean isRead;
    String notificationType;//alert/system note
    public Predicate getPredicate() {
        BooleanBuilder builder = new BooleanBuilder();
        QBroadcastNotification broadcastNotification=QBroadcastNotification.broadcastNotification;
        builder.and(broadcastNotification.userId.eq(SecurityUtils.getUserId()));
        if (isRead != null) {
            builder.and(broadcastNotification.isRead.eq(isRead));
        }
        if(StringUtils.isNotBlank(notificationType)){
            builder.and(broadcastNotification.notificationType.eq(notificationType));
        }

        return builder;

    }

}
