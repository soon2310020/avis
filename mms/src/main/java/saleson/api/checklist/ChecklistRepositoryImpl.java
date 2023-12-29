package saleson.api.checklist;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.model.Company;
import saleson.model.QUser;
import saleson.model.checklist.*;

import java.util.List;

public class ChecklistRepositoryImpl extends QuerydslRepositorySupport implements ChecklistRepositoryCustom{
    public ChecklistRepositoryImpl() {
        super(Checklist.class);
    }

    @Override
    public Page<Checklist> getAllOrderBySpecialField(Predicate predicate, Pageable pageable) {
        QChecklist checklist = QChecklist.checklist;
        QChecklistUser checklistUser = QChecklistUser.checklistUser;
        QChecklistCompany checklistCompany = QChecklistCompany.checklistCompany;
        QUser user = QUser.user;

        JPQLQuery query = from(checklist)
                .leftJoin(checklistUser)
                .on(checklist.id.eq(checklistUser.checklistId)
                        .and(checklistUser.id.in(JPAExpressions
                        .select(checklistUser.id.min())
                        .from(checklistUser)
                        .groupBy(checklistUser.checklistId))))
                .leftJoin(checklistCompany)
                .on(checklist.id.eq(checklistCompany.checklistId)
                        .and(checklistCompany.id.in(JPAExpressions
                        .select(checklistCompany.id.min())
                        .from(checklistCompany)
                        .groupBy(checklistCompany.checklistId))))
                .leftJoin(user).on(checklist.createdBy.eq(user.id));

        query.where(predicate);

        OrderSpecifier orderBy = checklist.id.desc();
        String property  = pageable.getSort().get().findFirst().get().getProperty();
        Boolean isAsc = pageable.getSort().getOrderFor(property).isAscending();
        if ("individualUser".equals(property)) {
            StringExpression expression = checklistUser.user.name;
            orderBy = isAsc ? expression.asc() : expression.desc();
        } else if ("assignedCompany".equals(property)) {
            StringExpression expression = checklistCompany.company.name;
            orderBy = isAsc ? expression.asc() : expression.desc();
        } else if ("creator".equals(property)) {
            StringExpression expression = user.name;
            orderBy = isAsc ? expression.asc() : expression.desc();
        }
        query.orderBy(orderBy);
        long total = query.fetchCount();

        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());
        return new PageImpl<>(query.fetch(), pageable, total);
    }

    @Override
    public List<Checklist> findAllByCompanyIdAndChecklistTypeAndObjectTypeAndEnabledIsTrue(Long companyId, ChecklistType checklistType, CheckListObjectType objectType) {
        QChecklist checklist = QChecklist.checklist;
        QChecklistCompany checklistCompany = QChecklistCompany.checklistCompany;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(checklistCompany.companyId.eq(companyId))
                .and(checklist.enabled.isTrue())
                .and(checklist.checklistType.eq(checklistType))
                .and(checklist.objectType.eq(objectType));

        JPQLQuery query = from(checklist).leftJoin(checklistCompany).on(checklist.id.eq(checklistCompany.checklistId)).where(builder);
        return query.fetch();

    }

    @Override
    public List<Checklist> findAllByCompanyContainsAndChecklistTypeAndObjectTypeAndEnabledIsTrue(Long companyId, ChecklistType checklistType, CheckListObjectType objectType) {
        QChecklist checklist = QChecklist.checklist;
        QChecklistCompany checklistCompany = QChecklistCompany.checklistCompany;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(checklist.checklistType.eq(checklistType))
                .and(checklist.objectType.eq(objectType))
                .and(checklist.enabled.isTrue())
                .and(checklistCompany.companyId.eq(companyId));

        JPQLQuery query = from(checklist)
                .join(checklistCompany).on(checklist.id.eq(checklistCompany.checklistId))
                .where(builder);
        return query.fetch();
    }
}
