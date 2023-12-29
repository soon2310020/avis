package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.twendie.avis.data.model.OTPCode;
import vn.com.twendie.avis.data.model.User;

import javax.transaction.Transactional;

@Repository
public interface OtpCodeRepo extends JpaRepository<OTPCode, Long> {

    OTPCode findByCodeAndUserAndActiveIsTrue(Long code, User user);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "update otp_code o set o.active = false where o.user_id = :userId")
    void updateOtpActiveFalseByUser(@Param("userId") Long userId);
}
