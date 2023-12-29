package com.emoldino.api.common.resource.composite.notbel.dto;

import com.emoldino.api.common.resource.base.noti.dto.NotiUserItem;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.ValueUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class NotBelItem extends NotiUserItem {
	public NotBelItem(NotiUserItem item) {
		ValueUtils.map(item, this);
	}

	private Long passedSeconds;

	public long getPassedSeconds() {
		if (passedSeconds != null) {
			return passedSeconds;
		} else if (getSentAt() == null) {
			passedSeconds = 0L;
			return 0;
		}
		long seconds = DateUtils2.getInstant().getEpochSecond() - getSentAt().getEpochSecond();
		passedSeconds = seconds <= 0 ? 0 : seconds;
		return passedSeconds;
	}

	public long getPassedMinutes() {
		long seconds = getPassedSeconds();
		return seconds <= 0 ? 0 : (seconds / 60L);
	}

	public long getPassedHours() {
		long seconds = getPassedSeconds();
		return seconds <= 0 ? 0 : (seconds / 3600L);
	}

	public long getPassedDays() {
		long seconds = getPassedSeconds();
		return seconds <= 0 ? 0 : (seconds / (3600L * 24));
	}
}
