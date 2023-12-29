package saleson.api.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.supplychain.resource.base.product.dto.ProductLite;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductLiteGetIn;
import com.emoldino.api.supplychain.resource.base.product.service.ProductService;
import com.emoldino.framework.util.BeanUtils;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	@GetMapping("/lite/{id}")
	public ProductLite getLite(@PathVariable("id") Long id) {
		return BeanUtils.get(ProductService.class).getLite(id);
	}

	@GetMapping("/lite")
	public Page<ProductLite> getPageLite(ProductLiteGetIn input, Pageable pageable) {
		return BeanUtils.get(ProductService.class).getPageLite(input, pageable);
	}

}
