package com.brainbox.shopclues.milkrun.activity;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.DEVICE_ID;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PACKAGE_NAME;
import static org.acra.ReportField.PRODUCT;
import static org.acra.ReportField.REPORT_ID;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.USER_CRASH_DATE;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import com.brainbox.shopclues.milkrun.R;

import android.app.Application;

@ReportsCrashes(formKey = "", reportType = org.acra.sender.HttpSender.Type.JSON, httpMethod = org.acra.sender.HttpSender.Method.PUT, formUriBasicAuthLogin = "pramod", formUriBasicAuthPassword = "pramod",

customReportContent = { REPORT_ID, DEVICE_ID, APP_VERSION_CODE, APP_VERSION_NAME, PACKAGE_NAME, BRAND, PRODUCT,
		ANDROID_VERSION, STACK_TRACE, USER_CRASH_DATE, LOGCAT })
public class MyApp extends Application {
	@Override
	public void onCreate() {
		ACRA.init(this);
		String acraName = getString(R.string.acra_name);
		ACRA.getConfig()
				.setFormUri("https://mobifly.iriscouch.com/acra-" + acraName + "/_design/acra-storage/_update/report");
		super.onCreate();
	}
}
