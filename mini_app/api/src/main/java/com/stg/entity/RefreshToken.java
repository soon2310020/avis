package com.stg.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
public class RefreshToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String token;

    private Instant expiredDate;

    private String username;
    
    private String credentials;
}
