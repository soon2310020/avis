package com.stg.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.stg.utils.DateUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "campaign")
public class Campaign {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "event")
    private String event;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "image")
    private String image;

    @Column(name = "content")
    private String content;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
    }

    public void setStartTime(String startTime) {
        this.startTime = DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01, startTime + ":00");
    }

    public void setEndTime(String endTime) {
        this.endTime = DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01, endTime + ":00");
    }
}
