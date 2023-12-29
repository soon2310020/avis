package saleson.api.statistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.StatisticsWut;

import java.util.List;
import java.util.Optional;

public interface StatisticsWutRepository extends JpaRepository<StatisticsWut, Long>, QuerydslPredicateExecutor<StatisticsWut>, StatisticsWutRepositoryCustom {
    List<StatisticsWut> findAllByMoldIdAndStartedAtAndValData(Long moldId,String startedAt,Boolean valData);
//    Optional<StatisticsWut> findFirstByMoldIdOrderByCreatedAtDesc(Long moldId);
    Optional<StatisticsWut> findFirstByMoldIdAndValDataOrderByStartedAtDesc(Long moldId,Boolean valData);
}
