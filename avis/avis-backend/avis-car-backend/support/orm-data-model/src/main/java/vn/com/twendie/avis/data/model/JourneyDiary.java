package vn.com.twendie.avis.data.model;

import com.google.common.collect.Lists;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Audited
@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "journey_diary")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class JourneyDiary extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "contract_id", insertable = false, updatable = false)
    private Long contractId;

    @Column(name = "time_start")
    private Timestamp timeStart;

    @Column(name = "address_start")
    private String addressStart;

    @Column(name = "image_odo_link_start")
    private String imageOdoLinkStart;

    @Column(name = "km_odo_start")
    private BigDecimal kmOdoStart;

    @Column(name = "km_driver_start")
    private BigDecimal kmDriverStart;

    public BigDecimal getKmStart() {
        return Objects.nonNull(kmOdoStart) ? kmOdoStart : kmDriverStart;
    }

    @Column(name = "customer_name_used")
    private String customerNameUsed;

    @Column(name = "customer_department")
    private String customerDepartment;

    @Column(name = "time_customer_get_in")
    private Timestamp timeCustomerGetIn;

    @Column(name = "address_customer_get_in")
    private String addressCustomerGetIn;

    @Column(name = "image_customer_get_in_link")
    private String imageCustomerGetInLink;

    @Column(name = "image_odo_link_customer_get_in")
    private String imageOdoLinkCustomerGetIn;

    @Column(name = "km_odo_customer_get_in")
    private BigDecimal kmOdoCustomerGetIn;

    @Column(name = "km_driver_customer_get_in")
    private BigDecimal kmDriverCustomerGetIn;

    public BigDecimal getKmCustomerGetIn() {
        return Objects.nonNull(kmOdoCustomerGetIn) ? kmOdoCustomerGetIn : kmDriverCustomerGetIn;
    }

    @Column(name = "time_customer_get_out")
    private Timestamp timeCustomerGetOut;

    @Column(name = "address_customer_get_out")
    private String addressCustomerGetOut;

    @Column(name = "image_customer_get_out_link")
    private String imageCustomerGetOutLink;

    @Column(name = "image_odo_link_customer_get_out")
    private String imageOdoLinkCustomerGetOut;

    @Column(name = "km_odo_customer_get_out")
    private BigDecimal kmOdoCustomerGetOut;

    @Column(name = "km_driver_customer_get_out")
    private BigDecimal kmDriverCustomerGetOut;

    public BigDecimal getKmCustomerGetOut() {
        return Objects.nonNull(kmOdoCustomerGetOut) ? kmOdoCustomerGetOut : kmDriverCustomerGetOut;
    }

    @Column(name = "time_breakdown")
    private Timestamp timeBreakdown;

    @Column(name = "image_breakdown_link")
    private String imageBreakdownLink;

    @Column(name = "image_odo_breakdown_link")
    private String imageOdoBreakdownLink;

    @Column(name = "km_odo_breakdown")
    private BigDecimal kmOdoBreakdown;

    @Column(name = "km_driver_breakdown")
    private BigDecimal kmDriverBreakdown;

    @Column(name = "time_end")
    private Timestamp timeEnd;

    @Column(name = "address_end")
    private String addressEnd;

    @Column(name = "image_odo_link_end")
    private String imageOdoLinkEnd;

    @Column(name = "km_odo_end")
    private BigDecimal kmOdoEnd;

    @Column(name = "km_driver_end")
    private BigDecimal kmDriverEnd;

    public BigDecimal getKmEnd() {
        return Objects.nonNull(kmOdoEnd) ? kmOdoEnd : kmDriverEnd;
    }

    @Column(name = "total_km_gps")
    private BigDecimal totalKmGps;

    @Column(name = "flag_unavailable_vehicle")
    private Boolean flagUnavailableVehicle;

    @Column(name = "step")
    private Integer step;

    @Column(name = "station_fee_images", length = 1000)
    private String stationFeeImages;

    public List<String> getStationFeeImages() {
        return Lists.newArrayList(StringUtils.split(
                ObjectUtils.defaultIfNull(stationFeeImages, EMPTY), "\n"));
    }

    @Column(name = "screenshot")
    private String screenshot;

    @Column(name = "note", length = 1000)
    private String note;

    @ManyToOne
    @JoinColumn(name = "contract_id", referencedColumnName = "id")
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", referencedColumnName = "id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private User driver;

    @OneToMany(mappedBy = "journeyDiary")
    private Set<JourneyDiaryCostType> journeyDiaryCostTypes;

    @OneToMany(mappedBy = "journeyDiary")
    private Set<JourneyDiaryStationFee> journeyDiaryStationFees;

    public List<String> getRecognitionFailedOdoLinks() {
        return new ArrayList<String>() {{
            if (Objects.isNull(kmOdoStart)) {
                add(imageOdoLinkStart);
            }
            if (Objects.isNull(kmOdoCustomerGetIn)) {
                add(imageOdoLinkCustomerGetIn);
            }
            if (Objects.isNull(kmOdoCustomerGetOut)) {
                add(imageOdoLinkCustomerGetOut);
            }
            if (Objects.isNull(kmOdoEnd)) {
                add(imageOdoLinkEnd);
            }
        }};
    }

}