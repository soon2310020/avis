package vn.com.twendie.avis.data.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContractNormListId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @ManyToOne
    @JoinColumn(name = "norm_list_id", nullable = false)
    private NormList normList;

}
