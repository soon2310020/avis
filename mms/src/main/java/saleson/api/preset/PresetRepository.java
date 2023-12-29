package saleson.api.preset;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import saleson.common.enumeration.PresetStatus;
import saleson.model.Preset;

@Repository
public interface PresetRepository extends JpaRepository<Preset, Integer>, QuerydslPredicateExecutor<Preset> {

	List<Preset> findAllByCiAndPresetStatusOrderByIdDesc(String ci, PresetStatus presetStatus);

	Preset findFirstByCiAndPresetStatusOrderByIdDesc(String ci, PresetStatus presetStatus);

	// PresetStatus가 READY인 경우 CANCEL로 업데이트
	@Modifying
	@Query("UPDATE Preset SET presetStatus = 'CANCELED' WHERE ci = :ci AND presetStatus = 'READY'")
	void updateReadyToCancelByCi(@Param("ci") String ci);

	void deleteAllByCi(String ci);

	List<Preset> findAllByPresetStatusAndShotMissingGreaterThanAndMissingDaysIsNull(PresetStatus presetStatus, Integer shotMissing);

	List<Preset> findAllByPresetStatusAndPreset(PresetStatus presetStatus, String preset);

}
