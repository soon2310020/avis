package vn.com.twendie.avis.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.twendie.avis.data.model.TokenFirebase;

import java.util.List;

public interface TokenFirebaseRepo extends JpaRepository<TokenFirebase, Long> {

    List<TokenFirebase> findAllByUserId(Integer userId);

    boolean existsByUserIdAndToken(Integer userId, String token);

    void deleteAllByToken(String token);

}
