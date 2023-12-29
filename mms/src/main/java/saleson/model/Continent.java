package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.ContinentName;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Continent {
    @Id
    @GeneratedValue
    private Long id;

    private String countryCode;

    @Enumerated(EnumType.STRING)
    private ContinentName continentName;

    public Continent(ContinentName continentName, String countryCode){
        this.continentName = continentName;
        this.countryCode = countryCode;
    }
}
