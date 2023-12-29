package com.stg.service.dto.quotation.validation;

import com.stg.service3rd.mbal.MbalApi3rdService;
import com.stg.service3rd.mbal.dto.resp.OccupationResp;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class OccupationValidator implements ConstraintValidator<Occupation, Integer>{

	@Autowired
	private MbalApi3rdService mbalApi3rdService;

	@Override
	public boolean isValid(Integer value, ConstraintValidatorContext context) {
		if (value == null) {
			return false;
		}
		List<OccupationResp> occupations = mbalApi3rdService.getOccupations();

		Long valueInt64 = Long.valueOf(value);
		return occupations.isEmpty() ||
				occupations.stream().anyMatch(o -> o.getActive() && valueInt64.equals(o.getId()));
	}

}
