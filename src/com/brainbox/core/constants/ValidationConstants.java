package com.brainbox.core.constants;

public class ValidationConstants {
	public static final String NAME_PATTERN = "[a-zA-Z\\s]{0,30}";
	public static final String TAX_PATTERN = ".{0}|[a-zA-Z0-9]{10,30}";
	public static final String PIN_PATTERN = "\\d{6}";
	public static final String PHONE_PATTERN = ".{0}|({0}0?\\d{10})";
	public static final String MOBILE_PATTERN = "\\d{10})";
	public static final String EMAIL_PATTERN =
	        "^[a-zA-Z0-9]+([\\._-]?[a-zA-Z0-9])*@([a-zA-Z0-9]+(\\-[a-zA-Z0-9])*(\\.))+[a-zA-Z]{2,4}$";
}
