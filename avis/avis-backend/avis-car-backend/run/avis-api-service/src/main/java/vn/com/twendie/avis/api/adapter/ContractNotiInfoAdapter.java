package vn.com.twendie.avis.api.adapter;

import com.google.common.base.Function;
import lombok.AllArgsConstructor;
import vn.com.twendie.avis.api.model.projection.ContractInfoForNotiProjection;
import vn.com.twendie.avis.data.model.Notification;
import vn.com.twendie.avis.data.model.NotificationContent;

import static vn.com.twendie.avis.data.enumtype.NotificationStatusEnum.WAITING;
import static vn.com.twendie.avis.data.enumtype.NotificationTypeEnum.CONTRACT;

@AllArgsConstructor
public class ContractNotiInfoAdapter implements Function<ContractInfoForNotiProjection, Notification> {

    private NotificationContent notificationContent;

    @Override
    public Notification apply(ContractInfoForNotiProjection info) {
        return Notification.builder()
                .userId(info.getDriverId())
                .type(CONTRACT.value())
                .specId(info.getContractId())
                .notificationContent(notificationContent)
                .params(info.getContractCode())
                .status(WAITING.value())
                .build();
    }

}
