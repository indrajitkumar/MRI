package com.brainbox.shopclues.milkrun.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.brainbox.core.constants.CommonConstants;
import com.brainbox.core.vo.Item;
import com.brainbox.core.vo.ReasonVO;
import com.brainbox.milkrun.constants.MilkRunConstants;

import com.brainbox.shopclues.milkrun.R;
import com.brainbox.vo.enums.ActionEnum;
import com.brainbox.vo.enums.PinTypeEnum;

/**
 * This activity is used for showing the list of the items.
 * 
 * @author BrainBox Network
 * 
 */
public abstract class ItemViewActivity extends MilkRunActivity implements
OnItemClickListener {
	protected static Item focusedItem;
	protected LayoutInflater mInflater;
	protected ArrayList<Item> data;
	protected CustomAdapter adapter;
	protected ListView mListView;
	protected int position;
	ReasonVO[] reasons;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		setContentView(R.layout.item_view);
		super.onCreate(savedInstanceState);
		mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		reasons = db.getReasons();
	}

	@Override
	protected void onStart() {
		super.onStart();
		dialog = new Dialog(this);
		position = this.getIntent().getIntExtra(CommonConstants.POSITION, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		showList(position);
	}

	/**
	 * 
	 * This method is invoked when user clicks on next button on navigation bar.
	 * 
	 * @param v
	 * 
	 *            the view object
	 */
	public void next(View v) {
		showList(++position);
	}

	protected void goToItem(String name) {
		int pos = 0;
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).name.equals(name)) {
				pos = i;
				break;
			}
		}
		mListView.setSelectionAfterHeaderView();
		// findViewById(R.id.autocomplete_item).setVisibility(View.GONE);
		mListView.setSelection(pos);
		inputManager.hideSoftInputFromWindow(
				getCurrentFocus().getWindowToken(), 0);
	}

	public ArrayList<String> getItems() {
		final ArrayList<String> items = new ArrayList<String>();
		for (Item item : data) {
			items.add(item.name);
		}
		return items;
	}

	/**
	 * This method is invoked when user clicks on previous button on navigation
	 * bar.
	 * 
	 * @param v
	 *            the view object
	 */
	public abstract void showList(int position);

	public void showItems(ArrayList<Item> data) {
		mListView = (ListView) findViewById(R.id.listView);
		mListView.setAdapter(adapter);
	}

	public void onItemClick(final AdapterView<?> adapter, final View view,
			final int position, final long id) {
		// Get the focused item.
		focusedItem = data.get(position);
		action = ActionEnum.UPDATE_QUANTITY;
		showDialog();
	}

	private void showDialog() {
		dialog = new Dialog(activity, R.style.BB_Theme_Dialog);
		dialog.setContentView(R.layout.update_qty);
		// builder.set
		dialog.findViewById(R.id.goButton).setOnClickListener(oicl);
		EditText editText = (EditText) dialog.findViewById(R.id.input);
		if (!MilkRunConstants.STATUS_OPEN.equals(focusedItem.status)) {
			String text = String.valueOf(focusedItem.qtyRec);
			editText.setText(text);
			editText.setSelection(text.length());
		}
		dialog.findViewById(R.id.cancelButton).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
		// Populate Reason codes
		Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner_reasons);
		// Create an ArrayAdapter using the string array and a default spinner
		// Get Mismatch Reasons
		ArrayAdapter<ReasonVO> reasons_adapter = new ArrayAdapter<ReasonVO>(
				this, android.R.layout.simple_spinner_item, reasons);
		// Specify the layout to use when the list of choices appears
		reasons_adapter
		.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(reasons_adapter);
		spinner.setPrompt("Reason for mismatch");
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view,
					int position, long id) {
				focusedItem.reason = reasons[position].reasonCode;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// arg0.set
			}
		});
		//
		int selected = reasons_adapter.getPosition(db
				.getReason(focusedItem.reason));
		if (selected >= 0 && selected <= reasons_adapter.getCount()) {
			spinner.setSelection(selected);
		}
		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.FILL_PARENT;
		// lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		// lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		// lp.softInputMode =
		// WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
		dialog.getWindow().setAttributes(lp);
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}

	protected class CustomAdapter extends ArrayAdapter<Item> {
		public CustomAdapter(final Context context,
				final int textViewResourceId, final List<Item> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {
			final Item item = getItem(position);
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.item_layout, null);
			}
			((TextView) convertView.findViewById(R.id.sku))
			.setText(item.productID);
			((TextView) convertView.findViewById(R.id.item_name))
			.setText(item.name);
			((TextView) convertView.findViewById(R.id.qty_exp)).setText(""
					+ item.qtyExp);
			if (!MilkRunConstants.STATUS_OPEN.equals(item.status)) {
				((TextView) convertView.findViewById(R.id.qty_avl)).setText(""
						+ item.qtyRec);
				((TextView) convertView.findViewById(R.id.qty_avl))
				.setBackgroundResource(R.drawable.bg_green);
			} else {
				((TextView) convertView.findViewById(R.id.qty_avl)).setText("");
				((TextView) convertView.findViewById(R.id.qty_avl))
				.setBackgroundDrawable(null);
			}
			if (!MilkRunConstants.STATUS_OPEN.equals(item.status)) {
				((TextView) convertView.findViewById(R.id.qty_rec)).setText(""
						+ item.qtyRec);
				if (item.qtyRec != item.qtyExp) {
					convertView.findViewById(R.id.qty_rec)
					.setBackgroundResource(R.drawable.bg_brown);
				} else {
					convertView.findViewById(R.id.qty_rec)
					.setBackgroundResource(R.drawable.bg_green);
				}
			} else {
				((TextView) convertView.findViewById(R.id.qty_rec)).setText("");
				((TextView) convertView.findViewById(R.id.qty_rec))
				.setBackgroundDrawable(null);
			}
			return convertView;
		}
	}

	protected OnClickListener cl = new OnClickListener() {
		@Override
		public void onClick(View v) {
			input = ((EditText) dialog.findViewById(R.id.input)).getText();
			dialog.dismiss();
			inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
			AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
			switch (action) {
			case VALIDATE_PIN:
				RadioGroup radioGroup = (RadioGroup) dialog
				.findViewById(R.id.option_pin_typ);
				int checkedRadioButton = radioGroup.getCheckedRadioButtonId();
				String type = ((RadioButton) dialog
						.findViewById(checkedRadioButton)).getText().toString();
				if (getString(R.string.option_merchant).equals(type)) {
					pinType = PinTypeEnum.TYPE_MERCHANT;
				} else {
					pinType = PinTypeEnum.TYPE_PICKUP_BOY;
				}
				new HttpResponseHanlder(ItemViewActivity.this,
						ActionEnum.CLOSE_PO).getResponse();
				break;
			case UPDATE_QUANTITY:
				if (input != null && !"".equals(input)) {
					int avlQty = Integer.valueOf(input.toString());
					if (avlQty > focusedItem.qtyExp || avlQty < 0) {
						alert.setMessage(String.format(
								getString(R.string.alert_invalid_qty), 0,
								focusedItem.qtyExp));
						alert.setNeutralButton("OK", null);
						alert.show();
					} else {
						focusedItem.qtyRec = Integer.valueOf(input.toString());
						focusedItem.status = MilkRunConstants.STATUS_REVISED;
						updateQuantity();
					}
				}
				break;
			default:
				if (input != null && !"".equals(input)) {
					int avlQty = Integer.valueOf(input.toString());
					//Code just chage to set 0 or 1 Input to undersatand better// revert after the checking
					if (avlQty > focusedItem.qtyExp || avlQty < 0) {
						alert.setMessage(String.format(
								getString(R.string.alert_invalid_qty), 0,
								focusedItem.qtyExp));
						alert.setNeutralButton("OK", null);
						alert.show();
					} else {
						focusedItem.qtyRec = Integer.valueOf(input.toString());
						focusedItem.status = MilkRunConstants.STATUS_REVISED;
						updateQuantity();
					}
				}
				break;
			}
		}
	};

	protected void updateQuantity() {
		db.updateItem(focusedItem);
		adapter.notifyDataSetChanged();
		mListView.requestFocus();
		showHideButtons();
	}

	private OnClickListener oicl = new OnClickListener() {
		public void onClick(View v) {
			CharSequence input = ((EditText) dialog.findViewById(R.id.input))
					.getText();
			// inputManager.hideSoftInputFromWindow();
			AlertDialog.Builder alert = new AlertDialog.Builder(
					dialog.getContext());

			if (input != null && !"".equals(input.toString())  && input.length()<5) {
				int avlQty = Integer.valueOf(input.toString());

				if(avlQty == 0 || avlQty == 1)
				{
//					alert.setMessage(String.format(
//							getString(R.string.alert_validate), 0,
//							focusedItem.qtyExp));
//					alert.setNeutralButton("OK", null);
//					alert.show();
					dialog.dismiss();
					focusedItem.qtyRec = Integer.valueOf(input.toString());
					focusedItem.status = MilkRunConstants.STATUS_REVISED;
					focusedItem.reason = null;
					updateQuantity();
				}
				else if(avlQty > 1) {
					alert.setMessage(String.format(
							getString(R.string.alert_invalid_qty), 0,
							focusedItem.qtyExp));
					alert.setNeutralButton("OK", null);
					alert.show();
				}
				
//				if (avlQty > focusedItem.qtyExp || avlQty < 0) {
//					alert.setMessage(String.format(
//							getString(R.string.alert_invalid_qty), 0,
//							focusedItem.qtyExp));
//					alert.setNeutralButton("OK", null);
//					alert.show();
//				} else if (avlQty != focusedItem.qtyExp) {
//				//if(avlQty == 0 || avlQty == 1){
//					Spinner spinner = (Spinner) dialog
//							.findViewById(R.id.spinner_reasons);
//					int res = spinner.getSelectedItemPosition();
//					if (res != 0) {
//						dialog.dismiss();
//						focusedItem.qtyRec = Integer.valueOf(input.toString());
//						focusedItem.status = MilkRunConstants.STATUS_REVISED;
//						String reasonCode = ((ReasonVO) spinner
//								.getSelectedItem()).reasonCode;
//						focusedItem.reason = reasonCode;
//						updateQuantity();
//					} else {
//						alert.setMessage(getString(R.string.alert_qty_mismatch_reason));
//						alert.setNeutralButton("OK", null);
//						alert.show();
//					}
//				} else {
//					dialog.dismiss();
//					focusedItem.qtyRec = Integer.valueOf(input.toString());
//					focusedItem.status = MilkRunConstants.STATUS_REVISED;
//					focusedItem.reason = null;
//					updateQuantity();
//				}
			}
			
			else{
				alert.setMessage(String.format(
						getString(R.string.alert_invalid_qty), 0,
						focusedItem.qtyExp));
				alert.setNeutralButton("OK", null);
				alert.show();
			}
		}
	};

	protected abstract void showHideButtons();
}