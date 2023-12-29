package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.NormList;

import java.util.Optional;

public interface NormListRepo extends JpaRepository<NormList, Long> {

    Optional<NormList> findByCode(String code);

}
