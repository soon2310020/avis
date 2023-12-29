package com.emoldino.api.analysis.resource.composite.ovrutl.util;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingUtilizationStatus;

public class OvrUtlUtils {

	public static ToolingUtilizationStatus setLifeCycleStatus(double utilizationRate) {
		if (utilizationRate < 40) {
			return ToolingUtilizationStatus.LOW;
		} else if (utilizationRate >= 40 && utilizationRate < 80) {
			return ToolingUtilizationStatus.MEDIUM;
		} else if (utilizationRate >= 80 && utilizationRate <= 100) {
			return ToolingUtilizationStatus.HIGH;
		} else if (utilizationRate > 100) {
			return ToolingUtilizationStatus.PROLONGED;
		}
		return ToolingUtilizationStatus.LOW;
	}

	public static int setUtiliztionRate(int accumShotCount, int forcastMaxShot) {
		double utilizationRate = ((double) accumShotCount / (double) forcastMaxShot) * 100.0;
		return (int) Math.round(utilizationRate);
	}

}
