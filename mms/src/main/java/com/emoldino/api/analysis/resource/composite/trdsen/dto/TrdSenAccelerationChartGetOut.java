package com.emoldino.api.analysis.resource.composite.trdsen.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <pre>
{
	"shots": [ {
		"title": "1st Shot",
		"points": [ {
			"x": 3,
			"y": 30
		},
		{
			"x": 8,
			"y": 20
		},
		{
			"x": 20,
			"y": 18
		},
		{
			"x": 33,
			"y": 7
		} ]
	}, {
		"title": "2nd Shot",
		"points": [ {
			"x": 3,
			"y": 30
		},
		{
			"x": 8,
			"y": 20
		},
		{
			"x": 20,
			"y": 18
		},
		{
			"x": 33,
			"y": 7
		} ]
	} ]
}
 * </pre>
 */
@Data
@NoArgsConstructor
public class TrdSenAccelerationChartGetOut {
	@ApiModelProperty(value = "Acceleration Value of Shots")
	private List<TrdSenAccelerationChartShot> shots = new ArrayList<>();

	public void addShot(TrdSenAccelerationChartShot shot) {
		shots.add(shot);
	}

	@Data
	@NoArgsConstructor
	public static class TrdSenAccelerationChartShot {
		public TrdSenAccelerationChartShot(String title, List<TrdSenAccelerationChartPoint> points) {
			this.title = title;
			this.points = points;
		}

		@ApiModelProperty(value = "Title of a Shot")
		private String title;
		@ApiModelProperty(value = "Points of a Shot")
		private List<TrdSenAccelerationChartPoint> points = new ArrayList<>();

		public void addPoint(TrdSenAccelerationChartPoint point) {
			points.add(point);
		}
	}

	@Data
	@NoArgsConstructor
	public static class TrdSenAccelerationChartPoint {
		public TrdSenAccelerationChartPoint(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@ApiModelProperty(value = "X-axis Value")
		private double x;
		@ApiModelProperty(value = "Y-axis Value")
		private double y;
		@ApiModelProperty(value = "Measured Time")
		// Time String
		private String t;
	}
}
