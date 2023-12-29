package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;

@Audited
@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_journey_diary_daily")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CustomerJourneyDiaryDaily extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "journey_diary_id")
    private Long journeyDiaryId;

    @Column(name = "date")
    private Timestamp date;

    @Column(name = "customer_name_used")
    private String customerNameUsed;

    @Column(name = "customer_department")
    private String customerDepartment;

    @Column(name = "trip_itinerary", length = 1000)
    private String tripItinerary;

    @Column(name = "km_start")
    private BigDecimal kmStart;

    @Column(name = "km_end")
    private BigDecimal kmEnd;

    @Column(name = "used_km")
    private BigDecimal usedKm;

    @Column(name = "used_km_self_drive")
    private BigDecimal usedKmSelfDrive;

    @Column(name = "working_time_gps_from")
    private Time workingTimeGpsFrom;

    @Column(name = "working_time_gps_to")
    private Time workingTimeGpsTo;

    @Column(name = "over_time")
    private Integer overTime;

    @Column(name = "over_km")
    private BigDecimal overKm;

    @Column(name = "over_km_self_drive")
    private BigDecimal overKmSelfDrive;

    @Column(name = "overnight")
    private Integer overnight;

    @Column(name = "is_holiday", nullable = false)
    private Boolean isHoliday;

    @Column(name = "is_weekend", nullable = false)
    private Boolean isWeekend;

    @Column(name = "is_self_drive", nullable = false)
    private Boolean isSelfDrive;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "vehicle_number_plate")
    private String vehicleNumberPlate;

    @Column(name = "parking_fee")
    private BigDecimal parkingFee;

    @Column(name = "tolls_fee")
    private BigDecimal tollsFee;

    @Column(name = "note")
    private String note;

    @ManyToOne
    @JoinColumn(name = "customer_payment_request_id", referencedColumnName = "id")
    private CustomerPaymentRequest customerPaymentRequest;

}
