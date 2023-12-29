package com.stg.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "question_response") // Khách hàng trả lời các câu hỏi
public class QuestionResponse {

    @EmbeddedId
    private QuestionResponseId id;

    @Column(name = "property_value", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private String propertyValue;

    @Embeddable
    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionResponseId implements Serializable {

        @Column(name="insurance_request_id", nullable = false)
        private Long insuranceRequestId;

        @Column(name = "property_key")
        private String propertyKey;
    }
}
