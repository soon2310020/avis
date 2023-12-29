package vn.com.twendie.avis.mobile.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverCommonInfo {

    @JsonProperty("id")
    private long id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("user_group")
    private UserGroupDTO userGroup;

    @JsonProperty("contract")
    private ContractDTO contract;

    @JsonProperty("journey_diary")
    private JourneyDiaryDTO journeyDiary;

    @JsonProperty("status")
    private int status;

    @JsonProperty("message")
    private String message;

}
