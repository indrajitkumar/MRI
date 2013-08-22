package com.brainbox.log.vo;

import java.util.EnumMap;

import com.brainbox.core.log.LogField;

public class AppLogData extends EnumMap<LogField, String> {

	private static final long serialVersionUID = -2441288183531492316L;

	public AppLogData() {
		super(LogField.class);
	}
}
