package com.stg.entity.quotation;

import com.stg.entity.AbstractAuditingEntity;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "quotation_supporter")
@Getter
@Setter
public class QuotationSupporter extends AbstractAuditingEntity implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String code;

    private String name;
    private String email;
    private String phone;

    private String branchCode;
    private String branchName;

    private String departmentCode;
    private String departmentName;

    public FlexibleCommon.ReferrerInput toDto() {
        FlexibleCommon.ReferrerInput dto = new FlexibleCommon.ReferrerInput();
        dto.setCode(this.getCode());
        dto.setName(this.getName());
        dto.setEmail(this.getEmail());
        dto.setPhoneNumber(this.getPhone());
        dto.setBranchCode(this.getBranchCode());
        dto.setBranchName(this.getBranchName());
        dto.setDepartmentCode(this.getDepartmentCode());
        dto.setDepartmentName(this.getDepartmentName());
        return dto;
    }
}