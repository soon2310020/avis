package saleson.common.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import saleson.model.LogAuthentication;

@Repository
public interface LogAuthenticationReposigory extends JpaRepository<LogAuthentication, Long> {
}
