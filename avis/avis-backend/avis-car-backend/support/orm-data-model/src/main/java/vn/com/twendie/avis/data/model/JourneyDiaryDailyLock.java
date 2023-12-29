package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Audited
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "journey_dairy_daily_lock")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class JourneyDiaryDailyLock extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "lock_time")
    private Timestamp lockTime;

    @ManyToOne
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private User updatedBy;

}
