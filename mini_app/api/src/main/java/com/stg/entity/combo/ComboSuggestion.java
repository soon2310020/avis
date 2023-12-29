package com.stg.entity.combo;

import com.stg.constant.ComboCode;
import com.stg.entity.AbstractAuditingEntity;
import com.stg.constant.FeePackage;
import com.stg.constant.FeeType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "combo_suggestion")
@Getter
@Setter
public class ComboSuggestion extends AbstractAuditingEntity implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ComboCode comboCode;

    @NotNull
    private Integer feeRank;

    @NotNull
    @Enumerated(EnumType.STRING)
    private FeePackage feePackage;

    @NotNull
    private String insuranceTerm;

    @NotNull
    @Column(length = 512)
    private String attributes;
}
