package vn.com.twendie.avis.api.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilterWrapper<T> {

    @JsonProperty("filter")
    private T filter;

    @NotNull
    @Positive
    @Builder.Default
    @JsonProperty("page")
    @ApiModelProperty(example = "1")
    private int page = 1;

    @NotNull
    @Positive
    @Builder.Default
    @JsonProperty("size")
    @ApiModelProperty(example = "10")
    private int size = 10;

    @Builder.Default
    @JsonProperty("sort_by")
    private List<String> sortBy = Collections.singletonList("-created_at");

}
