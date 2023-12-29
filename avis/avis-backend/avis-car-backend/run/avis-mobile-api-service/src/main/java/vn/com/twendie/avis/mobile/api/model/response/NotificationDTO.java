package vn.com.twendie.avis.mobile.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import vn.com.twendie.avis.data.enumtype.NotificationSettingTypeEnum;

import java.sql.Timestamp;

@Data
@Builder
public class NotificationDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("content")
    private String content;

    @JsonProperty("type")
    private String type;

    @JsonProperty("spec_id")
    private Long specId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("type_setting")
    private NotificationSettingTypeEnum typeSetting;

    @JsonProperty("from_date")
    private Timestamp fromDate;

    @JsonProperty("to_date")
    private Timestamp toDate;


    @JsonProperty("created_at")
    private Timestamp createdAt;

}
