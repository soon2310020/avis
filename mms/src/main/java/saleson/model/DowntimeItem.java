package saleson.model;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.*;

import lombok.*;
import saleson.common.enumeration.*;
import saleson.model.support.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DowntimeItem extends UserDateAudit {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "MACHINE_STATISTICS_ID")
    private Long machineStatisticsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MACHINE_STATISTICS_ID", insertable = false, updatable = false)
    private MachineStatistics machineStatistics;

    @Enumerated(EnumType.STRING)
    private MachineAvailabilityType type;

    private Integer hourFrom;
    private Integer minuteFrom;
    private Integer hourTo;
    private Integer minuteTo;

    private String reason;
    private String note;

    public DowntimeItem(Long machineStatisticsId, MachineStatistics machineStatistics, MachineAvailabilityType type, Integer hourFrom, Integer minuteFrom, Integer hourTo, Integer minuteTo, String reason, String note) {
        this.machineStatisticsId = machineStatisticsId;
        this.machineStatistics = machineStatistics;
        this.type = type;
        this.hourFrom = hourFrom;
        this.minuteFrom = minuteFrom;
        this.hourTo = hourTo;
        this.minuteTo = minuteTo;
        this.reason = reason;
        this.note = note;
    }
}
