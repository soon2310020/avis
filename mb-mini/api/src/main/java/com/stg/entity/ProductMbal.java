package com.stg.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_mbal")
public class ProductMbal implements Serializable {

    private static final long serialVersionUID = 2990707257882964962L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_contract_id")
    private InsuranceContract insuranceContract;

    @Column(name = "product_code")
    private String productCode; //Mã sản phẩm

    @Column(name = "product_name")
    private String productName; //Tên sản phẩm

    @Column(name = "coverage_year")
    private Integer coverageYear; //Thời hạn bảo hiểm

    @Column(name = "start_date")
    private String startDate; //Ngày bắt đầu hiệu lực

    @Column(name = "end_date")
    private String endDate; //Ngày hết hạn

    @Column(name = "sum_insured")
    private Long sumInsured; //Số tiền bảo hiểm

    @Column(name = "main")
    private boolean main;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
    }
}

