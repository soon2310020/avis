package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Audited
@Table(name = "journey_diary_cost_type")
@Entity
@Getter
@Setter
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class JourneyDiaryCostType extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    private Long id;

    @Column(name = "journey_diary_id", insertable = false, updatable = false)
    private Long journeyDiaryId;

    @Column(name = "image_cost_link")
    private String imageCostLink;

    @Column(name = "value")
    private BigDecimal value;

    @ManyToOne
    @JoinColumn(name = "journey_diary_id", referencedColumnName = "id", nullable = false)
    private JourneyDiary journeyDiary;

    @ManyToOne
    @JoinColumn(name = "cost_type_id", referencedColumnName = "id")
    private CostType costType;

}