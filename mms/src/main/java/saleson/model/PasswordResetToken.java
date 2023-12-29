package saleson.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@Setter
@ToString
public class PasswordResetToken {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String email;
  private String token;
  @CreatedDate
  private Instant createdAt;

  @Transient
  public boolean isExpired() {
    return ChronoUnit.MINUTES.between(createdAt, Instant.now()) > 30;
  }

}
