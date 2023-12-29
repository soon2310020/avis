package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Audited
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "journey_diary_station_fee")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class JourneyDiaryStationFee extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "journey_diary_id", insertable = false, updatable = false)
    private Long journeyDiaryId;

    @Column(name = "vehicle_plate")
    private String vehiclePlate;

    @Column(name = "private_code")
    private String privateCode;

    @Column(name = "vehicle_type_name")
    private String vehicleTypeName;

    @Column(name = "bot_name")
    private String botName;

    @Column(name = "stage_name")
    private String stageName;

    @Column(name = "in_name")
    private String inName;

    @Column(name = "in_time")
    private Timestamp inTime;

    @Column(name = "out_name")
    private String outName;

    @Column(name = "out_time")
    private Timestamp outTime;

    @Column(name = "check_name")
    private String checkName;

    @Column(name = "check_time")
    private Timestamp checkTime;

    @Column(name = "km_on_stage")
    private BigDecimal kmOnStage;

    @Column(name = "fee")
    private BigDecimal fee;

    @Column(name = "note")
    private String note;

    @Column(name = "station_confirm")
    private Boolean stationConfirm;

    @Column(name = "fee_confirm")
    private Boolean feeConfirm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_diary_id", referencedColumnName = "id")
    private JourneyDiary journeyDiary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private User driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", referencedColumnName = "id")
    private Vehicle vehicle;

}
