package com.stg.entity.lead;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.stg.entity.AbstractAuditingEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lead_info")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadInfo extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String leadId;

    @NotBlank
    private String status;
    
    private String statusDetail;

    @OneToMany(mappedBy = "leadInfo", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
    @OrderBy("id")
    private List<LeadActivity> activities;
    
    public String getLeadStatus() {
        LeadStatus s = LeadStatus.codeOf(status);
        return s != null ? s.getDescription() : null;
    }
}
