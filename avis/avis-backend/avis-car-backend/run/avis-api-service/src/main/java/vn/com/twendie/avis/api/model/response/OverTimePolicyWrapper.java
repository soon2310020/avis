package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OverTimePolicyWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("polices")
    private List<TimeUsePolicyDTO> overTimePolicies;

    @JsonProperty("group_id")
    private Integer groupId;

}
