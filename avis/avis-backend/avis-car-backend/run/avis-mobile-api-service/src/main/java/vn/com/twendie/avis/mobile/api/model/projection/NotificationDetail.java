package vn.com.twendie.avis.mobile.api.model.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public interface NotificationDetail {

    Integer getId();
    String getTitle();
    String getContent();
    String getType();

    @JsonProperty("spec_id")
    Integer getSpecId();

    String getStatus();

    @JsonProperty("created_at")
    Timestamp getCreatedAt();
}
