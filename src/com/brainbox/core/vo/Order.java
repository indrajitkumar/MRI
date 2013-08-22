package com.brainbox.core.vo;

import java.io.Serializable;

import com.brainbox.vo.enums.StatusEnum;

public class Order implements Serializable {
	private static final long serialVersionUID = 1L;
	public int id;
	public String orderId;
	public String subOrderId;
	public String merchantId;
	public String orderDt;
	public String buyerName;
	public StatusEnum status;

	public void setStatus(String status) {
		try {
			this.status = StatusEnum.valueOf(status);
		} catch (Exception e) {
			this.status = StatusEnum.OPEN;
		}
	}
}
