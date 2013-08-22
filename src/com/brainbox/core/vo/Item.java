package com.brainbox.core.vo;

import java.io.Serializable;

public class Item implements Serializable {
	private static final long serialVersionUID = 4821814284591461198L;
	public int id;
	public String itemId;
	public String productID;
	public String orderId;
	public String merchantId;
	public String name;
	public String description;
	public double price;
	public int qtyExp;
//	public int qtyAvl;
	public int qtyRec;
	public String status;
	public String reason;
	public String warning;
}
