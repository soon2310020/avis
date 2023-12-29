package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import saleson.common.enumeration.DayShiftType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Builder
public class DayShift extends UserDateAudit {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "LOCATION_ID", insertable = false, updatable = false)
    private Long locationId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOCATION_ID")
    private Location location;

    private Long numberOfShifts;
    private String start;
    private String end;

    @Enumerated(EnumType.STRING)
    private DayShiftType dayShiftType;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private Boolean automatic;

    @JsonIgnore
    @OneToMany(mappedBy = "dayShift")
    private List<HourShift> hourShifts = new ArrayList<>();

    public List<HourShift> getShift1() {
        return hourShifts.stream().filter(h -> h.getShiftNumber() == 1).collect(Collectors.toList());
    }

    public List<HourShift> getShift2() {
        return hourShifts.stream().filter(h -> h.getShiftNumber() == 2).collect(Collectors.toList());
    }

    public List<HourShift> getShift3() {
        return hourShifts.stream().filter(h -> h.getShiftNumber() == 3).collect(Collectors.toList());
    }

    public List<HourShift> getShift4() {
        return hourShifts.stream().filter(h -> h.getShiftNumber() == 4).collect(Collectors.toList());
    }

}
