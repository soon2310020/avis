package vn.com.twendie.avis.api.converter;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.model.response.NotificationDTO;
import vn.com.twendie.avis.data.model.Notification;

@Component
public class NotificationDTOConverter extends AbstractConverter<Notification, NotificationDTO> {

    @Override
    protected NotificationDTO convert(Notification notification) {
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
                .build();
    }

}
