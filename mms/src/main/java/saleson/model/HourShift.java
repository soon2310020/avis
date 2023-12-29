package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Builder
public class HourShift extends UserDateAudit {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DAY_SHIFT_ID", insertable = false, updatable = false)
    private Long dayShiftId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DAY_SHIFT_ID")
    private DayShift dayShift;

    private Long shiftNumber;

    private String start;
    private String end;

    public HourShift(Long dayShiftId, DayShift dayShift, Long shiftNumber, String start, String end) {
        this.dayShiftId = dayShiftId;
        this.dayShift = dayShift;
        this.shiftNumber = shiftNumber;
        this.start = start;
        this.end = end;
    }
}
