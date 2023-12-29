package saleson.api.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.api.tabbedFilter.payload.TabbedOverviewGeneralFilterPayload;
import saleson.model.Category;
import saleson.model.data.completionRate.CompletionRateData;
import saleson.model.data.dashboard.totalPart.ProductData;

import java.util.List;

public interface CategoryRepositoryCustom {

    Page<CompletionRateData> getCompletionRateData(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg);

    CompletionRateData getCompanyCompletionRate();

    Page<ProductData> getProductPartData(TabbedOverviewGeneralFilterPayload payload, Pageable pageable);

    Page<Category> findAllProductByPart(Long partId, List<Long> supplierId, Long moldId,Pageable pageable);

}
