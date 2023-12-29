package vn.com.twendie.avis.data.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Audited
@Entity
@Getter
@Setter
@Table(name = "time_use_policy")
public class TimeUsePolicy extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;


    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "start_value", nullable = true)
    private Integer startValue;

    @Column(name = "end_value", nullable = true)
    private Integer endValue;

    @Column(name = "rate", nullable = true, precision = 0)
    private Double rate;

    @Column(name = "group_id", nullable = false)
    private Integer groupId;

    @Column(name = "from_date", nullable = true)
    private Timestamp fromDate;

    @Column(name = "to_date", nullable = false)
    private Timestamp toDate;
}
