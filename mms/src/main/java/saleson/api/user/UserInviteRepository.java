package saleson.api.user;

import org.springframework.data.jpa.repository.JpaRepository;
import saleson.model.User;
import saleson.model.UserInvite;

import java.time.Instant;
import java.util.List;

public interface UserInviteRepository  extends JpaRepository<UserInvite, Long> {
    List<UserInvite> findByEmailIn(List<String> emails);

    List<UserInvite> findByEmail(String email);

    List<UserInvite> findByCreatedAtBetweenAndEnabled(Instant from, Instant to, boolean enabled);

    UserInvite findByEmailAndJoined(String email, Boolean joined);

    boolean existsByHashCode(Integer hashCode);

    UserInvite findByHashCode(Integer hashCode);
}
