package vn.com.twendie.avis.notification.model.onesignal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettingPayload {

    @NotNull
    private boolean day;

    @NotNull
    private boolean week;

    @NotNull
    private boolean month;

}
