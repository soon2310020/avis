package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.GraphType;
import saleson.common.hibernate.converter.BooleanYnConverter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private GraphType type;

    private Integer linePosition;
    @Column(name = "`ROW`")
    private Integer row;
    private Integer position;

    @Convert(converter = BooleanYnConverter.class)
    private Boolean enabled;

    public UserContent(Long userId, GraphType type, Integer linePosition, Integer row, Integer position, Boolean enabled){
        this.userId = userId;
        this.type = type;
        this.linePosition = linePosition;
        this.row = row;
        this.position = position;
        this.enabled = enabled;
    }
}
