package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedbackPayload {

    @JsonProperty("content")
    @NotBlank(message = "feedback.valid_error.content_is_blank")
    @Size(max = 1000, message = "feedback.valid_error.content_too_long")
    private String content;
}
