package saleson.model.data;

import saleson.common.enumeration.ColorCode;

public class AlertCount {
	private String key;
	private String title;
	private Long count;

	private ColorCode colorCode;


	public AlertCount(String key, String title, Long count) {
		this.key = key;
		this.title = title;
		this.count = count;
	}

	public AlertCount(String key, String title, Long count, ColorCode colorCode) {
		this.key = key;
		this.title = title;
		this.count = count;
		this.colorCode = colorCode;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public ColorCode getColorCode() {
		return colorCode;
	}

	public void setColorCode(ColorCode colorCode) {
		this.colorCode = colorCode;
	}
}
