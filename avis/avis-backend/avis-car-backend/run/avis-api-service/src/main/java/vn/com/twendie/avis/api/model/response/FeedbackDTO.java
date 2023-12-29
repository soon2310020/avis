package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDTO {

    private Long id;

    @JsonProperty("content")
    private String content;

    @JsonProperty("admin_name")
    private String adminName;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_at")
    private Timestamp createdAt;

}
