package saleson.api.endLifeCycle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.Mold;
import saleson.model.MoldEndLifeCycle;

import java.util.List;
import java.util.Optional;

public interface MoldEndLifeCycleRepository extends JpaRepository<MoldEndLifeCycle, Long>, QuerydslPredicateExecutor<MoldEndLifeCycle>, MoldEndLifeCycleRepositoryCustom {

    Optional<MoldEndLifeCycle> findFirstByMold(Mold mold);
    List<MoldEndLifeCycle> findAllByOrderByIdDesc();
    List<MoldEndLifeCycle> findByIdInOrderByIdDesc(List<Long> ids);
}
