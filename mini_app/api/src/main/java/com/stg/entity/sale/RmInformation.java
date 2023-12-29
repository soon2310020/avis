package com.stg.entity.sale;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mbal_rm_information")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RmInformation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rmCode;
    private String icCode;
    private String fullName;
    private String phoneNumber;
    private String branchCode;
    private String branchName;
   
}
