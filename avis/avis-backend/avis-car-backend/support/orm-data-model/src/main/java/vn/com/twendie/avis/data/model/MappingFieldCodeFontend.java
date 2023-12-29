package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Audited
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mapping_field_code_fontend")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class MappingFieldCodeFontend extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "code_fontend", nullable = false, length = 100)
    private String codeFontend;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "unit", length = 100)
    private String unit;

}
