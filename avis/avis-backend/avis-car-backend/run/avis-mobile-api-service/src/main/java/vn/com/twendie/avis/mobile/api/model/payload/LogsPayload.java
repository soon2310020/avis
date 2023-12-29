package vn.com.twendie.avis.mobile.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.twendie.avis.data.model.SystemLog;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogsPayload {
    public String user;
    public String ip;
    public String device="MOBILE";//MOBILE, WEB
    public String level;// ERROR, WARNING, INFO
    @JsonProperty("call_time")
    public Instant callTime;
    public String function;
    public String logs;

}
