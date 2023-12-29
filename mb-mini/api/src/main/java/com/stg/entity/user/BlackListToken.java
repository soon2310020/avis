package com.stg.entity.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "black_list_token")
public class BlackListToken {

    @Id
    @Column(name = "jti")
    private String jti;


    @Column(name = "user_id")
    private Long userId;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
    }

}
