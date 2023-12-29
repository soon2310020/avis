package com.stg.entity.quotation;

import com.stg.entity.AbstractAuditingEntity;
import com.stg.service.dto.mbal.AmountType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "quotation_amount")
@Getter
@Setter
public class QuotationAmount extends AbstractAuditingEntity implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private AmountType type;

    @NotNull
    private BigDecimal value;

    @NotNull
    private Integer startYear;

    @NotNull
    private Integer endYear;

    @ManyToOne
    @JoinColumn(name = "quotation_header_id")
    private QuotationHeader quotationHeader;
}
