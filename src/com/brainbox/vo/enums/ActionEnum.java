package com.brainbox.vo.enums;

public enum ActionEnum {
	UPDATE_QUANTITY, REFRESH, VALIDATE_PIN, UPDATE_PO("revise"), UPDATE_ITEM, CONFIRM_PO(
			"confirm"), CLOSE_PO("close"), SKIP_MERCHANT("skip"), MR_VALIDATE, GO_HOME, PROMPT_VALIDATE, PROMPT_CONFIRM, PROMPT_CLOSE, UPDATE_MILEAGE(
			"updateMileage"), RECORD_LOCATION("recordLocation");

	public String value;

	ActionEnum(String action) {
		this.value = action;
	}

	ActionEnum() {
		this.value = name();
	}
}
