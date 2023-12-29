package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Audited
@Getter
@Setter
@Table(name = "working_calendar")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class WorkingCalendar extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "date")
    private Date date;

    @Column(name = "is_weekend")
    private boolean weekend;

    @Column(name = "is_holiday")
    private boolean holiday;

    @ManyToOne
    @JoinColumn(name = "working_day_id", referencedColumnName = "id")
    private WorkingDay workingDay;

}
