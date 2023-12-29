package com.stg.service3rd.common.logger;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

import static com.stg.service3rd.common.Constants.HostParty;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "third_party_log")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ThirdPartyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "path")
    private String path;

    @Column(name = "host_party")
    @Enumerated(EnumType.STRING)
    private HostParty hostParty;

    @Column(name = "method")
    private String method;

    @Column(name = "action")
    private String action;

    @Column(name = "code")
    private Integer code;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "error_stack")
    @Type(type = "jsonb")
    private String errorStack;

    @Column(name = "payload")
    @Type(type = "jsonb")
    private String payload;

    @Column(name = "response")
    @Type(type = "jsonb")
    private String response;

    @Column(name = "send_time")
    private LocalDateTime sendTime;

    @Column(name = "received_time")
    private LocalDateTime receivedTime;

    @Column(name = "total_time")
    private Integer totalTime;

    @Column(name = "x_request_id")
    private String xRequestId; // requestId of ThirdParty: X-Request-ID || clientMessageId

    @Column(name = "request_id")
    private String requestId; // for trace console => check DB

}
