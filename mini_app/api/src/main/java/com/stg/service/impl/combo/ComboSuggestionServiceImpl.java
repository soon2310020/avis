package com.stg.service.impl.combo;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stg.common.HTMLtoPDFUtils;
import com.stg.constant.AgeRange;
import com.stg.constant.ComboCode;
import com.stg.constant.FeePackage;
import com.stg.constant.FeeRank;
import com.stg.constant.UserComboAttribute;
import com.stg.entity.combo.ComboSuggestion;
import com.stg.entity.combo.MicFee;
import com.stg.entity.combo.UserCombo;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.repository.ComboSuggestionRepository;
import com.stg.repository.MicFeeRepository;
import com.stg.repository.UserComboRepository;
import com.stg.service.ComboSuggestionService;
import com.stg.service.dto.combo.ComboSuggestionDto;
import com.stg.service.dto.combo.ComboSuggestionResponse;
import com.stg.service.dto.combo.UserComboSuggestionResp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ComboSuggestionServiceImpl implements ComboSuggestionService {

	private final BigDecimal TOTAL_DAY_A_YEAR = BigDecimal.valueOf(365L);
	private final BigDecimal TWO = BigDecimal.valueOf(2);
	private final BigDecimal THREE = BigDecimal.valueOf(3);

	private final BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000);
	private final BigDecimal ONE_MILLION = BigDecimal.valueOf(1000000);
	private final BigDecimal ONE_BILLION = BigDecimal.valueOf(1000000000);

	@Autowired
	private MicFeeRepository micFeeRepository;

	@Autowired
	private ComboSuggestionRepository comboSuggestionRepository;

	@Autowired
	private UserComboRepository userComboRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private HTMLtoPDFUtils HTMLtoPdfUtils;

	private String getCurrentUsername() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			return (((UserDetails) principal).getUsername());
		}
		throw new ApplicationException("", new ErrorDto(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"));
	}

	@Override
    public ResponseEntity<ComboSuggestionResponse> suggestCombo(ComboSuggestionDto dto) {
        return suggestCombo(dto, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<UserComboSuggestionResp> suggestCombos(BigDecimal inputAmount, LocalDate dob,
            List<ComboCode> comboCodes) {

        // ngày sinh => tuổi
        LocalDate curDate = LocalDate.now();
        int age = curDate.getYear() - dob.getYear();
        if (dob.getDayOfYear() > curDate.getDayOfYear()) {
            age = age - 1;
        }
        // tuổi + package + insurance_term => tổng phí mic
        int ageRange = AgeRange.getAgeRange(age);

        List<ComboSuggestion> suggestions = comboSuggestionRepository.findComboSuggestionByComboCodeAndFeeRank(
                comboCodes, FeeRank.getFeeRank(inputAmount.divide(ONE_MILLION, 0, RoundingMode.HALF_UP)));

        return suggestions.stream().map(s -> {
            Map<String, Object> suggestionAttributes;
            try {
                suggestionAttributes = objectMapper.readValue(s.getAttributes(), Map.class);
            } catch (Exception e) {
                log.error("suggestCombo: " + e.getMessage());
                return null;
            }

            List<String> insuranceTerms = Arrays.asList(s.getInsuranceTerm().split(","));
            List<MicFee> listMicFee = micFeeRepository.findMicFee(s.getFeePackage().toString(), insuranceTerms,
                    ageRange);

            BigDecimal totalMicFee = BigDecimal.ZERO;
            for (MicFee micFee : listMicFee) {
                totalMicFee = totalMicFee.add(micFee.getFee());
            }

            return new UserComboSuggestionResp(s.getComboCode(),
                    buildAttributes(s, inputAmount, totalMicFee, suggestionAttributes));
        }).filter(s -> s != null).collect(Collectors.toList());

    }

	@SuppressWarnings("unchecked")
    @Override
	public ResponseEntity<ComboSuggestionResponse> suggestCombo(ComboSuggestionDto dto, boolean isDownload) {
		ComboSuggestionResponse response = new ComboSuggestionResponse();
		dto.validate();
		//rank phí + combo_code => package, insurance_term, X0 -> X9
		List<ComboSuggestion> list = comboSuggestionRepository.findComboSuggestionByComboCodeAndFeeRank(dto.getComboCode().toString(), FeeRank.getFeeRank(dto.getInputAmount().divide(ONE_MILLION, 0, RoundingMode.HALF_UP)));
		log.info("list size: " + list.size());

		Map<String, Object> values = new HashMap<>();
		if(list.size() > 0){
			log.info("list size: " + list.get(0).getAttributes());
			try {
				values = objectMapper.readValue(list.get(0).getAttributes(), Map.class);
				log.info("X0: " + values.get("X0"));
			} catch (JsonProcessingException e) {
				log.error("suggestCombo: " + e.getMessage());
			}

		}

		//ngày sinh => tuổi
		LocalDate curDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		//convert String to LocalDate
		LocalDate dob = LocalDate.parse(dto.getDob(), formatter);
		log.info("dob: " + dob.getYear() +" "+ dob.getDayOfYear() +" "+ dob.getMonthValue());
		log.info("current: " + curDate.getYear() +" "+ curDate.getDayOfYear() +" "+ curDate.getMonthValue());
		int age = dto.getAge();

		log.info("age:" + age);
		// tuổi + package + insurance_term => tổng phí mic
		int ageRange = AgeRange.getAgeRange(age);
		List<String> insuranceTerms = Arrays.asList(list.get(0).getInsuranceTerm().split(","));
//		insuranceTerms = Arrays.asList("MAIN,BS_01".split(","));
		log.info("insuranceTerms:" + insuranceTerms.size());
		List<MicFee> listMicFee = micFeeRepository.findMicFee(list.get(0).getFeePackage().toString(), insuranceTerms, ageRange);

		log.info("listMicFee:" + listMicFee.size());
		BigDecimal totalMicFee = BigDecimal.ZERO;
		for(MicFee micFee : listMicFee){
			totalMicFee = totalMicFee.add(micFee.getFee());
		}

		log.info("totalMicFee:" + totalMicFee);

		UserCombo userCombo = toEntity(dto, totalMicFee, values, dob, list.get(0));

		log.info("getCurrentUsername: " + getCurrentUsername());

		try {
            if (isDownload) {
                Map<String, Object> attributes = new HashMap<>();
                try {
                    attributes = objectMapper.readValue(userCombo.getAttributes(), Map.class);
                    log.info("X0: " + values.get("X0"));
                } catch (JsonProcessingException e) {
                    log.info("IOException: " + e.getMessage());
                }
                response.setData(
                        HTMLtoPdfUtils.generatePdfFromHtml(dto.getComboCode().toString().toLowerCase(), attributes));
            }
			if(userCombo != null){
				response.setId(userCombo.getId());
			}
			return ResponseEntity.ok().body(response);
		} catch (IOException e) {
			log.info("IOException: "+ e.getMessage());
		}

		return ResponseEntity.ok().body(null);
	}

    private Map<String, Object> buildAttributes(ComboSuggestion comboSuggestion, BigDecimal inputAmount,
            BigDecimal totalMicFee, Map<String, Object> suggestionAttributes) {
        Map<String, Object> attributeMap = new HashMap<>();

        log.info("getPackageName: "
                + FeePackage.getPackageName(comboSuggestion.getFeePackage(), comboSuggestion.getInsuranceTerm()));
        attributeMap.put(UserComboAttribute.PACKAGE_NAME,
                FeePackage.getPackageName(comboSuggestion.getFeePackage(), comboSuggestion.getInsuranceTerm()));
        attributeMap.put(UserComboAttribute.TOTAL_FEE_YEAR, numberToformatString(inputAmount));
        attributeMap.put(UserComboAttribute.MIC_FEE_YEAR, numberToformatString(totalMicFee));
        attributeMap.put(UserComboAttribute.MIC_FEE_DAY, divideTwoAndFormat(totalMicFee, TOTAL_DAY_A_YEAR));

        BigDecimal mbalFeeYear = inputAmount.subtract(totalMicFee);
        attributeMap.put(UserComboAttribute.MBAL_FEE_YEAR, numberToformatString(mbalFeeYear));
        attributeMap.put(UserComboAttribute.MBAL_FEE_DAY, divideTwoAndFormat(mbalFeeYear, TOTAL_DAY_A_YEAR));

        Integer feePaymentTime = (Integer) suggestionAttributes.get("X0");
        attributeMap.put(UserComboAttribute.FEE_PAYMENT_TIME, feePaymentTime); // 3. fee_payment_time
        attributeMap.put(UserComboAttribute.TOTAL_FEE,
                numberToformatString(inputAmount.multiply(BigDecimal.valueOf(feePaymentTime)))); // 4.
                                                                                                 // total_fee

        BigDecimal x1 = getValue(suggestionAttributes, "X1", false);
        BigDecimal x2 = BigDecimal.valueOf((Integer) suggestionAttributes.get("X2"));
        BigDecimal a = mbalFeeYear.multiply(x2);
        BigDecimal totalBenefit = mbalFeeYear.multiply(x2).add(x1)
                .add(getValue(suggestionAttributes, "X9", false).multiply(BigDecimal.valueOf(5)));
        log.info("totalBenefit: " + totalBenefit);
        attributeMap.put(UserComboAttribute.TOTAL_BENEFIT, numberToformatString(totalBenefit)); // total_benefit
        attributeMap.put(UserComboAttribute.MBAL_BENEFIT, divideTwoAndFormat(a, THREE)); // 5. mbal_benefit
        attributeMap.put(UserComboAttribute.ACCIDENT_MBAL_BENEFIT, numberToformatString(
                divideTwo(a.multiply(TWO), THREE).add(getValue(suggestionAttributes, "X3", false)))); // 6.
        // accident_mbal_benefit
        attributeMap.put(UserComboAttribute.PUBLIC_ACCIDENT_MBAL_BENEFIT,
                numberToformatString(a.add(getValue(suggestionAttributes, "X4", false)))); // 7.
                                                                                           // public_accident_mbal_benefit

        attributeMap.put(UserComboAttribute.INPATIENT_COST, getValueAndFormat(suggestionAttributes, "X5")); // 5.
                                                                                                            // inpatient_cost
        attributeMap.put(UserComboAttribute.ACCIDENT_MEDICAL_COST, getValueAndFormat(suggestionAttributes, "X6"));// 6.
        // accident_medical_cost
        attributeMap.put(UserComboAttribute.SURGERY_COST, getValueAndFormat(suggestionAttributes, "X7"));// 7.
                                                                                                         // surgery_cost
        attributeMap.put(UserComboAttribute.BEFORE_AFTER_TREATMENT_COST, getValueAndFormat(suggestionAttributes, "X5")); // 8.
        // before_after_treatment_cost
        attributeMap.put(UserComboAttribute.OUTPATIENT_COST, getValueAndFormat(suggestionAttributes, "X9")); // 9.
                                                                                                             // outpatient_cost
        attributeMap.put(UserComboAttribute.MAX_OUTPATIENT_COST,
                numberToformatString(getValue(suggestionAttributes, "X9", false).multiply(BigDecimal.valueOf(5))));

        return attributeMap;
    }

	private UserCombo toEntity(ComboSuggestionDto dto, BigDecimal totalMicFee, Map<String, Object> values, LocalDate dob, ComboSuggestion comboSuggestion){

		Map<String, Object> attributeMap = new HashMap<>();

		log.info("getPackageName: " +FeePackage.getPackageName(comboSuggestion.getFeePackage(), comboSuggestion.getInsuranceTerm()));
		attributeMap.put(UserComboAttribute.PACKAGE_NAME, FeePackage.getPackageName(comboSuggestion.getFeePackage(), comboSuggestion.getInsuranceTerm()));
		attributeMap.put(UserComboAttribute.TOTAL_FEE_YEAR, numberToformatString(dto.getInputAmount()));
		attributeMap.put(UserComboAttribute.MIC_FEE_YEAR, numberToformatString(totalMicFee));
		attributeMap.put(UserComboAttribute.MIC_FEE_DAY, divideTwoAndFormat(totalMicFee, TOTAL_DAY_A_YEAR));

		BigDecimal mbalFeeYear = dto.getInputAmount().subtract(totalMicFee);
		attributeMap.put(UserComboAttribute.MBAL_FEE_YEAR, numberToformatString(mbalFeeYear));
		attributeMap.put(UserComboAttribute.MBAL_FEE_DAY, divideTwoAndFormat(mbalFeeYear, TOTAL_DAY_A_YEAR));

		Integer feePaymentTime = (Integer) values.get("X0");
		attributeMap.put(UserComboAttribute.FEE_PAYMENT_TIME, feePaymentTime); // 3. fee_payment_time
		attributeMap.put(UserComboAttribute.TOTAL_FEE, numberToformatString(dto.getInputAmount().multiply(BigDecimal.valueOf(feePaymentTime)))); //4. total_fee

		BigDecimal x1 = getValue(values, "X1", false);
		BigDecimal x2 = BigDecimal.valueOf((Integer) values.get("X2"));
		BigDecimal a = mbalFeeYear.multiply(x2);
		BigDecimal totalBenefit = mbalFeeYear.multiply(x2).add(x1).add(getValue(values, "X9", false).multiply(BigDecimal.valueOf(5)));
		log.info("totalBenefit: " + totalBenefit);
		attributeMap.put(UserComboAttribute.TOTAL_BENEFIT, numberToformatString(totalBenefit)); //total_benefit
		attributeMap.put(UserComboAttribute.MBAL_BENEFIT, divideTwoAndFormat(a, THREE)); //5. mbal_benefit
		attributeMap.put(UserComboAttribute.ACCIDENT_MBAL_BENEFIT, numberToformatString(divideTwo(a.multiply(TWO), THREE).add(getValue(values,"X3", false)))); //6. accident_mbal_benefit
		attributeMap.put(UserComboAttribute.PUBLIC_ACCIDENT_MBAL_BENEFIT, numberToformatString(a.add(getValue(values,"X4", false)))); //7. public_accident_mbal_benefit

		attributeMap.put(UserComboAttribute.INPATIENT_COST, getValueAndFormat(values, "X5")); //5. inpatient_cost
		attributeMap.put(UserComboAttribute.ACCIDENT_MEDICAL_COST, getValueAndFormat(values, "X6"));// 6. accident_medical_cost
		attributeMap.put(UserComboAttribute.SURGERY_COST, getValueAndFormat(values, "X7"));//7. surgery_cost
		attributeMap.put(UserComboAttribute.BEFORE_AFTER_TREATMENT_COST, getValueAndFormat(values, "X5")); //8. before_after_treatment_cost
		attributeMap.put(UserComboAttribute.OUTPATIENT_COST, getValueAndFormat(values, "X9")); //9. outpatient_cost
		attributeMap.put(UserComboAttribute.MAX_OUTPATIENT_COST, numberToformatString(getValue(values, "X9", false).multiply(BigDecimal.valueOf(5))));
		String attributes = "";
		try {
			attributes = objectMapper.writeValueAsString(attributeMap);
		} catch (JsonProcessingException e) {
			log.error("toEntity {}", e.getMessage());
		}
		UserCombo userCombo = UserCombo.builder()
				.comboCode(dto.getComboCode())
				.comboName(ComboCode.getComboName(dto.getComboCode()))
				.attributes(attributes)
				.username(getCurrentUsername())
				.dob(dob)
				.gender(dto.getGender())
				.inputAmount(dto.getInputAmount())
				.raw(Boolean.FALSE)
				.build();

		userComboRepository.save(userCombo);
		return userCombo;
	}

	private BigDecimal getValue(Map<String, Object> values, String x, boolean isDouble){
		if(isDouble){
			return BigDecimal.valueOf((Double) values.get(x));
		}
		return BigDecimal.valueOf((Integer) values.get(x));
	}

	private String getValueAndFormat(Map<String, Object> values, String x){
		return numberToformatString(BigDecimal.valueOf((Integer) values.get(x)));
	}


	private String numberToformatString(BigDecimal number){
		if(number.compareTo(ONE_BILLION) >= 0){
			int scale = 3;
			return number.divide(ONE_BILLION, scale, RoundingMode.HALF_UP).stripTrailingZeros() + " tỷ";
		} else if(number.compareTo(ONE_MILLION) >= 0){
			int scale = 1;
			if(number.remainder(ONE_MILLION).compareTo(BigDecimal.ZERO) == 0){
				scale = 0;
			}
			return number.divide(ONE_MILLION, scale, RoundingMode.HALF_UP) + " triệu";
		} else {
			return number.divide(ONE_THOUSAND, 0, RoundingMode.HALF_UP) + " nghìn";
		}
	}

	private String divideTwoAndFormat(BigDecimal d1, BigDecimal d2){
		return numberToformatString(d1.divide(d2, 0, RoundingMode.HALF_UP));
	}

	private BigDecimal divideTwo(BigDecimal d1, BigDecimal d2){
		return d1.divide(d2, 0, RoundingMode.HALF_UP);
	}
}
