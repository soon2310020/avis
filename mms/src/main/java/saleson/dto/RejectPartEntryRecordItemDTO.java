package saleson.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import saleson.model.Mold;
import saleson.model.User;
import saleson.model.data.MiniComponentData;
import saleson.model.rejectedPartRate.ProducedPart;
import saleson.model.rejectedPartRate.RejectedPartDetails;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class RejectPartEntryRecordItemDTO {
    private Long id;
    private String start;
    private String end;
    private Long shiftNumber;
    private List<MiniComponentData> moldList;

    private User reportedBy;
    private Integer totalRejectedAmount;
    private List<RejectedPartDetails> rejectedPartDetails;
    private String hour;

    @QueryProjection
    public RejectPartEntryRecordItemDTO(ProducedPart producedPart) {
        this.id = producedPart.getId();
        this.reportedBy = producedPart.getReportedBy();
        this.totalRejectedAmount = producedPart.getTotalRejectedAmount();
        if (CollectionUtils.isNotEmpty(producedPart.getRejectedPartDetails())) {

            this.rejectedPartDetails = producedPart.getRejectedPartDetails().stream()
                    .collect(Collectors.groupingBy(RejectedPartDetails::getReason))
                    .entrySet().stream().map(entry-> {
                        RejectedPartDetails rejectedPartDetail = new RejectedPartDetails();
                        rejectedPartDetail.setReason(entry.getKey());
                        Integer totalRejectedAmount = entry.getValue().stream().map(RejectedPartDetails::getRejectedAmount).reduce(Integer::sum).orElse(0);
                        if (totalRejectedAmount == 0) {
                            return null;
                        }
                        rejectedPartDetail.setRejectedAmount(totalRejectedAmount);
                        return rejectedPartDetail;
                    }).filter(Objects::nonNull).collect(Collectors.toList());
        }
        this.hour = producedPart.getHour();
    }
}
