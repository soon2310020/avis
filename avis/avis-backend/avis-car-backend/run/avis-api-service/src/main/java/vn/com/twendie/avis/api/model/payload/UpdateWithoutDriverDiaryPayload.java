package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class UpdateWithoutDriverDiaryPayload {

    @JsonProperty("id")
    @NotNull(message = "error.blank_input")
    private Long id;

    @JsonProperty("journey_diary_id")
    @NotNull(message = "error.blank_input")
    private Long journeyDiaryId;

    @JsonProperty("km_start")
    @Min(value = 0, message = "jdd.error.km_start_is_invalid")
    @Max(value = 9999999, message = "jdd.error.km_start_is_invalid")
    private BigDecimal kmStart;

    @JsonProperty("km_end")
    @Min(value = 0, message = "jdd.error.km_end_is_invalid")
    @Max(value = 9999999, message = "jdd.error.km_end_is_invalid")
    private BigDecimal kmEnd;

    @Size(max = 500, message = "jdd.error.note_too_long")
    private String note;

    @JsonSetter("note")
    public void setNote(String note) {
        this.note = StringUtils.trimToNull(note);
    }

}
