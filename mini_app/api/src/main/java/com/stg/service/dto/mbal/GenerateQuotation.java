package com.stg.service.dto.mbal;

import com.stg.entity.quotation.QuotationCustomer;
import com.stg.entity.quotation.QuotationHeader;
import com.stg.entity.quotation.QuotationProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GenerateQuotation {

	private String quotationDate;
	private GenerateQuotationCustomer customer;
	private List<GenerateQuotationAssured> assureds;
	private GenerateQuotationProductPackage productPackage;
	private List<GenerateQuotationAdditionalProduct> additionalProducts;
	private List<GenerateQuotationAmount> amounts;
	private Boolean raiderDeductFund = false;

	public GenerateQuotation(QuotationHeader header) {
		setQuotationDate(LocalDate.now().toString());
		setCustomer(new GenerateQuotationCustomer(header.getCustomer()));
		setProductPackage(new GenerateQuotationProductPackage(header));

		List<GenerateQuotationAssured> assureds = new ArrayList<GenerateQuotationAssured>();
		List<GenerateQuotationAdditionalProduct> additionalProducts = new ArrayList<GenerateQuotationAdditionalProduct>();
		for (int i = 0; i < header.getAssureds().size(); i++) {
			QuotationCustomer assured = header.getAssureds().get(i);
			addAssured(assureds, additionalProducts, assured);
		}

		if (header.getCustomer().getQuotationHeader() == null
				&& header.getCustomer().getAdditionalProducts() != null
				&& !header.getCustomer().getAdditionalProducts().isEmpty()) {
			addAssured(assureds, additionalProducts, header.getCustomer());
		}

		setAssureds(assureds);
		setAdditionalProducts(additionalProducts);

		if (header.getAmounts() != null) {
			setAmounts(
					header.getAmounts().stream().map(a -> new GenerateQuotationAmount(a)).collect(Collectors.toList()));
		}

		setRaiderDeductFund(header.getRaiderDeductFund());
	}

	private void addAssured(List<GenerateQuotationAssured> assureds,
			List<GenerateQuotationAdditionalProduct> additionalProducts, QuotationCustomer assured) {
		GenerateQuotationAssured generateAssured = new GenerateQuotationAssured(assured);
		int index = assureds.size();
		if (index == 0) {
			generateAssured.setType(AssuredType.MAIN);
		} else {
			generateAssured.setType(AssuredType.ADDITIONAL);
		}
		assureds.add(generateAssured);

		if (assured.getAdditionalProducts() != null) {
			for (int j = 0; j < assured.getAdditionalProducts().size(); j++) {
				QuotationProduct product = assured.getAdditionalProducts().get(j);
				GenerateQuotationAdditionalProduct additionalProduct = new GenerateQuotationAdditionalProduct(index,
						product);
				additionalProducts.add(additionalProduct);
			}
		}
	}
}
