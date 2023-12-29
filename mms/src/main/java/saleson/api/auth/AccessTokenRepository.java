package saleson.api.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import saleson.model.AccessToken;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken,Long>, QuerydslPredicateExecutor<AccessToken> {
    AccessToken findByAccessToken(String accessToken);
    void deleteAllByAccessToken(String accessToken);
    List<AccessToken> findAllByUserIdEqualsAndExpiredTimeGreaterThanEqualAndDeviceTokenIsNotNullAndDeviceTypeIsNotNull(Long userId, Instant currentDate);
    List<AccessToken> findAllByUserIdEqualsAndCreatedAtGreaterThanEqualAndDeviceTokenIsNotNullAndDeviceTypeIsNotNull(Long userId, Instant validDate);

}
