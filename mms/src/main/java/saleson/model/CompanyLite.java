package saleson.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "COMPANY")
@Data
public class CompanyLite {
    @Id
    private Long id;
    private String name;
}
