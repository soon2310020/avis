package saleson.api.mold.payload;

import com.emoldino.framework.enumeration.ActiveStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import saleson.common.enumeration.DateViewType;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.RangeType;
import saleson.common.enumeration.TabType;

import javax.validation.constraints.NotNull;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExportPayload {
    private List<Long> ids;
    private Integer timezoneOffsetClient;

    @NotNull
    private RangeType rangeType;
    private String time;
    private String fromDate;
    private String toDate;
    @NotNull
    private List<String> dataTypes;
    @NotNull
    private DateViewType frequency;

    private boolean isToolingForm;// format one tooling per Sheet
}
