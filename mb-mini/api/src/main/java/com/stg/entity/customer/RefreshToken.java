package com.stg.entity.customer;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "refresh_token")
@Data
@Accessors(chain = true)
public class RefreshToken {
    @Id
    @Column(name = "token")
    private String token;
    @Column(name = "mb_id")
    private String mbId;
    @OneToMany(mappedBy = "refreshToken", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<AccessTokenId> accessTokenIds;
    @Column(name = "issued_at")
    private Date issuedAt;
    @Column(name = "expires_at")
    private Date expiresAt;

    public List<AccessTokenId> getAccessTokenIds() {
        if (accessTokenIds == null) {
            accessTokenIds = new ArrayList<>();
        }

        return accessTokenIds;
    }

    public RefreshToken addAccessTokenId(AccessTokenId accessTokenIds) {
        accessTokenIds.setRefreshToken(this);
        getAccessTokenIds().add(accessTokenIds);
        return this;
    }

    @Entity
    @Table(name = "access_token_id")
    @Data
    @Accessors(chain = true)
    public static class AccessTokenId {
        @Id
        @Column(name = "id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @ManyToOne
        @JoinColumn(name = "refresh_token_id", referencedColumnName = "token")
        private RefreshToken refreshToken;
        @Column(name = "ati")
        private String ati;
        @Column(name = "expires_at")
        private Date expiresAt;
    }
}
