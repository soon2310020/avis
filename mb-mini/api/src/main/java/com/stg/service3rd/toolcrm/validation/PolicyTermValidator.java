package com.stg.service3rd.toolcrm.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PolicyTermValidator implements ConstraintValidator<PolicyTerm, Object> {

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (!(value instanceof IPolicyTerm)) {
			return true;
		}

		IPolicyTerm policyTerm = (IPolicyTerm) value;

		return policyTerm.validatePolicyTerm();
	}

}
