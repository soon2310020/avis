package com.stg.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.stg.entity.customer.CustomerDetail;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.stg.entity.customer.Customer;
import com.stg.utils.Constants;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "insurance_request")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)

public class InsuranceRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type")
    private Constants.PackageType packageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_package_id")
    private InsurancePackage insurancePackage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbal_package_id")
    private MbalPackage mbalPackage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mic_package_id")
    private MicPackage micPackage;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Column(name = "status")
    private boolean status;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "question_response", joinColumns = @JoinColumn(name = "insurance_request_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value", columnDefinition = "jsonb")
    @Fetch(FetchMode.SUBSELECT)
    @Type(type = "jsonb")
    private Map<QuestionName, String> questionResponse;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "illustration_number")
    private IllustrationTable illustrationTable;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_detail_id")
    private CustomerDetail customerDetail;

    @Column(name = "crm_tool_uuid")
    private UUID crmToolUuid;

    @Column(name = "process_id")
    private Long processId;

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
    }

}
