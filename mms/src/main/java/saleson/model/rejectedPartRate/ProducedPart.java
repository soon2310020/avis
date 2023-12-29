package saleson.model.rejectedPartRate;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import saleson.common.enumeration.Comparison;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.RejectedRateStatus;
import saleson.model.*;
import saleson.model.data.MiniComponentData;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ProducedPart extends UserDateAudit {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "MOLD_ID", insertable = false, updatable = false)
    private Long moldId;

    @JoinColumn(name = "MOLD_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Mold mold;


    @Column(name = "PART_ID", insertable = false, updatable = false)
    private Long partId;

    @JoinColumn(name = "PART_ID")
    @ManyToOne
    private Part part;

    private Integer totalProducedAmount;

    private Integer totalRejectedAmount;

    private String hour;
    private String day;
    private String week;
    private String month;

    @Enumerated(EnumType.STRING)
    private RejectedRateStatus rejectedRateStatus;

    @OneToMany(mappedBy = "producedPart")
    private Set<RejectedPartDetails> rejectedPartDetails;

    private String editedBy;

    @Column(name = "REPORTED_BY_ID", insertable = false, updatable = false)
    private Long reportedById;

    @JoinColumn(name = "REPORTED_BY_ID")
    @ManyToOne
    private User reportedBy;

    private Double rejectedRate;

    @Transient
    private Double yieldRate;

    @Transient
    private Comparison comparison;

    private Double avgRejectedRate;

    @Enumerated(EnumType.STRING)
    private Frequent frequent;

    @Transient
    private List<Machine> machineList;

//    public Double getRejectedRate(){
//        return NumberUtils.roundOffNumber(totalRejectedAmount * 100.00 / totalProducedAmount);
//    }

    public Double getYieldRate(){
        if(getRejectedRate() == null) return null;
        return totalProducedAmount == 0 ? 0 : 100.00 - getRejectedRate();
    }

    private Machine getMachine() {
        if (mold != null) {
            return mold.getMachine();
        }
        return null;
    }

    public MiniMold getMiniMold() {
        MiniMold miniMold = new MiniMold();
        if (mold != null) {
            miniMold.setId(mold.getId());
            miniMold.setEquipmentCode(mold.getEquipmentCode());
            miniMold.setLastShotAt(mold.getLastShotAt());
        }
        return miniMold;
    }
}
