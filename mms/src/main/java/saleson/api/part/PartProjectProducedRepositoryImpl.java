package saleson.api.part;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQuery;

import saleson.common.enumeration.ConfigCategory;
import saleson.model.PartProjectProduced;
import saleson.model.QPart;
import saleson.model.QPartProjectProduced;
import saleson.model.data.StatisticsPartData;

public class PartProjectProducedRepositoryImpl extends QuerydslRepositorySupport implements PartProjectProducedRepositoryCustom {
    public PartProjectProducedRepositoryImpl() {
        super(PartProjectProduced.class);
    }

    @Override
    public Long getProjectProduced(Long projectId) {
        QPart part = QPart.part;
        QPartProjectProduced partProjectProduced = QPartProjectProduced.partProjectProduced;

        NumberExpression quantityRequired = new CaseBuilder().when(part.quantityRequired.isNotNull().or(part.quantityRequired.ne(0L))).then(part.quantityRequired).otherwise(1L);
        NumberExpression partProduced;
        if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
            partProduced = new CaseBuilder().when(partProjectProduced.totalProducedVal.isNotNull()).then(partProjectProduced.totalProducedVal).otherwise(0L);
        } else {
        	partProduced = new CaseBuilder().when(partProjectProduced.totalProduced.isNotNull()).then(partProjectProduced.totalProduced).otherwise(0L);
        }
        NumberExpression projectProduced = partProduced.divide(quantityRequired).castToNum(Long.class).min();

        JPQLQuery query = from(part).leftJoin(partProjectProduced).on(part.id.eq(partProjectProduced.partId));
        query.where(partProjectProduced.projectId.eq(projectId).and(part.enabled.isTrue()));
        query.select(projectProduced);
        List result = query.fetch();
        return result.isEmpty() ? 0L : (result.get(0) != null ? (long) result.get(0) : 0L);
    }

    @Override
    public List<StatisticsPartData> getPartProducedByProjectId(Long projectId, List<Long> partIds) {
        QPartProjectProduced partProjectProduced = QPartProjectProduced.partProjectProduced;
        NumberExpression partProduced;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			partProduced = new CaseBuilder().when(partProjectProduced.totalProducedVal.isNotNull()).then(partProjectProduced.totalProducedVal).otherwise(0L);
		} else {
			partProduced = new CaseBuilder().when(partProjectProduced.totalProduced.isNotNull()).then(partProjectProduced.totalProduced).otherwise(0L);
		}
		NumberExpression totalPartProduced = partProduced.sum();
        JPQLQuery query = from(partProjectProduced).where(partProjectProduced.projectId.eq(projectId));
        if (CollectionUtils.isNotEmpty(partIds)) {
            query.where(partProjectProduced.partId.in(partIds));
        }
        query.groupBy(partProjectProduced.partId);
        query.select(Projections.constructor(StatisticsPartData.class, partProjectProduced.partId, totalPartProduced.castToNum(Integer.class)));
        return query.fetch();
    }
}
