package com.zwc.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherDatabaseHelper extends SQLiteOpenHelper {
	public WeatherDatabaseHelper(Context context, String name, int version) {
		super(context, name, null, version);
	}

	private static final int DATABASE_VERSION = 1;
	private static final String DICTIONARY_TABLE_NAME = "areaid";
	private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE "
			+ DICTIONARY_TABLE_NAME + " (" + "_id "
			+ "INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + "areaid "
			+ "VARCHAR(20) NOT NULL," + "namecn " + "VARCHAR(20) NOT NULL)";

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

	}

}
