package com.brainbox.core.vo;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestVO {
	public String url;
	public String requestMethod;
	public Map<String, String> params;
	
	public HttpRequestVO(){
		params = new HashMap<String, String>();
	}
}
