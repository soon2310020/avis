package com.stg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.stg.entity.user.User;
import com.stg.service.dto.mb_employee.EmployeeHisResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mb_employee_import_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MbEmployeeImportHis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "error_line")
    private String errorContent;

    @Column(name = "error_sys_detail")
    private String errorSysDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EmployeeHisResult status;

    @Column(name = "total_success")
    private int totalSuccess;

    @Column(name = "total_failed")
    private int totalFailed;

    @OneToOne
    @JoinColumn(name = "created_by")
    @JsonBackReference
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
