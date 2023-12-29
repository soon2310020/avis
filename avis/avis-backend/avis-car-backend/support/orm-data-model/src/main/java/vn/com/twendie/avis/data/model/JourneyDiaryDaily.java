package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Long.MAX_VALUE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Audited
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "journey_diary_daily")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class JourneyDiaryDaily extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "journey_diary_id", insertable = false, updatable = false)
    private Long journeyDiaryId;

    @Column(name = "contract_id", insertable = false, updatable = false)
    private Long contractId;

    @Column(name = "parent_id", insertable = false, updatable = false)
    private Long parentId;

    @Column(name = "date")
    private Timestamp date;

    @Column(name = "month")
    private Timestamp month;

    @Column(name = "customer_name_used")
    private String customerNameUsed;

    @Column(name = "customer_department")
    private String customerDepartment;

    @Column(name = "trip_itinerary", length = 1000)
    private String tripItinerary;

    @Column(name = "km_start")
    private BigDecimal kmStart;

    @Column(name = "km_customer_get_in")
    private BigDecimal kmCustomerGetIn;

    @Column(name = "km_customer_get_out")
    private BigDecimal kmCustomerGetOut;

    @Column(name = "km_end")
    private BigDecimal kmEnd;

    @Column(name = "total_km")
    private BigDecimal totalKm;

    @Column(name = "empty_km")
    private BigDecimal emptyKm;

    @Column(name = "used_km")
    private BigDecimal usedKm;

    @Column(name = "used_km_self_drive")
    private BigDecimal usedKmSelfDrive;

    @Column(name = "working_time_app_from")
    private Time workingTimeAppFrom;

    @Column(name = "working_time_app_to")
    private Time workingTimeAppTo;

    @Column(name = "working_time_gps_from")
    private Time workingTimeGpsFrom;

    @Column(name = "working_time_gps_to")
    private Time workingTimeGpsTo;

    @Column(name = "over_time")
    private Long overTime;

    @Column(name = "over_km")
    private BigDecimal overKm;

    @Column(name = "over_km_self_drive")
    private BigDecimal overKmSelfDrive;

    @Column(name = "overnight")
    private Integer overnight;

    @Builder.Default
    @Column(name = "is_holiday", nullable = false)
    private Boolean isHoliday = false;

    @Builder.Default
    @Column(name = "is_weekend", nullable = false)
    private Boolean isWeekend = false;

    @Builder.Default
    @Column(name = "is_over_day", nullable = false)
    private Boolean isOverDay = false;

    @Builder.Default
    @Column(name = "is_self_drive", nullable = false)
    private Boolean isSelfDrive = false;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "vehicle_number_plate")
    private String vehicleNumberPlate;

    @Column(name = "image_cost_links", length = 1000)
    private String imageCostLinks;

    @Column(name = "image_breakdown_link", length = 1000)
    private String imageBreakdownLink;

    @Column(name = "image_odo_links", length = 1000)
    private String imageOdoLinks;

    @Column(name = "image_customer_get_in_link")
    private String imageCustomerGetInLink;

    @Column(name = "image_customer_get_out_link")
    private String imageCustomerGetOutLink;

    @Column(name = "station_fee_images", length = 1000)
    private String stationFeeImages;

    @Column(name = "confirmation_screenshot")
    private String confirmationScreenshot;

    @Builder.Default
    @Column(name = "flag_odo_recognition_failed")
    private Boolean flagOdoRecognitionFailed = false;

    @Builder.Default
    @Column(name = "flag_multi_date")
    private Boolean flagMultiDate = false;

    @Builder.Default
    @Column(name = "flag_unavailable_vehicle")
    private Boolean flagUnavailableVehicle = false;

    @Builder.Default
    @Column(name = "flag_changed_vehicle")
    private Boolean flagChangedVehicle = false;

    @Builder.Default
    @Column(name = "flag_changed_km_norm")
    private Boolean flagChangedKmNorm = false;

    @Builder.Default
    @Column(name = "flag_changed_working_day")
    private Boolean flagChangedWorkingDay = false;

    @Builder.Default
    @Column(name = "flag_created_manually")
    private Boolean flagCreatedManually = false;

    @Column(name = "flag_finished_modify")
    private Boolean flagFinishedModify;

    @Column(name = "note", length = 1000)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_diary_id", referencedColumnName = "id")
    private JourneyDiary journeyDiary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", referencedColumnName = "id")
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private User driver;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", referencedColumnName = "id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private JourneyDiaryDaily parent;

    @OneToMany(mappedBy = "parent")
    private Collection<JourneyDiaryDaily> children;

    public List<JourneyDiaryDaily> getChildren() {
        if (CollectionUtils.isEmpty(children)) {
            return Collections.emptyList();
        } else {
            return children.stream()
                    .distinct()
                    .filter(child -> !child.isDeleted())
                    .sorted(Comparator.comparingLong(jdd -> defaultIfNull(jdd.getId(), MAX_VALUE)))
                    .collect(Collectors.toList());
        }
    }

    @OneToMany(mappedBy = "journeyDiaryDaily")
    private Collection<JourneyDiaryDailyCostType> journeyDiaryDailyCostTypes;

    public List<JourneyDiaryDailyCostType> getJourneyDiaryDailyCostTypes() {
        if (CollectionUtils.isEmpty(journeyDiaryDailyCostTypes)) {
            return Collections.emptyList();
        } else {
            return journeyDiaryDailyCostTypes.stream()
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    @Transient
    private Set<JourneyDiaryStationFee> journeyDiaryStationFees;

}
