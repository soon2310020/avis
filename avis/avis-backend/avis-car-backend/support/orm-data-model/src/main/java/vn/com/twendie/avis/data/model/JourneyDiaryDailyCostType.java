package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Audited
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "journey_diary_daily_cost_type")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class JourneyDiaryDailyCostType extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "journey_diary_daily_id", insertable = false, updatable = false)
    private Long journeyDiaryDailyId;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "image_cost_link", length = 1000)
    private String imageCostLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_diary_daily_id", nullable = false)
    private JourneyDiaryDaily journeyDiaryDaily;

    @ManyToOne
    @JoinColumn(name = "cost_type_id", nullable = false)
    private CostType costType;

}
