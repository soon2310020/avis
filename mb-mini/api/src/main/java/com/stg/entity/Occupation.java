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
@Table(name = "occupation")
public class Occupation {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "no")
    private Integer no;

    @Column(name = "job")
    private String job;

    @Column(name = "occupation")
    private String occupation;

    //nhóm nghề
    @Column(name = "occupation_group")
    private String occupationGroup;

    @Column(name = "column1")
    private String column1;

    @Column(name = "column2")
    private String column2;

    @Column(name = "shorten")
    private String shorten;

    @Column(name = "column3")
    private String column3;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @PrePersist
    public void prePersist(){
        this.creationTime = LocalDateTime.now();
    }

}
