package com.stg.entity;

import com.stg.utils.Constants;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
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
    private Constants.HOST_PARTY hostParty;

    @Column(name = "method")
    private String method;

    @Column(name = "action")
    private String action; /* additional 1 */

    @Column(name = "code")
    private Integer code;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "error_stack")
    @Type(type = "jsonb")
    private String errorStack; /* additional 2 */

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
    private Integer totalTime; /* additional 3 */

    @Column(name = "x_request_id")
    private String xRequestId; // requestId of ThirdParty: X-Request-ID || clientMessageId

    @Column(name = "request_id")
    private String requestId; // for trace console => check DB

    @Column(name = "step_process", nullable = false)
    @Enumerated(EnumType.STRING)
    private StepProcess stepProcess = StepProcess.COMPLETED;

    @Column(name = "third_party_payload")
    @Type(type = "jsonb")
    private String thirdPartyPayload;

    public enum StepProcess {
        START, COMPLETED, ERROR
    }
}
