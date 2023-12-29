package vn.com.twendie.avis.data.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Audited
@Entity
@Getter
@Setter
@Table(name = "payment_request_item")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PaymentRequestItem extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Long price;

    @Column(name = "count", precision = 2)
    private BigDecimal count;

    @Column(name = "unit")
    private String unit;

    @Column(name = "total_price")
    private Long totalPrice;

    @ManyToOne
    @JoinColumn(name = "customer_payment_request_id", referencedColumnName = "id")
    private CustomerPaymentRequest customerPaymentRequest;

}
