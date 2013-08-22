package com.brainbox.core.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.brainbox.core.constants.ErrorConstants;
import com.brainbox.core.utils.JSONReader;
import com.brainbox.core.vo.ReasonVO;
import com.brainbox.mobile.exception.SystemException;

public class MRJSONParser extends JSONReader {

	private static final String JSON_REASON_CODE = "ReasonId";
	private static final String JSON_REASON_TXT = "ReasonText";
	private static final String JSON_REASON_ADD_REQ = "ar";

	public static ArrayList<ReasonVO>  readReasons(String json) throws SystemException {
		ArrayList<ReasonVO> reasons = new ArrayList<ReasonVO>();

		try {
			JSONArray jArray = new JSONArray(json);
			for (int i = 0; i < jArray.length(); i++) {
				ReasonVO reason = new ReasonVO();
				JSONObject jsonObject = jArray.getJSONObject(i);
				if (jsonObject.has(JSON_REASON_CODE))
					reason.reasonCode = jsonObject.getString(JSON_REASON_CODE);
				if (jsonObject.has(JSON_REASON_TXT))
					reason.reasonText = jsonObject.getString(JSON_REASON_TXT);
				if (jsonObject.has(JSON_REASON_ADD_REQ))
					reason.additionalRequired = jsonObject
							.getBoolean(JSON_REASON_ADD_REQ);
				reasons.add(reason);
			}

		} catch (JSONException e) {
			throw new SystemException(ErrorConstants.FORMAT_ERROR);
		}
		return reasons;
	}
}
