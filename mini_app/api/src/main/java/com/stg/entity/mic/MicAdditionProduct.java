package com.stg.entity.mic;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "mic_addition_product")
public class MicAdditionProduct implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String maSp;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "nhom", updatable = false, insertable = false)
//    private MicPackage micPackage;
    private int nhom; /*benefit*/

    @Column(length = 5)
    private String bs1;
    @Column(length = 5)
    private String bs2;
    @Column(length = 5)
    private String bs3;
    @Column(length = 5)
    private String bs4;

    /**/
    @CreatedDate
    private LocalDateTime createdDate = LocalDateTime.now();
    @LastModifiedDate
    private LocalDateTime lastModifiedDate = LocalDateTime.now();
}
