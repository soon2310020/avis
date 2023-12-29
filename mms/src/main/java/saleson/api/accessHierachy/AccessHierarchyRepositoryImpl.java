package saleson.api.accessHierachy;

import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AccessHierarchyRepositoryImpl extends QuerydslRepositorySupport implements AccessHierarchyRepositoryCustom {

    public AccessHierarchyRepositoryImpl() {
        super(Cdata.class);
    }

/*
    @Override
    public List<Long> getFullCompanyParentId(Long companyId) {
        List<Long> res = new ArrayList<>();
        QAccessCompanyRelation accessCompanyRelation = QAccessCompanyRelation.accessCompanyRelation;
        JPQLQuery query = from(accessCompanyRelation);


        List<AccessCompanyRelation> accessCompanyRelationList = query.fetch();

        res = accessCompanyRelationList.stream().map(a -> a.getCompanyParentId()).collect(Collectors.toList());
        return res;
    }
*/
}
