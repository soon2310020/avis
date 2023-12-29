package com.stg.constant;

public enum AgeRange {
	LESS_THAN_ONE(1),
	ONE_TO_THREE(2),
	FOUR_TO_SIX(3),
	SEVEN_TO_NINE(4),
	TEN_TO_EIGHTEEN(5),
	NINETEEN_TO_THIRTY(6),
	THIRTY_ONE(7),
	FORTY_ONE(8),
	FIFTY_ONE(9),
	SIXTY_ONE(10);

	private int range;
	AgeRange(int range) {
		this.range = range;
	}

	public static int getAgeRange(int age){
		if(age < 1){
			return AgeRange.LESS_THAN_ONE.range;
		} else if (age >= 1 && age <= 3) {
			return AgeRange.ONE_TO_THREE.range;
		} else if (age >= 4 && age <= 6) {
			return AgeRange.FOUR_TO_SIX.range;
		} else if (age >= 7 && age <= 9) {
			return AgeRange.SEVEN_TO_NINE.range;
		} else if (age >= 10 && age <= 18) {
			return AgeRange.TEN_TO_EIGHTEEN.range;
		} else if (age >= 19 && age <= 30) {
			return AgeRange.NINETEEN_TO_THIRTY.range;
		} else if (age >= 31 && age <= 40) {
			return AgeRange.THIRTY_ONE.range;
		} else if (age >= 41 && age <= 50) {
			return AgeRange.FORTY_ONE.range;
		} else if (age >= 51 && age <= 60) {
			return AgeRange.FIFTY_ONE.range;
		} else if (age >= 61 && age <= 65) {
			return AgeRange.SIXTY_ONE.range;
		}
		return 0;
	}


}

//public class Main {
//	public static void main(String[] args) {
//		int age = 15;
//		System.out.println("AgeRange: " + AgeRange.getAgeRange(age));
//
//	}
//}
