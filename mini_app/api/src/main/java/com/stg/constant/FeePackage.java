package com.stg.constant;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public enum FeePackage {
	COPPER, SILVER, GOLD, PLATINUM, DIAMOND;

	public static String getPackageName(FeePackage feePackage, String insuranceTerm){
		if(FeePackage.COPPER.equals(feePackage)){
			return checkOutPatient("Gói Đồng", insuranceTerm);
		} else if (FeePackage.SILVER.equals(feePackage)) {
			return checkOutPatient("Gói Bạc", insuranceTerm);
		} else if (FeePackage.GOLD.equals(feePackage)) {
			return checkOutPatient("Gói Vàng", insuranceTerm);
		} else if (FeePackage.PLATINUM.equals(feePackage)) {
			return checkOutPatient("Gói Bạch Kim", insuranceTerm);
		} else if (FeePackage.DIAMOND.equals(feePackage)) {
			return checkOutPatient("Gói Kim Cương", insuranceTerm);
		} else {
			return "";
		}
	}

	static String checkOutPatient(String packageName, String insuranceTerm){
		log.info("packageName: {}, insuranceTerm: {}", packageName, insuranceTerm);
		List<String> insuranceTerms = Arrays.asList(insuranceTerm.split(","));
		if(insuranceTerms.contains("BS_01")) {
			log.info("insuranceTerms: BS_01");
			return packageName + " + Ngoại trú";
		}
		return packageName;
	}
}
