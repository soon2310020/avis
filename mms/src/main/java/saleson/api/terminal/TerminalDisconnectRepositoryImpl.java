package saleson.api.terminal;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.api.batch.payload.IdData;
import saleson.common.enumeration.OperatingStatus;
import saleson.model.*;

import java.util.List;

public class TerminalDisconnectRepositoryImpl extends QuerydslRepositorySupport implements TerminalDisconnectRepositoryCustom {
    public TerminalDisconnectRepositoryImpl() {
        super(TerminalDisconnect.class);
    }

    @Override
    public List<TerminalDisconnect> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QTerminalDisconnect terminalDisconnect = QTerminalDisconnect.terminalDisconnect;
        QTerminal terminal = terminalDisconnect.terminal;
        JPQLQuery query = from(terminalDisconnect).where(predicate);
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
    public List<IdData> getAllIds(Predicate predicate) {
        QTerminalDisconnect table = QTerminalDisconnect.terminalDisconnect;
        JPQLQuery query = from(table).where(predicate);
        query.select(Projections.constructor(IdData.class, table.terminalId, table.id));
        return query.fetch();
    }

    @Override
    public List<TerminalDisconnect> findAllOrderByAlertDate(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QTerminalDisconnect terminalDisconnect = QTerminalDisconnect.terminalDisconnect;
        JPQLQuery query = from(terminalDisconnect).where(predicate);
        OrderSpecifier order = terminalDisconnect.createdAt.desc();
        if(directions[0].equals(Sort.Direction.ASC)){
            order = terminalDisconnect.createdAt.asc();
        }
        query.orderBy(order);
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());

        return query.fetch();
    }
}
