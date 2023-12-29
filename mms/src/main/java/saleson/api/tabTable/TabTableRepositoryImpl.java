package saleson.api.tabTable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

import saleson.common.enumeration.ObjectType;
import saleson.model.QCompany;
import saleson.model.QCounter;
import saleson.model.QLocation;
import saleson.model.QMachine;
import saleson.model.QMold;
import saleson.model.QPart;
import saleson.model.QTabTable;
import saleson.model.QTabTableData;
import saleson.model.QTerminal;
import saleson.model.QUser;
import saleson.model.TabTable;

@Repository
public class TabTableRepositoryImpl extends QuerydslRepositorySupport implements TabTableRepositoryCustom{

    public TabTableRepositoryImpl() {
        super(TabTable.class);
    }


    @Override
    public List<TabTable> findAllByUserIdAndDeletedFalseAndObjectType(Long userId, ObjectType objectType) {
        QTabTable tabTable = QTabTable.tabTable;
        QTabTableData tabTableData = QTabTableData.tabTableData;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(tabTable.userId.eq(userId))
                .and(tabTable.objectType.eq(objectType))
                .and(tabTable.deleted.isFalse());
        JPQLQuery<Long> total;
		if (objectType == ObjectType.TOOLING) {
			total = JPAExpressions.select(tabTableData.refId.countDistinct())//
					.from(tabTableData)//
					.innerJoin(Q.mold).on(tabTableData.refId.eq(Q.mold.id))//
					.where(tabTable.id.eq(tabTableData.tabTableId));
			Set<EntityPathBase<?>> join = new HashSet<>();
			QueryUtils.applyMoldFilter(total, join, "COMMON");
		} else if (objectType == ObjectType.COUNTER) {
            QCounter counter = QCounter.counter;
            total = JPAExpressions
                    .select(tabTableData.refId.countDistinct())
                    .from(tabTableData).leftJoin(counter).on(tabTableData.refId.eq(counter.id)).where(tabTable.id.eq(tabTableData.tabTableId).and(counter.enabled.isTrue()));
        } else if (objectType ==ObjectType.PART) {
            QPart part = QPart.part;
            total = JPAExpressions
                    .select(tabTableData.refId.countDistinct())
                    .from(tabTableData).leftJoin(part).on(tabTableData.refId.eq(part.id)).where(tabTable.id.eq(tabTableData.tabTableId).and(part.enabled.isTrue()));
        } else if (objectType ==ObjectType.TERMINAL) {
            QTerminal terminal = QTerminal.terminal;
            total = JPAExpressions
                    .select(tabTableData.refId.countDistinct())
                    .from(tabTableData).leftJoin(terminal).on(tabTableData.refId.eq(terminal.id)).where(tabTable.id.eq(tabTableData.tabTableId).and(terminal.enabled.isTrue()));
        } else if (objectType ==ObjectType.USER) {
            QUser user = QUser.user;
            total = JPAExpressions
                    .select(tabTableData.refId.countDistinct())
                    .from(tabTableData).leftJoin(user).on(tabTableData.refId.eq(user.id)).where(tabTable.id.eq(tabTableData.tabTableId).and(user.enabled.isTrue().and(user.requested.isNull().or(user.requested.isFalse()))));
        } else if (objectType ==ObjectType.MACHINE) {
            QMachine machine = QMachine.machine;
            total = JPAExpressions
                    .select(tabTableData.refId.countDistinct())
                    .from(tabTableData).leftJoin(machine).on(tabTableData.refId.eq(machine.id)).where(tabTable.id.eq(tabTableData.tabTableId).and(machine.enabled.isTrue()));
        } else if (objectType ==ObjectType.COMPANY) {
            QCompany company = QCompany.company;
            total = JPAExpressions
                    .select(tabTableData.refId.countDistinct())
                    .from(tabTableData).leftJoin(company).on(tabTableData.refId.eq(company.id)).where(tabTable.id.eq(tabTableData.tabTableId).and(company.enabled.isTrue()));
        } else if (objectType ==ObjectType.LOCATION) {
            QLocation location = QLocation.location;
            total = JPAExpressions
                    .select(tabTableData.refId.countDistinct())
                    .from(tabTableData).leftJoin(location).on(tabTableData.refId.eq(location.id)).where(tabTable.id.eq(tabTableData.tabTableId).and(location.enabled.isTrue()));
        } else {
            total = JPAExpressions
                    .select(tabTableData.refId.countDistinct())
                    .from(tabTableData).where(tabTable.id.eq(tabTableData.tabTableId));
        }
        JPQLQuery<TabTable> query = from(tabTable).select(Projections.constructor(TabTable.class, tabTable, total))
                .where(builder);
        return query.fetch();
    }
}
