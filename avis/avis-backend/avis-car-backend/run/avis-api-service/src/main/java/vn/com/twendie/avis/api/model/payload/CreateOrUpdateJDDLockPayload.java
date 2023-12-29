package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOrUpdateJDDLockPayload {

    @JsonProperty("lock_time")
    @NotNull(message = "error.invalid_input")
    private Timestamp lockTime;
}
