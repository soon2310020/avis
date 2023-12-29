package vn.com.twendie.avis.api.mapping;

import org.javatuples.Pair;
import vn.com.twendie.avis.data.enumtype.WorkingDayEnum;
import vn.com.twendie.avis.data.model.WorkingDay;

public class WorkingDayValueMapping implements ValueMapping<String> {
    @Override
    @SuppressWarnings("unchecked")
    public String map(Object value) {
        Pair<WorkingDay, Integer> workingDayValuePair = (Pair<WorkingDay, Integer>) value;
        switch (WorkingDayEnum.valueOf(workingDayValuePair.getValue0().getId())) {
            case MON_TO_FRI:
                return "22 day (Mon-Fri)";
            case MON_TO_SAT:
                return "26 day (Mon-Sat)";
            case MON_TO_SUN:
                return "30 day (Mon-Sun)";
            case MON_TO_SAT_PLUS_2_SUN:
                return "28 day (Mon-Sat + 2Sun)";
            case FLEXIBLE:
                return String.format("%d day (Mon-Sat) Linh Hoạt", workingDayValuePair.getValue1());
        }
        return null;
    }
}
