package com.brainbox.core.vo;

import com.brainbox.core.vo.LoginResponseVO;
import com.google.gson.annotations.SerializedName;

public class MRLoginResponseVO extends LoginResponseVO {

	public static final long serialVersionUID = 1L;
	public boolean mileageUpdated;
	@SerializedName("Reason")
	public String reason;
	@SerializedName("pid")
	public String pickUpBoyId;

}
