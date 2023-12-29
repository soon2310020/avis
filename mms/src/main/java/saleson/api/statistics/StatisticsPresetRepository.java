package saleson.api.statistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.StatisticsPreset;

import java.util.List;

public interface StatisticsPresetRepository extends JpaRepository<StatisticsPreset, Long>, QuerydslPredicateExecutor<StatisticsPreset> {
    List<StatisticsPreset> findAllByMoldId(Long moldId);
    List<StatisticsPreset> findAllByPresetId(Integer presetId);
}
