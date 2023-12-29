package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.WorkingDay;

import java.util.List;
import java.util.Optional;

public interface WorkingDayRepo extends JpaRepository<WorkingDay, Long> {

    Optional<WorkingDay> findByCode(String code);

    List<WorkingDay> findAllByDeletedFalseOrderById();

}
