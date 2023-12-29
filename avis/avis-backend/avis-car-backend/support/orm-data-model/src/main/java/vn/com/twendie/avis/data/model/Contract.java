package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;
import vn.com.twendie.avis.api.core.annotation.Hidden;
import vn.com.twendie.avis.data.enumtype.ContractExtendStatusEnum;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static vn.com.twendie.avis.data.enumtype.WorkingDayEnum.FLEXIBLE;

@Audited
@Table(name = "contract")
@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Contract extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "prefix_code", nullable = false, length = 100)
    private String prefixCode;

    @Column(name = "suffix_code", nullable = false, length = 100)
    private String suffixCode;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "term")
    private Integer term;

    @Column(name = "vehicle_working_area")
    private String vehicleWorkingArea;

    @Column(name = "sign_date")
    private Timestamp signDate;

    @Column(name = "driver_id", insertable = false, updatable = false)
    private Long driverId;

    @Column(name = "driver_is_transferred_another")
    private Boolean driverIsTransferredAnother;

    @Column(name = "driver_is_transferred_another_at")
    private Timestamp driverIsTransferredAnotherAt;

    @Column(name = "driver_know_english")
    private Boolean driverKnowEnglish;

    @Column(name = "vehicle_id", insertable = false, updatable = false)
    private Long vehicleId;

    @Column(name = "vehicle_is_transferred_another", nullable = false)
    private Boolean vehicleIsTransferredAnother;

    @Column(name = "vehicle_is_transferred_another_at")
    private Timestamp vehicleIsTransferredAnotherAt;

    @Column(name = "parking_id")
    private Long parkingId;

    @Column(name = "from_datetime")
    private Timestamp fromDatetime;

    @Column(name = "to_datetime")
    private Timestamp toDatetime;

    @Column(name = "working_time_weekend_holiday_from")
    private Time workingTimeWeekendHolidayFrom;

    @Column(name = "working_time_weekend_holiday_to")
    private Time workingTimeWeekendHolidayTo;

    @Column(name = "time_use_policy_group_id")
    private Integer timeUsePolicyGroupId;

    @Hidden
    @Column(name = "deposit")
    private Long deposit;

    @Hidden
    @Column(name = "payment_term")
    private Integer paymentTerm;

    @Hidden
    @Column(name = "vat_toll_fee")
    private Integer vatTollFee;

    @Column(name = "is_include_empty_km")
    private Boolean includeEmptyKm;

    @Column(name = "working_time_from")
    private Time workingTimeFrom;

    @Column(name = "working_time_to")
    private Time workingTimeTo;

    @Column(name = "working_day")
    private Integer workingDayValue;

    public Integer getWorkingDayValue() {
        if (Objects.nonNull(workingDay) && workingDay.getCode().equals(FLEXIBLE.getCode())) {
            return workingDayValue;
        } else {
            return null;
        }
    }

    @Column(name = "return_vehicle_early")
    private Boolean returnVehicleEarly;

    @Column(name = "days_inform_before_return_vehicle")
    private Integer daysInformBeforeReturnVehicle;

    @Column(name = "days_inform_before_early_termination")
    private Integer daysInformBeforeEarlyTermination;

    @Column(name = "fuel_adjust_percent", precision = 5, scale = 2)
    private BigDecimal fuelAdjustPercent;

    @Hidden
    @Column(name = "penalty_rate_early_termination", precision = 5, scale = 2)
    private BigDecimal penaltyRateEarlyTermination;

    @Column(name = "date_early_termination")
    private Timestamp dateEarlyTermination;

    @Column(name = "status", nullable = false)
    private Integer status;

    public Integer getExtendStatus() {
        return ContractExtendStatusEnum.parseStatus(status, driverIsTransferredAnother, vehicleIsTransferredAnother)
                .getCode();
    }

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "include_appendix")
    private Boolean includeAppendix;

    @Column(name = "canceled_at")
    private Timestamp canceledAt;

    @ManyToOne
    @JoinColumn(name = "contract_period_type_id", referencedColumnName = "id")
    private ContractPeriodType contractPeriodType;

    @ManyToOne
    @JoinColumn(name = "contract_type_id", referencedColumnName = "id")
    private ContractType contractType;

    @ManyToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "member_customer_id", referencedColumnName = "id")
    private MemberCustomer memberCustomer;

    @ManyToOne
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private User driver;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", referencedColumnName = "id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "rental_service_type_id", referencedColumnName = "id")
    private RentalServiceType rentalServiceType;

    @ManyToOne
    @JoinColumn(name = "working_day_id", referencedColumnName = "id")
    private WorkingDay workingDay;

    @ManyToOne
    @JoinColumn(name = "fuel_type_id", referencedColumnName = "id")
    private FuelType fuelType;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", referencedColumnName = "id", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "contract")
    private Set<ContractChangeHistory> contractChangeHistories;

    @Hidden
    @OneToMany(mappedBy = "id.contractId")
    private List<ContractCostType> contractCostTypes;

    @OneToMany(mappedBy = "contract")
    private Set<ContractDriverHistory> contractDriverHistories;

    @OneToMany(mappedBy = "id.contractId")
    private List<ContractNormList> contractNormLists;

    @OneToMany(mappedBy = "contract")
    private List<JourneyDiary> journeyDiaries;

    @OneToMany(mappedBy = "contract")
    private List<JourneyDiaryDaily> journeyDiaryDailies;

    @OneToMany(mappedBy = "contract")
    private Set<LogContractNormList> logContractNormLists;

    @OneToMany(mappedBy = "contract")
    private Set<LogContractPriceCostType> logContractPriceCostTypes;

    @Transient
    private Timestamp firstJourneyDiaryDate;

}