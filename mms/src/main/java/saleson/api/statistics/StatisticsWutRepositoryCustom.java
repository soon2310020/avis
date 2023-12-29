package saleson.api.statistics;

import org.springframework.data.domain.Pageable;
import saleson.api.report.payload.ProductivitySearchPayload;
import saleson.model.data.supplierReport.SupplierProductionData;

import java.util.List;

public interface StatisticsWutRepositoryCustom {
    List<SupplierProductionData> findProductionQuantity(List<Long> moldIds, ProductivitySearchPayload payload);

    List<SupplierProductionData> findProductionWUTQuantity(List<Long> moldIds, ProductivitySearchPayload payload, Pageable pageable);
}
