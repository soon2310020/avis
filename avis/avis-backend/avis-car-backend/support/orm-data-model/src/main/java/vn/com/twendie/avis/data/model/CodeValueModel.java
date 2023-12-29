package vn.com.twendie.avis.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeValueModel {

    @NotBlank
    @JsonProperty("code")
    private String code;

    @PositiveOrZero
    @Max(value = 99999999999L, message = "number.over_limit")
    @JsonProperty("value")
    private BigDecimal value;

    @JsonIgnore
    private String link;

    @JsonProperty("id")
    private Long id;

}
