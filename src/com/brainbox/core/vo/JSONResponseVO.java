package com.brainbox.core.vo;

import java.io.Serializable;

public class JSONResponseVO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String FAILED = "FAILED";
	public String status = FAILED;
	public String message;
	public String extra;

	public JSONResponseVO() {
		status = FAILED;
	}
}
