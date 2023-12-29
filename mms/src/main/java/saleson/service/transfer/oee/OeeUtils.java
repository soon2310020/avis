package saleson.service.transfer.oee;

import java.time.Instant;
import java.util.Optional;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.BeanUtils;

import com.emoldino.framework.util.DateUtils2;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import saleson.api.machine.MachineMoldMatchingHistoryRepository;
import saleson.api.mold.MoldRepository;
import saleson.model.Machine;
import saleson.model.MachineMoldMatchingHistory;
import saleson.model.Mold;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OeeUtils {

	public static Machine getMachineByMoldId(Long moldId) {
		Mold mold = BeanUtils.get(MoldRepository.class).getOne(moldId);
		Optional<MachineMoldMatchingHistory> optional = BeanUtils.get(MachineMoldMatchingHistoryRepository.class)
				.findFirstByMoldAndMatchDayIsNotNullAndCompletedIsFalseOrderByMatchTimeDesc(mold);
		if (!optional.isPresent()) {
			return null;
		}
		Machine machine = optional.get().getMachine();
		return machine;
	}

	public static boolean checkTimeAfterMatchTime(Mold mold, String time) {
		Optional<MachineMoldMatchingHistory> optional = BeanUtils.get(MachineMoldMatchingHistoryRepository.class)
				.findFirstByMoldAndMatchDayIsNotNullAndCompletedIsFalseOrderByMatchTimeDesc(mold);
		if (!optional.isPresent()) {
			return false;
		}
		Instant lstInstant = DateUtils2.toInstant(time, DateUtils2.DatePattern.yyyyMMddHHmmss, LocationUtils.getZoneIdByMold(mold));
		if (lstInstant.compareTo(optional.get().getMatchTime()) < 0) {
			return false;
		}
		return true;
	}

}
