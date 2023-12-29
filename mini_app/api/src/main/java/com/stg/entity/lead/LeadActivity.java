package com.stg.entity.lead;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.stg.entity.AbstractAuditingEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lead_activity")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadActivity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lead_info_id")
    private LeadInfo leadInfo;
    
    @NotBlank
    private String action;
    
    @NotBlank
    private String title;
    
    private String leadName;
    
    private String customerName;
    
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String note;
}
