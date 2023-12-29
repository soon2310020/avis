package vn.com.twendie.avis.notification.model.onesignal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonData {

    @JsonProperty("notification_id")
    private long notificationId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("spec_id")
    private Long specId;

}