package com.brainbox.core.widget;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.brainbox.core.vo.Item;
import com.brainbox.core.vo.Order;
import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.milkrun.helper.MilkRunDatabaseHelper;
import com.brainbox.shopclues.milkrun.R;

public class OrdersListAdapter extends BaseExpandableListAdapter {
	private ArrayList<Order> orders;
	public MilkRunDatabaseHelper db;
	public LayoutInflater mInflater;
	Context context;

	public OrdersListAdapter(ArrayList<Order> orders, Context context) {
		this.orders = orders;
		this.context = context;
		this.db = new MilkRunDatabaseHelper(context);
		this.mInflater =
		        (LayoutInflater) context
		                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	public Object getChild(int groupPosition, int childPosition) {
		Order order = orders.get(groupPosition);
		Item item = null;
		ArrayList<Item> items = db.getItemsByOrderId(order.orderId);
		if (items != null && !items.isEmpty())
			item = db.getItemsByOrderId(order.orderId).get(childPosition);
		return item;
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public int getChildrenCount(int groupPosition) {
		return db.getItemsByOrderId(orders.get(groupPosition).orderId).size();
	}

	public Object getGroup(int groupPosition) {
		return orders.get(groupPosition);
	}

	public int getGroupCount() {
		return orders.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
	        boolean isLastChild, View convertView, ViewGroup parent) {
		final Item item = (Item) getChild(groupPosition, childPosition);
		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.order_child_view, null);
		}
		if (item != null) {
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
		}
		return convertView;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
	        View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.order_list_item, null);
		}
		final Order order = orders.get(groupPosition);
		((TextView) convertView.findViewById(R.id.ordId))
		        .setText(order.orderId);
		((TextView) convertView.findViewById(R.id.ordDt))
		        .setText(order.orderDt);
		int resId = 0;
		switch (order.status) {
		case OPEN:
			resId = 0;
			break;
		case FILLED:
			resId = R.drawable.bg_green;
			break;
		case PARTIAL_FILL:
			resId = R.drawable.bg_amber;
			break;
		case SKIPPED:
		case REVISED:
			resId = R.drawable.bg_brown;
			break;
		}
		if (isExpanded) {
			resId = R.drawable.bg_group_expanded;
		}
		if (resId != 0) {
			convertView.setBackgroundResource(resId);
		} else {
			convertView.setBackgroundDrawable(null);
		}
		return convertView;
	}
}
