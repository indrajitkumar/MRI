package com.brainbox.core.helper;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.brainbox.core.utils.LogUtils;
import com.brainbox.shopclues.milkrun.R;

public class DatabaseHelper extends SQLiteOpenHelper {
	protected Resources resources;
	protected Context ctx;
	protected static DatabaseHelper helper;

	public DatabaseHelper(Context context) {
		this(context, context.getResources().getString(R.string.database_name), context.getResources().getInteger(
				R.integer.database_version));
		resources = context.getResources();
	}

	public DatabaseHelper(Context context, String dbName, int version) {
		super(context, dbName, null, version);
		resources = context.getResources();
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		LogUtils.debug(getClass().getSimpleName() + "creating Tables");
		createTables(db);
	}

	public void createTables(SQLiteDatabase db) {
		db.execSQL(resources.getString(R.string.create_config_table));
		db.execSQL(resources.getString(R.string.create_seq_table));
	}

	public void dropTables(SQLiteDatabase db) {
		db.execSQL(resources.getString(R.string.drop_config_table));
		db.execSQL(resources.getString(R.string.drop_sequence_table));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		LogUtils.debug("Upgrading database");
		dropTables(db);
		createTables(db);
	}

	public void insertValue(String key, String value) {
		LogUtils.v("Inserting " + key + "|" + value);
		SQLiteDatabase db = getWritableDatabase();

		// SQLiteDatabase.openDatabase(path, factory, flags);
		String insert_value = resources.getString(R.string.insert_value);
		String[] args = new String[] { key, value };
		db.execSQL(insert_value, args);
		db.close();
	}

	public String getValue(String key) {
		LogUtils.v("Reading value for " + key);
		SQLiteDatabase db = getReadableDatabase();
		String select_value = resources.getString(R.string.select_value);
		String[] args = new String[] { key };
		Cursor cursor = db.rawQuery(select_value, args);
		String value = null;
		if (cursor.moveToNext()) {
			value = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return value;
	}

	public int getSeqNextValue(String name) {
		LogUtils.v("Reading Sequence " + name);
		SQLiteDatabase db = getReadableDatabase();
		String select_value = resources.getString(R.string.get_sequence);
		String[] args = new String[] { name };
		Cursor cursor = db.rawQuery(select_value, args);
		int value = 0;
		if (cursor.moveToNext()) {
			value = cursor.getInt(0);
		}
		cursor.close();
		db.close();
		db = getWritableDatabase();
		Object[] args2 = new Object[] { name, value + 1 };
		db.execSQL(resources.getString(R.string.update_sequence), args2);
		db.close();
		return value;
	}
}