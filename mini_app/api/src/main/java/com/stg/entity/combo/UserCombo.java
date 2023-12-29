package com.stg.entity.combo;

import com.stg.constant.ComboCode;
import com.stg.constant.Gender;
import com.stg.entity.AbstractAuditingEntity;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_combo")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
public class UserCombo extends AbstractAuditingEntity implements Serializable {

	private static final long serialVersionUID = 1L;

    public UserCombo() {
    }
    
    public UserCombo(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @NotNull
    private String username;

//    @NotNull
    private LocalDate dob;

//    @NotNull
    @Enumerated(EnumType.STRING)
    private Gender gender;

//    @NotNull
    private BigDecimal inputAmount;

//    @NotNull
    @Enumerated(EnumType.STRING)
    private ComboCode comboCode;

    private String comboName;

    @NotNull
    @Column(length = 1024)
    private String attributes;

    @NotNull
    private Boolean raw;
}
