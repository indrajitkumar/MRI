package com.brainbox.milkrun.helper;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.brainbox.core.helper.DatabaseHelper;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.core.vo.Item;
import com.brainbox.core.vo.Merchant;
import com.brainbox.core.vo.Order;
import com.brainbox.core.vo.ReasonVO;

import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.shopclues.milkrun.R;


public class MilkRunDatabaseHelper extends DatabaseHelper {
	public MilkRunDatabaseHelper(final Context context) {
		super(context);		
	}

	public MilkRunDatabaseHelper(Context context, String dbName, int version) {
		super(context, dbName, version);
		LogUtils.v("Creating database ");
	}

	@Override
	public void createTables(SQLiteDatabase db) {
		super.createTables(db);
		db.execSQL(resources.getString(com.brainbox.shopclues.milkrun.R.string.create_merchant_table));
		db.execSQL(resources.getString(R.string.create_order_table));
		db.execSQL(resources.getString(R.string.create_item_table));
		db.execSQL(resources.getString(R.string.create_mapping_table));
		db.execSQL(resources.getString(R.string.create_reason_table_query));

	}

	@Override
	public void dropTables(SQLiteDatabase db) {
		super.dropTables(db);
		db.execSQL(resources.getString(R.string.drop_merchant_table));
		db.execSQL(resources.getString(R.string.drop_order_table));
		db.execSQL(resources.getString(R.string.drop_item_table));
		db.execSQL(resources.getString(R.string.query_drop_table_reasons));
		db.execSQL(resources.getString(R.string.drop_mapping_table));

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		LogUtils.debug("Upgrading database");
		super.onUpgrade(db, oldVersion, newVersion);
	}

	public void insertMerchant(Merchant merchant) {
		SQLiteDatabase db = getWritableDatabase();
		String[] args = new String[] { merchant.merchantId };
		db.execSQL(resources.getString(R.string.delete_merchant), args);
		db.execSQL(resources.getString(R.string.delete_merchant_mapping), args);
		db.execSQL(resources.getString(R.string.delete_merchant_order), args);
		db.execSQL(resources.getString(R.string.delete_merchant_item), args);
		String insert_merchant = resources.getString(R.string.insert_merchant);
		args = new String[] { merchant.merchantId, merchant.manifestId, merchant.name, merchant.contactPerson,
				merchant.phone, merchant.addressLine1, merchant.addressLine2, merchant.city, merchant.state,
				merchant.pin, merchant.status };
		db.execSQL(insert_merchant, args);
		db.close();
	}
	
	
	public void insertMerchantData(Merchant merchant) {
		SQLiteDatabase db = getWritableDatabase();
		String saveQuery = resources.getString(R.string.insert_merchant);
		try {
			byte[] bytes = merchant.serialize();
			Object[] bindArgs;
			bindArgs = new Object[] { merchant.merchantId, merchant.manifestId, bytes, "Y" };
			db.execSQL(saveQuery, bindArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		db.close();
	}

	public void insertOrder(Order order) {
		SQLiteDatabase db = getWritableDatabase();
		String insert_order = resources.getString(R.string.insert_order);
		String[] args2 = new String[] { order.orderId, order.orderDt, order.buyerName, order.status.name(),
				order.merchantId };
		db.execSQL(insert_order, args2);
		db.close();
	}

	public void insertItem(Item item) {
		SQLiteDatabase db = getWritableDatabase();
		String[] args;
		String query = resources.getString(R.string.insert_item);
		args = new String[] { item.itemId, item.name, String.valueOf(item.qtyRec), item.warning, item.status, item.reason };
		db.execSQL(query, args);
		String insertOrderItem = resources.getString(R.string.insert_mapping);
		
		args = new String[] { item.itemId, item.orderId, item.orderId, item.merchantId, String.valueOf(item.qtyExp),
				String.valueOf(item.qtyRec), item.warning, item.status, item.reason };
		db.execSQL(insertOrderItem, args);
		db.close();
	}

	/**
	 * Updates the reasons in Database.
	 * 
	 * @param status
	 *            the status for reasons are applicable.
	 * @param reasons
	 */
	public void updateReasons(List<ReasonVO> reasons) {
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			db.beginTransaction();
			db.execSQL(resources.getString(R.string.delete_reasons_query));
			for (ReasonVO reason : reasons) {
				db.execSQL(resources.getString(R.string.insert_reason_query), new Object[] { reason.reasonCode,
						reason.reasonText, reason.additionalRequired });
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtils.error("Error while saving reasons", e);
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	public ReasonVO[] getReasons() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(resources.getString(R.string.select_reasons_query), null);
		ReasonVO[] reasons = new ReasonVO[c.getCount() + 1];
		int i = 0;
		ReasonVO reason = new ReasonVO();
		reason.reasonText = resources.getString(R.string.qty_mismatch_reason_dd_label);
		reasons[i++] = reason;
		while (c.moveToNext()) {
			reason = new ReasonVO();
			reason.reasonText = c.getString(c.getColumnIndex("REASON_TEXT"));
			reason.additionalRequired = (c.getInt(c.getColumnIndex("ADDITIONAL_REQUIRED")) != 0);
			reason.reasonCode = c.getString(c.getColumnIndex("REASON_CODE"));
			reasons[i++] = reason;
		}
		c.close();
		return reasons;
	}

	public ReasonVO[] getReasonArray() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(resources.getString(R.string.select_reasons_query), null);
		ReasonVO[] reasons = new ReasonVO[c.getCount()];
		int i = 0;
		while (c.moveToNext()) {
			ReasonVO reason = new ReasonVO();
			reason.reasonText = c.getString(c.getColumnIndex("REASON_TEXT"));
			reason.additionalRequired = (c.getInt(c.getColumnIndex("ADDITIONAL_REQUIRED")) != 0);
			reason.reasonCode = c.getString(c.getColumnIndex("REASON_CODE"));
			reasons[i++] = reason;
		}
		c.close();
		return reasons;
	}

	public ReasonVO getReason(String code) {
		SQLiteDatabase db = this.getReadableDatabase();
		if (code == null) {
			return null;
		}
		Cursor c = db.rawQuery(resources.getString(R.string.SQL_SELECT_REASON_BY_CODE), new String[] { code });
		ReasonVO reason = null;
		if (c.moveToNext()) {
			reason = new ReasonVO();
			reason.reasonText = c.getString(c.getColumnIndex("REASON_TEXT"));
			reason.additionalRequired = (c.getInt(c.getColumnIndex("ADDITIONAL_REQUIRED")) != 0);
			reason.reasonCode = c.getString(c.getColumnIndex("REASON_CODE"));
		}
		c.close();
		return reason;
	}

	public void cleanMR() {
		SQLiteDatabase db = getWritableDatabase();
		db.delete("MILKRUN_ORDER_ITEM", null, null);
		db.delete("MILKRUN_ITEM", null, null);
		db.delete("MILKRUN_ORDER", null, null);
		db.delete("MILKRUN_MERCHANT", null, null);
		db.close();
	}

	public ArrayList<Merchant> getAllMerchants() {
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cursor = db.rawQuery(resources.getString(R.string.select_all_merchants), new String[] {});
		final ArrayList<Merchant> merchants = getMerchants(cursor);
		cursor.close();
		if (db.isOpen()) {
			db.close();
		}
		return merchants;
	}

	public ArrayList<Order> getOrdersByMerchantId(String MerchantId) {
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cursor = db.rawQuery(resources.getString(R.string.select_orders), new String[] { MerchantId });
		final ArrayList<Order> orders = getOrders(cursor);
		cursor.close();
		if (db.isOpen()) {
			db.close();
		}
		return orders;
	}

	public Order getOrderByOrderID(String orderID) {
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cursor = db.rawQuery(resources.getString(R.string.select_order_by_id), new String[] { orderID });
		final ArrayList<Order> orders = getOrders(cursor);
		cursor.close();
		if (db.isOpen()) {
			db.close();
		}
		if (orders.isEmpty()) {
			return null;
		} else {
			return orders.get(0);
		}
	}

	public ArrayList<Item> getItemsByOrderId(String orderId) {
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cursor = db
				.rawQuery(resources.getString(R.string.select_items_by_order), new String[] { orderId });
		final ArrayList<Item> items = getItems(cursor);
		cursor.close();
		if (db.isOpen()) {
			db.close();
		}
		return items;
	}

	public ArrayList<Item> getItemsByMerchantId(String merchantId) {
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cursor = db.rawQuery(resources.getString(R.string.select_items_by_merchant),
				new String[] { merchantId });
		final ArrayList<Item> items = getItems(cursor);
		cursor.close();
		if (db.isOpen()) {
			db.close();
		}
		return items;
	}

	public ArrayList<Item> getWarningsByMerchantId(String merchantId) {
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cursor = db.rawQuery(resources.getString(R.string.select_item_warnings),
				new String[] { merchantId });
		final ArrayList<Item> items = getWarnings(cursor);
		cursor.close();
		if (db.isOpen()) {
			db.close();
		}
		return items;
	}

	private ArrayList<Merchant> getMerchants(final Cursor cursor) {
		final ArrayList<Merchant> merchants = new ArrayList<Merchant>();
		while (cursor != null && cursor.moveToNext()) {
			final Merchant merchant = new Merchant();
			merchant.merchantId = (cursor.getString(cursor.getColumnIndex("MERCHANT_ID")));
			// merchant.manifestId =
			// (cursor.getString(cursor.getColumnIndex("MANIFEST_ID")));
			merchant.name = (cursor.getString(cursor.getColumnIndex("MERCHANT_NAME")));
			merchant.contactPerson = (cursor.getString(cursor.getColumnIndex("CONTACT_PERSON")));
			merchant.phone = (cursor.getString(cursor.getColumnIndex("CONTACT_NUMBER")));
			merchant.addressLine1 = (cursor.getString(cursor.getColumnIndex("ADDRESS_LINE1")));
			merchant.addressLine2 = (cursor.getString(cursor.getColumnIndex("ADDRESS_LINE2")));
			merchant.city = (cursor.getString(cursor.getColumnIndex("CITY")));
			merchant.state = (cursor.getString(cursor.getColumnIndex("STATE")));
			merchant.pin = (cursor.getString(cursor.getColumnIndex("PIN")));
			merchant.status = cursor.getString(cursor.getColumnIndex("PO_STATUS"));
			merchant.manifestId = cursor.getString(cursor.getColumnIndex("MANIFEST_ID"));
			merchant.qtyExp = cursor.getInt(cursor.getColumnIndex("QTY_EXP"));
			merchant.qtyRec = cursor.getInt(cursor.getColumnIndex("QTY_REC"));

			merchants.add(merchant);
		}
		return merchants;
	}

	private ArrayList<Order> getOrders(final Cursor cursor) {
		final ArrayList<Order> orders = new ArrayList<Order>();
		while (cursor != null && cursor.moveToNext()) {
			final Order order = new Order();
			order.id = cursor.getInt(cursor.getColumnIndex("ROW_ID"));
			order.orderId = cursor.getString(cursor.getColumnIndex("ORDER_ID"));
			order.merchantId = cursor.getString(cursor.getColumnIndex("MERCHANT_ID"));
			order.orderDt = cursor.getString(cursor.getColumnIndex("ORDER_DT"));
			order.setStatus(cursor.getString(cursor.getColumnIndex("STATUS")));
			order.buyerName = cursor.getString(cursor.getColumnIndex("CUSTOMER_NAME"));
			orders.add(order);
		}
		return orders;
	}

	private ArrayList<Item> getItems(final Cursor cursor) {
		final ArrayList<Item> items = new ArrayList<Item>();
		while (cursor != null && cursor.moveToNext()) {
			final Item item = new Item();
			// item.skuCode =
			// cursor.getString(cursor.getColumnIndex("SKU_CODE"));
			item.itemId = cursor.getString(cursor.getColumnIndex("ITEM_ID"));
			if (cursor.getColumnIndex("ORDER_ID") != -1) {
				item.orderId = cursor.getString(cursor.getColumnIndex("ORDER_ID"));
			}
			item.name = cursor.getString(cursor.getColumnIndex("PRODUCT_NAME"));
			item.qtyExp = cursor.getInt(cursor.getColumnIndex("QTY_EXP"));
			// item.qtyAvl = cursor.getInt(cursor.getColumnIndex("QTY_AVL"));
			item.qtyRec = cursor.getInt(cursor.getColumnIndex("QTY_REC"));
			item.status = cursor.getString(cursor.getColumnIndex("STATUS"));
			item.reason = cursor.getString(cursor.getColumnIndex("COMMENT"));
			item.warning = cursor.getString(cursor.getColumnIndex("WARNING"));
			items.add(item);
		}
		return items;
	}

	private ArrayList<Item> getWarnings(final Cursor cursor) {
		final ArrayList<Item> items = new ArrayList<Item>();
		while (cursor != null && cursor.moveToNext()) {
			final Item item = new Item();
			// item.skuCode =
			// cursor.getString(cursor.getColumnIndex("SKU_CODE"));
			item.itemId = cursor.getString(cursor.getColumnIndex("ITEM_ID"));
			item.warning = cursor.getString(cursor.getColumnIndex("WARNING"));
			item.name = cursor.getString(cursor.getColumnIndex("PRODUCT_NAME"));
			items.add(item);
		}
		return items;
	}

	public void updateItem(Item item) {
		SQLiteDatabase db = getWritableDatabase();
		String updateItemQuery = resources.getString(R.string.sql_update_item);
		String[] args = new String[] { String.valueOf(item.qtyRec), item.reason, MilkRunConstants.STATUS_REVISED,
				item.itemId };
		db.execSQL(updateItemQuery, args);
		db.close();
	}

	public void updateOrderItem(Item item) {
		SQLiteDatabase db = getWritableDatabase();
		String updateItemQuery = resources.getString(R.string.sql_update_order_item);
		String[] args = new String[] { String.valueOf(item.qtyRec), item.reason, MilkRunConstants.STATUS_REVISED,
				item.itemId, item.orderId };
		db.execSQL(updateItemQuery, args);
		db.close();
	}

	public void updateOrderStatus(Order order, String status) {
		SQLiteDatabase db = getWritableDatabase();
		String query = resources.getString(R.string.sql_update_order_status);
		String[] args = new String[] { status, order.orderId };
		db.execSQL(query, args);
		db.close();
	}

	public void updateRecQuantity(Item item) {
		SQLiteDatabase db = getWritableDatabase();
		String updateItemQuery = resources.getString(R.string.sql_update_qtyRec);
		db.execSQL(updateItemQuery, new String[] { item.itemId, item.itemId });
		db.close();
	}

	public void closePO(String merchantId) {
		updatePO(merchantId, MilkRunConstants.STATUS_CLOSED);
	}

	public void updatePO(String merchantId, String status) {
		SQLiteDatabase db = getWritableDatabase();
		String query = resources.getString(R.string.sql_update_PO);
		String[] args = new String[] { status, merchantId };
		db.execSQL(query, args);
		db.close();
	}

	public void refresh() {
		// TODO Auto-generated method stub
	}

	public void skipMerchant(Merchant merchant, String reasonCode) {
		updatePO(merchant.merchantId, MilkRunConstants.STATUS_SKIPPED);
	}
}