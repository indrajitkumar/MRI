package com.brainbox.core.vo;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class ReasonVO implements Serializable {

	private static final long serialVersionUID = 1L;
	@SerializedName("ReasonId")
	public String reasonCode;
	public String reasonText;
	@SerializedName("ar")
	public boolean additionalRequired;

	@Override
	public String toString() {
		return reasonText;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ReasonVO && obj != null) {
			return reasonCode.equals(((ReasonVO) obj).reasonCode);
		}
		return false;
	}
}
