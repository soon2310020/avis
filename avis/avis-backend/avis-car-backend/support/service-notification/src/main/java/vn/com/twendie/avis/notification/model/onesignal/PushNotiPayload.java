package vn.com.twendie.avis.notification.model.onesignal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class PushNotiPayload {

    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("contents")
    private NotiContent contents;

    @JsonProperty("include_player_ids")
    private Set<String> includePlayerIds;

    @JsonProperty("data")
    private Object data;

}
