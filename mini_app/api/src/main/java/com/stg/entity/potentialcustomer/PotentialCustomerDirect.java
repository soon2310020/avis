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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.stg.entity.AbstractAuditingEntity;
import com.stg.entity.combo.UserCombo;
import com.stg.entity.lead.LeadInfo;
import com.stg.entity.quotation.QuotationHeader;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "potential_customer_direct")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PotentialCustomerDirect extends AbstractAuditingEntity implements Serializable {

    public static final String LEAD_PREFIX = "DR";

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "potential_customer_id")
    private PotentialCustomer potentialCustomer;

    @ManyToOne
    @JoinColumn(name = "user_combo_id")
    private UserCombo userCombo;

    @OneToOne
    @JoinColumn(name = "quotation_header_id")
    private QuotationHeader quotationHeader;

    @Enumerated(EnumType.STRING)
    private DirectState state;

    @NotNull
    private Boolean raw;

    @Enumerated(EnumType.STRING)
    private AppStatus appStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_info_id")
    private LeadInfo leadInfo;

    private String applicationNumber;

    private String cifNumber;

    @OneToOne(mappedBy = "potentialCustomerDirect", fetch = FetchType.LAZY)
    private PotentialCustomerDirectSubmitStatus submitStatus;

    public String getLeadId() {
        return id != null ? LEAD_PREFIX + Long.toString(id) : null;
    }
}
