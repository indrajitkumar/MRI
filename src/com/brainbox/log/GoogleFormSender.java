/*
 *  Copyright 2010 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.brainbox.log;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.sender.ReportSender;

import android.net.Uri;
import android.os.Build;

import com.brainbox.core.config.SystemConfig;
import com.brainbox.core.constants.ErrorConstants;
import com.brainbox.core.log.HttpRequest;
import com.brainbox.core.log.LogField;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.log.vo.AppLogData;
import com.brainbox.mobile.exception.SystemException;

/**
 * ACRA's default {@link ReportSender}: sends report data to a GoogleDocs Form.
 * 
 * @author Kevin Gaudin
 * 
 */
public class GoogleFormSender implements LogSender {
	public static final String GOOGLE_FORM_URL = "https://docs.google.com/spreadsheet/formResponse?formkey=%s&ifq";

	private final Uri mFormUri;

	/**
	 * Creates a new dynamic GoogleFormSender which will send data to a Form
	 * identified by its key. All parameters are retrieved from
	 * {@link ACRA#getConfig()} and can thus be changed dynamically with
	 * {@link ACRAConfiguration#setFormKey(String)}
	 */
	public GoogleFormSender() {
		mFormUri = Uri.parse(String.format(GOOGLE_FORM_URL,
				"dEhKbENzYUhjNWlxV2JSa3JXZ2dHcGc6MQ"));
	}

	/**
	 * Creates a new fixed GoogleFormSender which will send data to a Form
	 * identified by its key provided as a parameter. Once set, the destination
	 * form can not be changed dynamically.
	 * 
	 * @param formKey
	 *            The formKey of the destination Google Doc Form.
	 */
	public GoogleFormSender(String formKey) {
		mFormUri = Uri.parse(String.format(GOOGLE_FORM_URL, formKey));
	}

	public void send(AppLogData logData) throws SystemException {
		Uri formUri = mFormUri;

		logData.put(LogField.PACKAGE, SystemConfig.packageInfo.packageName);

		logData.put(LogField.VERSION_CODE,
				String.valueOf(SystemConfig.packageInfo.versionCode));

		logData.put(LogField.VERSION_NAME, SystemConfig.packageInfo.versionName);

		logData.put(LogField.DEVICE_ID, SystemConfig.tManager.getDeviceId());

		logData.put(LogField.BRAND, Build.BRAND);

		logData.put(LogField.MODEL, Build.MODEL);

		final Map<String, String> formParams = remap(logData);
		// values observed in the GoogleDocs original html form
		formParams.put("pageNumber", "0");
		formParams.put("backupCache", "");
		formParams.put("submit", "Envoyer");

		try {
			final URL reportUrl = new URL(formUri.toString());
			LogUtils.debug("Connect to " + reportUrl);

			// final HttpRequest request = new HttpRequest();
			// request.setConnectionTimeOut(ACRA.getConfig().connectionTimeout());
			// request.setSocketTimeOut(ACRA.getConfig().socketTimeout());
			// request.setMaxNrRetries(ACRA.getConfig()
			// .maxNumberOfRequestRetries());
			// request.sendPost(reportUrl, formParams);

		} catch (IOException e) {
			throw new SystemException(ErrorConstants.IO_ERROR);
		}
	}

	private Map<String, String> remap(Map<LogField, String> report) {

		LogField[] fields = LogField.values();

		int inputId = 0;
		final Map<String, String> result = new HashMap<String, String>();
		for (LogField originalKey : fields) {
			result.put("entry." + inputId + ".single", report.get(originalKey));

			inputId++;
		}
		return result;
	}
}
