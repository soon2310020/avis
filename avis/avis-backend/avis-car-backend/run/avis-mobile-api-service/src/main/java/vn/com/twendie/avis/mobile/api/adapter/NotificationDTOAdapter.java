package vn.com.twendie.avis.mobile.api.adapter;

import org.apache.commons.lang3.StringUtils;
import vn.com.twendie.avis.data.model.Notification;
import vn.com.twendie.avis.mobile.api.model.response.NotificationDTO;

import java.util.function.Function;

public class NotificationDTOAdapter implements Function<Notification, NotificationDTO> {

    @Override
    public NotificationDTO apply(Notification notification) {
        String content = notification.getNotificationContent().getContent();
        try {
            content = String.format(content, StringUtils.defaultString(notification.getParams()).split(","));
        } catch (Exception ignored) {
        }
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getNotificationContent().getTitle())
                .content(content)
                .type(notification.getType())
                .specId(notification.getSpecId())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .fromDate(notification.getFromDate())
                .toDate(notification.getToDate())
                .typeSetting(notification.getTypeSetting())
                .build();
    }

}
