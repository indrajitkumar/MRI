package com.brainbox.core.vo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Merchant implements Serializable {
	public static final long serialVersionUID = 1L;
	public int rowId;
	public String merchantId;
	public String manifestId;
	public String name;
	public String addressLine1;
	public String addressLine2;
	public String city;
	public String state;
	public String pin;
	public String contactPerson;
	public String phone;
	public String authCode;
	public String status;
	public int qtyExp;
	public int qtyRec;
	public double lat;
	public double lon;

	public byte[] serialize() throws IOException {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this);
		bytes = bos.toByteArray();
		bos.flush();
		bos.close();
		oos.flush();
		oos.close();
		return bytes;
	}

}
