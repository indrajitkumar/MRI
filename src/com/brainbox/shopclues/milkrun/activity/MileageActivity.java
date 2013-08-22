package com.brainbox.shopclues.milkrun.activity;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.brainbox.core.http.HttpRequestTask;
import com.brainbox.core.utils.CommonUtils;
import com.brainbox.core.utils.JSONReader;
import com.brainbox.core.vo.HttpRequestVO;
import com.brainbox.core.vo.JSONResponseVO;

import com.brainbox.milkrun.helper.MilkRunHttpRequestHelper;

import com.brainbox.shopclues.milkrun.R;
import com.brainbox.vo.enums.ActionEnum;

public class MileageActivity extends MilkRunActivity {

	private static final String PARAM_MILEAGE = "mileage";
	public static final String MILEAGE_TYPE = "MILEAGE_TYPE";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.mileage);
		super.onCreate(savedInstanceState);
	}

	public void saveMileage(View v) {
		String text = ((TextView) findViewById(R.id.input)).getText().toString();
		if (CommonUtils.isNumeric(text)) {
			HttpRequestVO reqVo = new HttpRequestVO();
			reqVo.params.put(PARAM_MILEAGE, text);
			reqVo.params.put(MilkRunHttpRequestHelper.PARAM_TYPE, getIntent().getStringExtra(MILEAGE_TYPE));
			reqVo.params.put(MilkRunHttpRequestHelper.PARAM_ACTION, ActionEnum.UPDATE_MILEAGE.value);
			new HttpRequestTask(this, httpRequestHelper, new Handler(callback)).execute(reqVo);
		}
	}

	private Callback callback = new Callback() {
		public boolean handleMessage(Message msg) {
			JSONResponseVO vo = (JSONResponseVO) msg.getData().getSerializable("DATA");
			if (vo != null && JSONReader.SUCCESS.equalsIgnoreCase(vo.status)) {
				showAlertDialog(vo.message, "SUCCESS", ocl);
			} else {
				showAlertDialog("Mileage Not Updated ", "ERROR", ocl);
			}
			return true;
		};
	};

	OnClickListener ocl = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			setResult(RESULT_OK, null);
			finish();
		}
	};

}