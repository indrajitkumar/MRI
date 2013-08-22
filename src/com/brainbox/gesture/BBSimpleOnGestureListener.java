package com.brainbox.gesture;

import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class BBSimpleOnGestureListener extends SimpleOnGestureListener {

	public static String SWIPE_TYPE = "SWIPTE_TYPE";
	public static String SWIPE_ACTION = "com.brainbox.gesture.SWIPE";
	Context context;

	public BBSimpleOnGestureListener(Context context) {
		this.context = context;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		float sensitvity = 50;
		Intent i = new Intent("com.brainbox.gesture.SWIPE");
		SwipeTypeEnum swipeType = null;

		float movX = e1.getX() - e2.getX();
		float movY = e1.getY() - e2.getY();

		if (movX > sensitvity) {
			swipeType = SwipeTypeEnum.SWIPE_LEFT;
		} else if (-movX > sensitvity) {
			swipeType = SwipeTypeEnum.SWIPE_RIGHT;
		}

		if (movY > sensitvity) {
			swipeType = SwipeTypeEnum.SWIPE_UP;
		} else if (-movY > sensitvity) {
			swipeType = SwipeTypeEnum.SWIPE_DOWN;
		}
		i.putExtra(SWIPE_TYPE, swipeType);
		context.sendBroadcast(i);
//		LogUtils.debug("Swipe : " + swipeType);
		return super.onFling(e1, e2, velocityX, velocityY);
	}

}
