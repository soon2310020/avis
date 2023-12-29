package com.emoldino.framework.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class BooleanOut {
	private static final BooleanOut TRUE = new BooleanOut(true);
	private static final BooleanOut FALSE = new BooleanOut(false);

	private boolean value;

	public static BooleanOut getTrue() {
		return TRUE;
	}

	public static BooleanOut getFalse() {
		return FALSE;
	}

	public static BooleanOut getDefault(boolean value) {
		return value ? TRUE : FALSE;
	}

}
