package saleson.api.machine;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.model.MachineMoldMatchingHistory;
import saleson.model.MachineStatistics;
import saleson.model.Mold;
import saleson.model.QMachineMoldMatchingHistory;

import java.time.Instant;
import java.util.List;

public class MachineMoldMatchingHistoryRepositoryImpl extends QuerydslRepositorySupport implements MachineMoldMatchingHistoryRepositoryCustom{
    public MachineMoldMatchingHistoryRepositoryImpl() {
        super(MachineMoldMatchingHistory.class);
    }

    @Override
    public List<MachineMoldMatchingHistory> findAllByMatchTimeBetweenUnMatchTimeBetween(Instant fromDate, Instant toDate) {
        QMachineMoldMatchingHistory machineMoldMatchingHistory = QMachineMoldMatchingHistory.machineMoldMatchingHistory;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(machineMoldMatchingHistory.matchTime.between(fromDate, toDate)
                .or(machineMoldMatchingHistory.unmatchTime.between(fromDate, toDate)));
        return from(machineMoldMatchingHistory).where(builder).fetch();
    }

    @Override
    public List<Mold> findAllMoldByMatchDayOrUnMatchDayAndMachineId(String matchDay, String unMatchDay, Long machineId) {
        QMachineMoldMatchingHistory machineMoldMatchingHistory = QMachineMoldMatchingHistory.machineMoldMatchingHistory;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(machineMoldMatchingHistory.matchDay.eq(matchDay).or(machineMoldMatchingHistory.unmatchDay.eq(unMatchDay)))
                .and(machineMoldMatchingHistory.machineId.eq(machineId));
        return from(machineMoldMatchingHistory).where(builder).select(machineMoldMatchingHistory.mold).fetch();
    }

    @Override
    public List<MachineMoldMatchingHistory> findAllMatchingByMatchingDayBetweenAndMachineId(String start, String end, Long machineId) {
        QMachineMoldMatchingHistory machineMoldMatchingHistory = QMachineMoldMatchingHistory.machineMoldMatchingHistory;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Expressions.booleanTemplate("CONCAT_WS('', {0}, {1}) BETWEEN {2} AND {3}", machineMoldMatchingHistory.matchDay, machineMoldMatchingHistory.matchHour, start, end)
                        .or(Expressions.booleanTemplate("CONCAT_WS('', {0}, {1}) BETWEEN {2} AND {3}", machineMoldMatchingHistory.unmatchDay, machineMoldMatchingHistory.unmatchHour, start, end))
                        .or(Expressions.booleanTemplate("CONCAT_WS('', {0}, {1}) <= {2}", machineMoldMatchingHistory.matchDay, machineMoldMatchingHistory.matchHour, start)
                                .and(machineMoldMatchingHistory.unmatchDay.isNull().or(Expressions.booleanTemplate("CONCAT_WS('', {0}, {1}) >= {2} ", machineMoldMatchingHistory.unmatchDay, machineMoldMatchingHistory.unmatchHour, end)))))
                .and(machineMoldMatchingHistory.machineId.eq(machineId));

        return from(machineMoldMatchingHistory).where(builder).fetch();
    }
}
