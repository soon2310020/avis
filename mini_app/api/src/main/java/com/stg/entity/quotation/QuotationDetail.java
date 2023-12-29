package com.stg.entity.quotation;

import com.stg.entity.AbstractAuditingEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "quotation_detail")
@Getter
@Setter
public class QuotationDetail extends AbstractAuditingEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "quotation_header_id")
	private QuotationHeader quotationHeader;

	private Integer policyYear;

	private Integer insuredAge;

	private BigDecimal basePremium;

	private BigDecimal topupPremium;

	private BigDecimal withdrawal;

	private BigDecimal selectedRateBaseValue;

	private BigDecimal selectedRateTopupValue;

	private BigDecimal selectedRateAccountValue;

	private BigDecimal selectedRateSurenderValue;

	private BigDecimal committedRateBaseValue;

	private BigDecimal committedRateTopupValue;

	private BigDecimal committedRateAccountValue;

	private BigDecimal committedRateSurenderValue;

	private BigDecimal lowRateDeathBenefit;

	private BigDecimal lowRateLoyaltyBonus;

	private BigDecimal lowRateAccountValue;

	private BigDecimal lowRateSurenderValue;

	private BigDecimal highRateDeathBenefit;

	private BigDecimal highRateLoyaltyBonus;

	private BigDecimal highRateAccountValue;

	private BigDecimal highRateSurenderValue;

}
