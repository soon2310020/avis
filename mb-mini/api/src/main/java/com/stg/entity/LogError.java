package com.stg.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "log_error")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class LogError {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "method")
    private String method;

    @Column(name = "path")
    private String path;

    @Column(name = "payload")
    @Type(type = "jsonb")
    private String payload;

    @Column(name = "code")
    private int code;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "send_time")
    private LocalDateTime sendTime;

    @Column(name = "received_time")
    private LocalDateTime receivedTime;

    @Column(name = "host_party")
    private String hostParty;

    @PrePersist
    public void prePersist() {
        this.receivedTime = LocalDateTime.now();
        this.id = UUID.randomUUID().toString();
    }

}
