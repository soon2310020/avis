package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.TimeUsePolicy;

import java.util.List;

public interface TimeUsePolicyRepo extends JpaRepository<TimeUsePolicy, Integer> {

    List<TimeUsePolicy> findAllByDeletedFalseOrderById();
}
