package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Audited
@Table(name = "contract_norm_list")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ContractNormList extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @EqualsAndHashCode.Include
    private ContractNormListId id;

    @Column(name = "quota")
    private BigDecimal quota;

}
