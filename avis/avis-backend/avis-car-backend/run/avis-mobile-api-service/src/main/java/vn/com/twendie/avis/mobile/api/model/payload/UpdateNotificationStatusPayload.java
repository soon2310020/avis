package vn.com.twendie.avis.mobile.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateNotificationStatusPayload {

    @NotNull
    @NotEmpty
    @JsonProperty("ids")
    private List<Integer> notificationId;

    @JsonProperty("status")
    private String status;
}
