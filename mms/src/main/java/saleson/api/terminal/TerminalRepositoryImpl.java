package saleson.api.terminal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.CaseForEqBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.common.enumeration.OperatingStatus;
import saleson.model.Cdata;
import saleson.model.Counter;
import saleson.model.Location;
import saleson.model.QMold;
import saleson.model.QStatistics;
import saleson.model.QTerminal;
import saleson.model.Terminal;
import saleson.model.data.CounterToolingCode;
import saleson.model.data.DashboardChartData;
import saleson.model.data.TerminalData;

public class TerminalRepositoryImpl extends QuerydslRepositorySupport implements TerminalRepositoryCustom {
    public TerminalRepositoryImpl() {
        super(Cdata.class);
    }

	@Override
	public Long countCounter(Long locationId) {
		QMold mold = QMold.mold;
		JPQLQuery<Counter> query = from(Q.counter)//
				.innerJoin(mold).on(mold.counterId.eq(Q.counter.id).and(QueryUtils.isCounter()))//
				.where(//
						mold.locationId.eq(locationId)//
								.and(mold.deleted.isNull().or(mold.deleted.isFalse()))//
				);
		return query.fetchCount();
	}

    @Deprecated
    @Override
    public List<CounterToolingCode> findLastStatisticsTerminalCounter(List<String> terminalCodes){
        QStatistics statistics = QStatistics.statistics;

		JPQLQuery<CounterToolingCode> subQuery = from(statistics)//
				.innerJoin(Q.counter).on(Q.counter.equipmentCode.eq(statistics.ci).and(QueryUtils.isCounter()))//
				.where(//
						Q.counter.operatingStatus.ne(OperatingStatus.DISCONNECTED)//
								.and(statistics.id.in(JPAExpressions//
										.select(statistics.id.max())//
										.from(statistics)//
										.groupBy(statistics.ci)//
								))//
				)//
				.select(Projections.constructor(CounterToolingCode.class, statistics.ti, statistics.ci));

//		query.orderBy(JPQLQueryUtils.getOrderSpecifiers(pageable, Terminal.class));

        List<CounterToolingCode> lastCounterData = subQuery.fetch();
        List<CounterToolingCode> lastCounterTerminalData = lastCounterData;
        if (terminalCodes != null) lastCounterTerminalData = lastCounterData.stream()
            .filter(x -> terminalCodes.contains(x.getTerminalCode())).collect(Collectors.toList());
        return lastCounterTerminalData;
    }

    @Deprecated
    @Override
    public List<CounterToolingCode> findAllLastStatisticsTerminalCounter() {
        return findLastStatisticsTerminalCounter(null);
    }

	@Override
	public Page<TerminalData> findTerminalDataSortByNumberCounter(Predicate predicate, Pageable pageable) {
		Sort.Direction[] directions = { Sort.Direction.DESC };
		pageable.getSort().forEach(order -> directions[0] = order.getDirection());

		QTerminal qTerminal = QTerminal.terminal;

		NumberExpression<Long> numberOfCounter = Q.counter.id.count();
		OrderSpecifier<Long> orderSpecifier = numberOfCounter.desc();
		if (directions[0].equals(Sort.Direction.ASC)) {
			orderSpecifier = numberOfCounter.asc();
		}

		JPQLQuery<TerminalData> query = from(qTerminal)//
				.select(Projections.constructor(TerminalData.class, qTerminal, numberOfCounter))//
				.leftJoin(Q.counter).on(//
						Q.counter.lastTerminalId.eq(qTerminal.id)//
								.and(Q.counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))//
								.and(QueryUtils.isCounter())//
				)//
				.where(predicate)//
				.groupBy(qTerminal.id)//
				.orderBy(orderSpecifier);
		QueryUtils.applyPagination(query, pageable);
		QueryResults<TerminalData> results = query.fetchResults();

		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	@Deprecated
	@Override
	public List<CounterToolingCode> findTiCiSameLocation(Predicate predicate, List<String> terminalCodes) {
		QTerminal terminal = QTerminal.terminal;

		BooleanBuilder builder = new BooleanBuilder(predicate);
		if (terminalCodes != null && terminalCodes.size() > 0) {
			builder.and(terminal.equipmentCode.in(terminalCodes));
		}

		JPQLQuery<CounterToolingCode> query = from(terminal)//
				.select(Projections.constructor(CounterToolingCode.class, terminal.equipmentCode, Q.counter.equipmentCode))//
				.leftJoin(Q.counter).on(Q.counter.locationId.eq(terminal.locationId).and(QueryUtils.isCounter()))//
				.where(builder);

		return query.fetch();
	}

	@Override
	public List<CounterToolingCode> getCounterToolingCodesById(Long id) {
		QTerminal qTerminal = QTerminal.terminal;
		QMold qMold = QMold.mold;
		JPQLQuery<CounterToolingCode> query = from(qTerminal)//
				.select(Projections.constructor(CounterToolingCode.class, qTerminal.equipmentCode, Q.counter.equipmentCode, qMold.equipmentCode))//
				.innerJoin(Q.counter).on(//
						Q.counter.lastTerminalId.eq(qTerminal.id)//
								.and(Q.counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))//
								.and(QueryUtils.isCounter())//
				)//
				.leftJoin(qMold).on(//
						qMold.counterId.eq(Q.counter.id)//
								.and(qMold.deleted.isNull().or(qMold.deleted.isFalse()))//
				)//
				.where(qTerminal.id.in(id));
		return query.fetch();
	}

    @Override
    public List<Terminal> findByTerminalCodeInSorted(List<String> terminalCodes){
        QTerminal terminal = QTerminal.terminal;

        CaseForEqBuilder<String>.Cases<Integer, NumberExpression<Integer>> cases = terminal.equipmentCode.when("").then(-1);
        for(int i = 0; i < terminalCodes.size(); i++){
            cases.when(terminalCodes.get(i)).then(i);
        }
        NumberExpression<Integer> guidExpression = cases.otherwise(-1);

        JPQLQuery query = from(terminal)
                .where(terminal.equipmentCode.in(terminalCodes))
                .orderBy(guidExpression.asc());

        return query.fetch();
    }

    @Override
    public List<DashboardChartData> findImplementationStatus(DashboardFilterPayload payload){
        QTerminal terminal = QTerminal.terminal;

        if(payload != null && payload.getCompanyId() != null){
            JPQLQuery query = from(terminal)
                    .where(terminal.location.company.id.eq(payload.getCompanyId()))
                    .groupBy(terminal.operatingStatus)
                    .select(Projections.constructor(DashboardChartData.class, terminal.operatingStatus, terminal.id.count()));
            return query.fetch();
        }
        JPQLQuery query = from(terminal)
                .groupBy(terminal.location.company)
                .select(Projections.constructor(DashboardChartData.class, terminal.location.company, terminal.id.count()));
        return query.fetch();
    }

    @Override
    public List<Terminal> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QTerminal terminal = QTerminal.terminal;
        JPQLQuery query = from(terminal).where(predicate);
        NumberExpression<Integer> expression = new CaseBuilder()
                .when(terminal.operatingStatus.isNull())
                .then(0)
                .when(terminal.operatingStatus.eq(OperatingStatus.WORKING))
                .then(1)
                .when(terminal.operatingStatus.eq(OperatingStatus.IDLE))
                .then(2)
                .when(terminal.operatingStatus.eq(OperatingStatus.NOT_WORKING))
                .then(3)
                .otherwise(4);
        OrderSpecifier order = expression.desc();
        if(directions[0].equals(Sort.Direction.ASC)){
            order = expression.asc();
        }
        query.orderBy(order);
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());

        return query.fetch();
    }

    @Override
    public List<Location> findAllLocationByTerminalId(List<Long> terminalIdList) {
        QTerminal terminal = QTerminal.terminal;
        JPQLQuery<Location> query = from(terminal).select(terminal.location).where(terminal.id.in(terminalIdList));
        return query.fetch();
    }

    @Override
    public Optional<Location> findLocationByTerminalId(Long id) {
        QTerminal terminal = QTerminal.terminal;
        JPQLQuery<Location> query = from(terminal).select(terminal.location).where(terminal.id.eq(id));
        return Optional.ofNullable(query.fetchOne() == null ? null : query.fetchOne());
    }

    @Override
    public List<Long> findAllIdByPredicate(Predicate predicate) {
        QTerminal terminal = QTerminal.terminal;
        JPQLQuery query = from(terminal).where(predicate).select(terminal.id);
        return query.fetch();
    }

    @Override
    public List<Terminal> findAllOrderByConnectionStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QTerminal terminal = QTerminal.terminal;
        JPQLQuery query = from(terminal).where(predicate);
        NumberExpression<Integer> expression = new CaseBuilder()
                .when(terminal.operatingStatus.eq(OperatingStatus.WORKING))
                .then(0)
                .otherwise(1);
        OrderSpecifier order = expression.desc();
        if(directions[0].equals(Sort.Direction.ASC)){
            order = expression.asc();
        }
        query.orderBy(order);
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());

        return query.fetch();
    }
}
