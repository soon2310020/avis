package saleson.api.mold;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.MoldCorrective;
import saleson.model.MoldDownTime;

import java.util.List;

public interface MoldDownTimeRepository  extends JpaRepository<MoldDownTime, Long>, QuerydslPredicateExecutor<MoldDownTime> {
    List<MoldDownTime> getByMoldCorrective(MoldCorrective moldCorrective);
}
