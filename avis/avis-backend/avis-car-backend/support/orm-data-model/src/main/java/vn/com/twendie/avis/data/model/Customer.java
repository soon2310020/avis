package vn.com.twendie.avis.data.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;

@Audited
@Entity
@Getter
@Setter
@Table(name = "customer")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Customer extends BaseModel implements Serializable {

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

    @Column(name = "position")
    private String position;

    @Column(name = "mobile")
    private String mobile;

    public String getMobileFull() {
        return StringUtils.defaultString(countryCode) + StringUtils.defaultString(mobile);
    }

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "id_card")
    private String idCard;

    @Column(name = "card_type")
    private Integer cardType;

    @Column(name = "tax_code")
    private String taxCode;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_account_holder")
    private String bankAccountHolder;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_branch")
    private String bankBranch;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "iso2")
    private String iso2;

    @Column(name = "in_contract")
    private boolean inContract;

    @Column(name = "active")
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "customer_type_id", referencedColumnName = "id")
    private CustomerType customerType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "created_by_id", referencedColumnName = "id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by_id", referencedColumnName = "id")
    private User updatedBy;

}