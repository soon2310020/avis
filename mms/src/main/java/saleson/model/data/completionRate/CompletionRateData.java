package saleson.model.data.completionRate;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import saleson.common.util.DateUtils;
import saleson.model.User;
import saleson.service.util.NumberUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class CompletionRateData {
    private Long id;
    private String code;
    private String name;
    private Integer numEntered;
    private Double rate;
    private String requestTo;
    private List<User> requestedTo;
    private String updatedOn;
    private Object data;

    @QueryProjection
    public CompletionRateData(Long id, String code, String name,Integer numEntered, Double rate) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.rate = NumberUtils.roundToOneDecimalDigit(rate * 100);
        this.numEntered =numEntered;
    }

    @QueryProjection
    public CompletionRateData(Long id, String code, String name,Integer numEntered, Double rate, String requestTo) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.rate = NumberUtils.roundToOneDecimalDigit(rate * 100);
        this.numEntered =numEntered;
        this.requestTo = requestTo;
    }

    @QueryProjection
    public CompletionRateData(Long id, Double rate) {
        this.id = id;
        this.rate = NumberUtils.roundToOneDecimalDigit(rate * 100);
    }

    @QueryProjection
    public CompletionRateData(Long id, Double rate, Instant updatedOn) {
        this.id = id;
        this.rate = NumberUtils.roundToOneDecimalDigit(rate * 100);
        this.updatedOn = DateUtils.getDate(updatedOn, "yyyy-MM-dd");
    }

    @QueryProjection
    public CompletionRateData(Long id, String code, String name,Integer numEntered, Double rate, Instant updatedOn) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.rate = NumberUtils.roundToOneDecimalDigit(rate * 100);
        this.numEntered =numEntered;
        this.updatedOn = DateUtils.getDate(updatedOn, "yyyy-MM-dd");
    }

    @QueryProjection
    public CompletionRateData(Long id, String code, String name,Integer numEntered, Double rate, Instant updatedOn, Object data) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.rate = NumberUtils.roundToOneDecimalDigit(rate * 100);
        this.numEntered =numEntered;
        this.updatedOn = DateUtils.getDate(updatedOn, "yyyy-MM-dd");
        this.data = data;
    }

    public String getFirstRequestedUserName() {
        return CollectionUtils.isEmpty(requestedTo) ? "" : requestedTo.get(0).getName();
    }
}
