package com.stg.entity.quotation;

import com.stg.entity.AbstractAuditingEntity;
import com.stg.service.dto.mbal.ProductType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "quotation_product")
@Getter
@Setter
public class QuotationProduct extends AbstractAuditingEntity implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private ProductType type;

    @NotNull
    private Integer policyTerm;

    @NotNull
    private Integer premiumTerm;

    @NotNull
    private BigDecimal sumAssured;

    @ManyToOne
    @JoinColumn(name = "quotation_customer_id")
    private QuotationCustomer quotationCustomer;
}
