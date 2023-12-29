package saleson.api.mold;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.api.batch.payload.IdData;
import saleson.model.MoldEfficiency;
import saleson.model.QMoldEfficiency;
import saleson.model.QMoldLocation;

import java.util.List;

public class MoldEfficiencyRepositoryImpl  extends QuerydslRepositorySupport implements MoldEfficiencyRepositoryCustom {
	public MoldEfficiencyRepositoryImpl() {
		super(MoldEfficiency.class);
	}

	@Override
	public List<IdData> getAllIds(Predicate predicate) {
		QMoldEfficiency table = QMoldEfficiency.moldEfficiency;
		JPQLQuery query = from(table).where(predicate);
		query.select(Projections.constructor(IdData.class, table.moldId, table.id));
		return query.fetch();
	}
}
