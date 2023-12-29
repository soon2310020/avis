package vn.com.twendie.avis.data.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "otp_code")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OTPCode extends BaseModel{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private Long code;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "active")
    private Boolean active;
}
