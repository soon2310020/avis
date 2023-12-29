package saleson.api.tabTable;

import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import saleson.model.QTabTableData;
import saleson.model.TabTableData;

import java.util.List;

@Repository
public class TabTableDataRepositoryImpl extends QuerydslRepositorySupport implements TabTableDataRepositoryCustom{

    public TabTableDataRepositoryImpl() {
        super(TabTableData.class);
    }


    @Override
    public List<Long> findAllRefIdByTabTableId(Long tabTableId) {
        QTabTableData tabTableData = QTabTableData.tabTableData;
        JPQLQuery<Long> query = from(tabTableData).select(tabTableData.refId).where(tabTableData.tabTableId.eq(tabTableId));
        return query.fetch();
    }

    @Override
    public List<TabTableData> findAllByTabTableId(Long tabTableId) {
        QTabTableData tabTableData = QTabTableData.tabTableData;
        JPQLQuery<TabTableData> query = from(tabTableData)
                .where(tabTableData.tabTableId.eq(tabTableId).and(tabTableData.refId.isNotNull()));
        return query.fetch();
    }
}
