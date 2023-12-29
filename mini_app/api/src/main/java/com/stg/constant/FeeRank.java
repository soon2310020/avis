package com.stg.constant;

import java.math.BigDecimal;

public enum FeeRank {
	RANK1(1),
	RANK2(2),
	RANK3(3),
	RANK4(4),
	RANK5(5),
	RANK6(6);

	private int rank;
	FeeRank(int rank) {
		this.rank = rank;
	}

	public static int getFeeRank(BigDecimal feeAmount){
		int intValue = feeAmount.intValue();
		if( intValue >= 10  && intValue < 20){
			return FeeRank.RANK1.rank;
		} else if (intValue >= 20 && intValue < 30) {
			return FeeRank.RANK2.rank;
		} else if (intValue >= 30 && intValue < 40) {
			return FeeRank.RANK3.rank;
		} else if (intValue >= 40 && intValue < 70) {
			return FeeRank.RANK4.rank;
		} else if (intValue >= 70 && intValue < 100) {
			return FeeRank.RANK5.rank;
		} else if (intValue >= 100) {
			return FeeRank.RANK6.rank;
		}
		return 0;
	}

}
