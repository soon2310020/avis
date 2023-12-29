package vn.com.twendie.avis.notification.model.onesignal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class NotiContent {

    @JsonProperty("vi")
    private String vi;

    @JsonProperty("en")
    private String en;

}
