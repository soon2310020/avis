package com.stg.entity;

import com.stg.service.dto.notify.NotifyState;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
//@Entity
//@Table(name = "notification")
public class Notification {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "customer_id")
    private Long customerId;

    private String title;
    private String content;

    @Enumerated(EnumType.ORDINAL)
    private NotifyState state;
    private boolean deleted;

    @CreatedDate
    private LocalDateTime createdDate = LocalDateTime.now();
    @LastModifiedDate
    private LocalDateTime lastModifiedDate = LocalDateTime.now();
}
