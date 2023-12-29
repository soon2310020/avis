package vn.com.twendie.avis.data.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;

@Audited
@Entity
@Getter
@Setter
@Table(name = "rental_service_type")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class RentalServiceType extends BaseModel implements Serializable {

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

    @ManyToOne
    @JoinColumn(name = "contract_type_id", referencedColumnName = "id")
    private ContractType contractType;

}