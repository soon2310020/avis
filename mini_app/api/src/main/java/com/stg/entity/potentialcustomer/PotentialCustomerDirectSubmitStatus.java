package com.stg.entity.potentialcustomer;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.stg.entity.AbstractAuditingEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "potential_customer_direct_submit_status")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PotentialCustomerDirectSubmitStatus extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "potential_customer_direct_id")
    private PotentialCustomerDirect potentialCustomerDirect;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DirectSubmitStatus status;

    private Integer errorHttpCode;

    private String errorMessage;

    private Integer retry;

}
