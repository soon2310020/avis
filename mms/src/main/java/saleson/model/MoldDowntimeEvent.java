package saleson.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.querydsl.core.annotations.QueryInit;
import lombok.*;
import saleson.common.enumeration.DowntimeStatus;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.time.Instant;

@Entity @Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MoldDowntimeEvent extends UserDateAudit {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "MOLD_ID", insertable = false, updatable = false)
    private Long moldId;

    @QueryInit("location.company")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOLD_ID")
    private Mold mold;

    private Instant lastUptime;

    private Instant confirmedAt;

    private Long downtimeSeconds;

    @Enumerated(EnumType.STRING)
    private DowntimeStatus downtimeStatus;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private Boolean latest;

    public Machine getMachine() {
        return mold == null ? null : mold.getMachine();
    }
}
