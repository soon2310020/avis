package vn.com.twendie.avis.data.model;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Audited
@Table(name = "member_customer")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class MemberCustomer extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "role", nullable = false, length = 100)
    private String role;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "mobile", length = 100)
    private String mobile;

    public String getMobileFull() {
        return StringUtils.defaultString(countryCode) + StringUtils.defaultString(mobile);
    }

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "iso2")
    private String iso2;

    @Column(name = "in_contract")
    private boolean inContract;

    @Column(name = "active")
    private boolean active;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private MemberCustomer parent;

    @OneToMany(mappedBy = "parent")
    private List<MemberCustomer> children;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;

}
