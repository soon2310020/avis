package com.stg.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_log")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class RequestLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String url;

    @NotNull
    private String method;

    @NotNull
    private Integer code;

    private String payload;

    private String response;

    private String errorMessage;

    private String errorStack;

    @NotNull
    private LocalDateTime requestTime;

    @NotNull
    private LocalDateTime responseTime;

    private String requestId;
    
    @CreatedBy
    private String createdBy;
}
