package saleson.model.data.dashboard.totalPart;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductData {
    private Long productId;
    private String productName;
    private String categoryName;
    private Long partCount;
    private Double percentage;

    @QueryProjection
    public ProductData(Long productId, String productName, Long partCount) {
        this.productId = productId;
        this.productName = productName;
        this.partCount = partCount;
    }

    @QueryProjection
    public ProductData(Long productId, String productName, String categoryName, Long partCount) {
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.partCount = partCount;
    }
}
