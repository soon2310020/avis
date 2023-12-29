package saleson.api.endLifeCycle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.Mold;
import saleson.model.StatisticsAccumulatingShot;

import java.util.List;
import java.util.Optional;

public interface StatisticsAccumulatingShotRepository extends JpaRepository<StatisticsAccumulatingShot, Long>, QuerydslPredicateExecutor<StatisticsAccumulatingShot> {
    List<StatisticsAccumulatingShot> findAllByMoldOrderByDayAsc(Mold mold);
    List<StatisticsAccumulatingShot> findAllByMoldAndDayLessThanEqualOrderByDayAsc(Mold mold,String day);
    List<StatisticsAccumulatingShot> findAllByMoldAndRemainingShotLessThanEqualOrderByDayAsc(Mold mold,Integer remainingShot);
    Optional<StatisticsAccumulatingShot> findFirstByMoldAndDay(Mold mold,String day);

}
