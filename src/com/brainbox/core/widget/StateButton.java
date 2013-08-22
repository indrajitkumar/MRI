package com.brainbox.core.widget;


import com.brainbox.shopclues.milkrun.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class StateButton extends Button {
	public StateButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private static final int[] STATE_ON = { R.attr.state_on };

	private boolean mIsOn = false;

	public void setOn(boolean isOn) {
		mIsOn = isOn;
		refreshDrawableState();
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (mIsOn) {
			mergeDrawableStates(drawableState, STATE_ON);
		}
		return drawableState;
	}

}
