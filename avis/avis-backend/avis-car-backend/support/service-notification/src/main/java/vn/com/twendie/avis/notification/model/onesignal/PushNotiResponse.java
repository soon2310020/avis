package vn.com.twendie.avis.notification.model.onesignal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushNotiResponse implements Serializable {

    @JsonProperty("id")
    private String id;

    @JsonProperty("recipients")
    private Integer recipients;

}
