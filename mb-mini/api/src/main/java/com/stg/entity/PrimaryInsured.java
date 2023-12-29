package com.stg.entity;

import com.stg.utils.DateUtil;
import com.stg.utils.RelationshipPolicyHolderType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "primary_insured")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class PrimaryInsured implements Serializable {

    private static final long serialVersionUID = 2990707257882964962L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "email")
    private String email;

    @Column(name = "birthday")
    private LocalDateTime birthday;

    @Column(name = "phone_number")
    private String phone;

    // giấy tờ tùy thân
    @Column(name = "identification")
    private String identification;

    @Column(name = "id_card_type")
    private String idCardType;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "job")
    private String job;

    @Column(name = "address")
    private String address;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_request_id")
    private InsuranceRequest insuranceRequest;

    @Column(name = "insured_id")
    private String insuredId; // giá trị của mbal

    @Column(name = "customer_is_assured")
    private boolean customerIsAssured; // true = Người mua bảo hiểm cũng là người hưởng bảo hiểm chính

    @Column(name = "mbal_question_response")
    @Type(type = "jsonb")
    private String mbalQuestionResponse; // trả lời câu hỏi sức khỏe MBAL

    @Column(name = "mic_question_response")
    @Type(type = "jsonb")
    private String micQuestionResponse; // trả lời câu hỏi sức khỏe MIC

    @Column(name = "app_question_number")
    private Integer appQuestionNumber;

    @Column(name = "mini_insured_id")
    private String miniAssuredId; // giá trị của miniApp

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_with_policy_holder")
    private RelationshipPolicyHolderType relationshipWithPolicyHolder;

    @Column(name = "annual_income")
    private BigDecimal annualIncome;

    public void setBirthday(String birthday) {
        if (birthday != null) {
            this.birthday = DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01, birthday + " 00:00:00");
        }
    }

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
    }
}
