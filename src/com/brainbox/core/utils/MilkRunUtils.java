package com.brainbox.core.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.brainbox.core.constants.ConfigConstants;
import com.brainbox.core.vo.Item;
import com.brainbox.core.vo.Merchant;
import com.brainbox.core.vo.Order;
import com.brainbox.core.vo.ReasonVO;
import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.milkrun.helper.MilkRunDatabaseHelper;

import com.brainbox.vo.enums.StatusEnum;
import com.csvreader.CsvReader;


public class MilkRunUtils {
	public static MilkRunDatabaseHelper db = (MilkRunDatabaseHelper) ConfigConstants.DATABASE_HELPER;

	public static void readManifest(String apiRes, String status)
			throws IOException {
		if (status == null) {
			status = MilkRunConstants.STATUS_OPEN;
		}
		CsvReader parser = new CsvReader(new StringReader(apiRes));
		parser.readHeaders();
		HashMap<String, Merchant> merchants = new HashMap<String, Merchant>();
		String merchantId = null;
		while (parser.readRecord()) {
			String id = parser.get("Merchant Id");
			Merchant merchant;
			if (!"".equals(id) && !merchants.containsKey(id)) {
				merchantId = id;
				merchant = new Merchant();
				merchant.merchantId = id;
				merchant.manifestId = parser.get("Manifest Id");
				db.insertValue(MilkRunConstants.MANIFEST_ID,
						merchant.manifestId);
				merchant.name = parser.get("Merchant Name");
				merchant.addressLine1 = parser.get("Address");
				merchant.phone = parser.get("Phone");
				merchant.status = status;
				db.insertMerchant(merchant);
				merchants.put(id, merchant);
			} else {
				merchant = merchants.get(merchantId);
			}
			String orderID = parser.get("Order ID");
			Order order = new Order();
			order.orderId = orderID;
			order.orderDt = parser.get("Order Date");
			order.setStatus(status);
			order.merchantId = merchantId;
			order.buyerName = "Buyer 1";
			db.insertOrder(order);
			Item item = new Item();
			item.orderId = order.orderId;
			item.merchantId = merchant.merchantId;
			item.name = parser.get("Product Name");
			item.productID = parser.get("Product Id");
			item.itemId = parser.get("Item Id");
			item.qtyExp = Integer.valueOf(parser.get("Expected QTY"));
			String rec = parser.get("Received QTY");
			item.qtyRec = Integer.valueOf("".equals(rec) ? "0" : rec);
			item.status = status;
			item.reason = parser.get("ReasonId");
			item.warning = parser.get("Warning");
			db.insertItem(item);

			// updateOrderStatus(order);
		}
		updateRecQuantity();
		updateOrders();
	}

	private static void updateRecQuantity() {
		ArrayList<Merchant> merchants = db.getAllMerchants();
		for (Merchant merchant : merchants) {
			ArrayList<Item> items = db
					.getItemsByMerchantId(merchant.merchantId);
			for (Item item : items) {
				db.updateRecQuantity(item);
			}
		}

	}

	private static void updateOrders() {
		ArrayList<Merchant> merchants = db.getAllMerchants();
		for (Merchant merchant : merchants) {
			ArrayList<Order> orders = db
					.getOrdersByMerchantId(merchant.merchantId);
			for (Order order : orders) {
				ArrayList<Item> expandedItems = db
						.getItemsByOrderId(order.orderId);
				boolean skipped = true;
				StatusEnum orderstStatus = StatusEnum.FILLED;
				for (Item item : expandedItems) {
					if (item.qtyRec > 0) {
						skipped = false;
					}
					if (MilkRunConstants.STATUS_OPEN.equals(item.status)) {
						orderstStatus = StatusEnum.OPEN;
						skipped = false;
						break;
					} else if (item.qtyExp != item.qtyRec) {
						orderstStatus = StatusEnum.PARTIAL_FILL;
						// break;
					}
				}
				if (skipped) {
					orderstStatus = StatusEnum.SKIPPED;
				}
				order.status = orderstStatus;
				db.updateOrderStatus(order, orderstStatus.name());
			}
		}

	}

	public static List<ReasonVO> readReasons(String apiRes) throws IOException {
		CsvReader parser = new CsvReader(new StringReader(apiRes));
		parser.readHeaders();
		List<ReasonVO> reasons = new ArrayList<ReasonVO>();
		while (parser.readRecord()) {
			ReasonVO reason = new ReasonVO();
			reason.reasonCode = parser.get("Id");
			reason.reasonText = parser.get("Reason");
			reasons.add(reason);
		}
		return reasons;
	}
}
