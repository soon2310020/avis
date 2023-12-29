package saleson.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MachineMoldMatchingHistory extends UserDateAudit {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "MACHINE_ID", insertable = false, updatable = false)
    private Long machineId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MACHINE_ID")
    private Machine machine;

    @Column(name = "MOLD_ID", insertable = false, updatable = false)
    private Long moldId;


    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOLD_ID")
    private Mold mold;

    private Instant matchTime;
    private String matchDay;
    private String matchHour;

    private Instant unmatchTime;
    private String unmatchDay;
    private String unmatchHour;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean completed;


}
