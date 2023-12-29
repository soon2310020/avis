package saleson.api.mold;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import saleson.model.MoldStandardValue;

public interface MoldStandardValueRepository extends JpaRepository<MoldStandardValue, Long> {

	MoldStandardValue findTopByMoldIdAndMonth(Long moldId, String month);

	Optional<MoldStandardValue> findOneByMoldIdAndMonthAndPeriodMonthsAndMinCdataCountAndMaxCdataCount(Long moldId, String month, int periodMonths, int minCdataCount,
			int maxCdataCount);

	void deleteAllByMoldIdIn(List<Long> moldIdList);

}
