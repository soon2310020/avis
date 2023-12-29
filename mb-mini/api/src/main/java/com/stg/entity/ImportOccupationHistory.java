package com.stg.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "import_occupation_history")
public class ImportOccupationHistory {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename")
    private String filename;

    @Column(name = "row_number")
    private Integer rowNumber;

    @Column(name = "error_row")
    private String errorRow;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Column(name = "status")
    private String status;

    @PrePersist
    public void prePersist(){
        this.creationTime = LocalDateTime.now();
    }

}
