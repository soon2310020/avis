package com.stg.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "health_question")
public class HealthQuestion {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tiêu đề câu hỏi
    @Column(name = "title")
    private String title;

    // Nội dung câu hỏi, nội dung checkbox cam kết
    @Column(name = "content")
    private String content;

    // Loại: Question/Checkbox
    @Column(name = "type")
    private String type;

    // Lựa chọn 1 cho câu hỏi
    @Column(name = "option1")
    private String option1;

    // Lựa chọn 2 cho câu hỏi
    @Column(name = "option2")
    private String option2;

    @Column(name = "type_content")
    private String typeContent;

}
