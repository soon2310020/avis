package vn.com.twendie.avis.mobile.api.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationExtraData {

    @JsonProperty("notification_id")
    private int notificationId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("spec_id")
    private Long specId;

}
