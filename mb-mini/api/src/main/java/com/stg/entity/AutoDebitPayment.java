package com.stg.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "auto_debit_payment")
public class AutoDebitPayment {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "customer_id")
    private Long customerId;

    @Column(length = 50)
    private String sourceId;

    @Column(length = 20)
    private String sourceType;

    @Column(length = 50)
    private String sourceNumber;

    @Column(length = 100)
    private String sourceName;

    private boolean registered;
    @Column(length = 100)
    private String statusCode;
    @Column(length = 255)
    private String statusMessage;

    //@Version
    private int version;

    @CreatedDate
    private LocalDateTime createdDate = LocalDateTime.now();
    @LastModifiedDate
    private LocalDateTime lastModifiedDate = LocalDateTime.now();
}
