package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

import static javax.persistence.FetchType.LAZY;

@Audited
@Table(name = "user")
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class User extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "birthdate")
    private Timestamp birthdate;

    @Column(name = "id_card")
    private String idCard;

    @Column(name = "card_type")
    private Integer cardType;

    @Column(name = "iso2")
    private String iso2;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "mobile_full")
    private String mobileFull;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "driver_licenses")
    private String driverLicenses;

    @Column(name = "driver_license_number")
    private String driverLicenseNumber;

    @Column(name = "driver_license_expiry_date")
    private Timestamp driverLicenseExpiryDate;

    @Column(name = "know_english")
    private Boolean knowEnglish;

    @Column(name = "ddt_certificate")
    private Boolean ddtCertificate;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "status")
    private Integer status;

    @Column(name = "current_journey_diary_id")
    private Long currentJourneyDiaryId;

    @Column(name = "current_contract_id")
    private Long currentContractId;

    @Column(name = "lending_contract_id")
    private Long lendingContractId;

    @Column(name = "login_times")
    @Builder.Default
    private Integer loginTimes = 0;

    @Column(name = "login_failed_times")
    @Builder.Default
    private int loginFailedTimes = 0;

    @Column(name = "in_contract")
    private Boolean inContract;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "removable")
    @Builder.Default
    private Boolean removable = false;

    @ManyToOne
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "user_role_id", referencedColumnName = "id")
    private UserRole userRole;

    @ManyToOne
    @JoinColumn(name = "user_type_id", referencedColumnName = "id")
    private UserType userType;

    @ManyToOne
    @JoinColumn(name = "user_group_id", referencedColumnName = "id")
    private UserGroup userGroup;

    @ManyToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "unit_operator_id", referencedColumnName = "id")
    private User unitOperator;

    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @OneToOne
    @JoinColumn(name = "member_customer_id", referencedColumnName = "id")
    private MemberCustomer memberCustomer;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "current_journey_diary_id", referencedColumnName = "id", insertable = false, updatable = false)
    private JourneyDiary currentJourneyDiary;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "current_contract_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Contract currentContract;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "lending_contract_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Contract lendingContract;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;

}