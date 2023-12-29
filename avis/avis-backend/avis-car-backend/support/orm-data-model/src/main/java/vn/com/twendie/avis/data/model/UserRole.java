package vn.com.twendie.avis.data.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Audited
@Getter
@Setter
@Table(name = "user_role")
@Entity
public class UserRole extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

}