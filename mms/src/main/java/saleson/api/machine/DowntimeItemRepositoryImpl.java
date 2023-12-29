package saleson.api.machine;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.model.DowntimeItem;

public class DowntimeItemRepositoryImpl extends QuerydslRepositorySupport implements DowntimeItemRepositoryCustom{

    public DowntimeItemRepositoryImpl() {
        super(DowntimeItem.class);
    }
}
