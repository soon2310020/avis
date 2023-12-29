package com.stg.entity.potentialcustomer;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.stg.entity.AbstractAuditingEntity;
import com.stg.entity.lead.LeadInfo;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "potential_customer_refer")
@Getter
@Setter
public class PotentialCustomerRefer extends AbstractAuditingEntity implements Serializable {

    public static final String LEAD_PREFIX = "RF";
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "potential_customer_id")
    private PotentialCustomer potentialCustomer;

    @NotBlank
    private String rmCode;

    @NotBlank
    private String icCode;

    @NotBlank
    private String icFullName;

    private String icEmail;

    private String icPhoneNumber;

    @NotBlank
    private String icBranchCode;

    @NotBlank
    private String icBranchName;
    
    private String reason;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private PotentialCustomerReferState state;

    @Enumerated(EnumType.STRING)
    private AppStatus appStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_info_id")
    private LeadInfo leadInfo;
    
    public String getLeadId() {
        return id != null ? LEAD_PREFIX + Long.toString(id) : null;
    }
}
