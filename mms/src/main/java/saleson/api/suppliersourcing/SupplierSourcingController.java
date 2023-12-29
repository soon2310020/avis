package saleson.api.suppliersourcing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import saleson.api.suppliersourcing.payload.SupplierSourcingGetIn;
import saleson.api.suppliersourcing.payload.SupplierSourcingGetOut;
import saleson.api.suppliersourcing.payload.SupplierSourcingPostIn;

@RestController
@RequestMapping("/api/supplier-sourcing")
public class SupplierSourcingController {
	@Autowired
	private SupplierSourcingService service;

	@GetMapping
	public SupplierSourcingGetOut getList(SupplierSourcingGetIn input) {
		return service.getList(input);
	}

	@PostMapping
	public SupplierSourcingGetOut post(SupplierSourcingGetIn params, @RequestBody SupplierSourcingPostIn input) {
		input.setProductId(params.getProductId());
		input.setPartId(params.getPartId());
		input.setPeriodType(params.getPeriodType());
		input.setPeriodValue(params.getPeriodValue());
		return service.post(input);
	}

	@PostMapping("/temp")
	public SupplierSourcingGetOut postTemp(SupplierSourcingGetIn params, @RequestBody SupplierSourcingPostIn input) {
		input.setProductId(params.getProductId());
		input.setPartId(params.getPartId());
		input.setPeriodType(params.getPeriodType());
		input.setPeriodValue(params.getPeriodValue());
		return service.postTemp(input);
	}

}
