package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Audited
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_payment_request")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CustomerPaymentRequest extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "contract_code")
    private String contractCode;

    @Column(name = "contract_type_id")
    private Long contractTypeId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "admin_name")
    private String adminName;

    @Column(name = "customer_address")
    private String customerAddress;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "vehicle_number_plate")
    private String vehicleNumberPlate;

    @Column(name = "from_date")
    private Timestamp fromDate;

    @Column(name = "to_date")
    private Timestamp toDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", referencedColumnName = "id")
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", referencedColumnName = "id")
    private User createdBy;

    @OneToMany(mappedBy = "customerPaymentRequest")
    private List<CustomerJourneyDiaryDaily> customerJourneyDiaryDailies;

    @OneToMany(mappedBy = "customerPaymentRequest")
    private List<PaymentRequestItem> paymentRequestItems;

    @Column(name = "member_customer_ids")
    private String memberCustomerIds;

//    public List<CustomerJourneyDiaryDaily> getCustomerJourneyDiaryDailies() {
//        return CollectionUtils.isEmpty(customerJourneyDiaryDailies) ? customerJourneyDiaryDailies
//                : customerJourneyDiaryDailies.stream()
//                .distinct()
//                .filter(c -> !c.isDeleted())
//                .collect(Collectors.toList());
//    }
//
//    public List<PaymentRequestItem> getPaymentRequestItems() {
//        return CollectionUtils.isEmpty(paymentRequestItems) ? paymentRequestItems
//                : paymentRequestItems.stream()
//                .distinct()
//                .filter(p -> !p.isDeleted())
//                .collect(Collectors.toList());
//    }
}
