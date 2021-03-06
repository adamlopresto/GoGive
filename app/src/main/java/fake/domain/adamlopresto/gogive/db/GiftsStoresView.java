package fake.domain.adamlopresto.gogive.db;

import android.database.sqlite.SQLiteDatabase;

public class GiftsStoresView {
public static final String VIEW = "gifts_stores_view";
	
	//as of version 2
	public static final String COLUMN_ID = GiftsStoresTable.COLUMN_ID;
	public static final String COLUMN_GIFT = GiftsStoresTable.COLUMN_GIFT;
	public static final String COLUMN_STORE = GiftsStoresTable.COLUMN_STORE;
	public static final String COLUMN_GIFT_NAME = "gift_name";
	public static final String COLUMN_GIFT_NOTES = "gift_notes";
	public static final String COLUMN_STATUS = GiftsTable.COLUMN_STATUS;
	public static final String COLUMN_PRICE = GiftsTable.COLUMN_PRICE;
	public static final String COLUMN_STORE_NAME = "store_name";

	//as of version 3
	public static final String COLUMN_RECIPIENT_NAME = "recipient_name";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE VIEW "+VIEW+" AS SELECT "
				+ "gs."+COLUMN_ID + " as "+COLUMN_ID+", "
				+ "g._id as "+COLUMN_GIFT + ", "
				+ COLUMN_STORE + ", "
				+ "g."+GiftsTable.COLUMN_NAME+" AS "+COLUMN_GIFT_NAME+", "
				+ "g."+GiftsTable.COLUMN_NOTES+" AS "+COLUMN_GIFT_NOTES+", "
				+ COLUMN_STATUS + ", "
				+ COLUMN_PRICE + ", "
				+ "s."+StoresTable.COLUMN_NAME+" AS "+COLUMN_STORE_NAME+", "
				+ "r."+RecipientsTable.COLUMN_NAME+" AS "+COLUMN_RECIPIENT_NAME
				+ " FROM "+GiftsTable.TABLE+" AS g"
				+ " INNER JOIN "+RecipientsTable.TABLE+" as r "
				+ " ON g."+GiftsTable.COLUMN_RECIPIENT+"=r."+RecipientsTable.COLUMN_ID
				+ " LEFT OUTER JOIN "+GiftsStoresTable.TABLE+" AS gs "
				+ " ON gs."+GiftsStoresTable.COLUMN_GIFT+"=g."+GiftsTable.COLUMN_ID
				+ " LEFT OUTER JOIN "+StoresTable.TABLE+" AS s"
				+ " ON gs."+GiftsStoresTable.COLUMN_STORE+"=s."+StoresTable.COLUMN_ID
		);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		if (oldVersion == 2)
			dropView(db);
		if (oldVersion < 3)
			onCreate(db);
	}
	
	private static void dropView(SQLiteDatabase db){
		db.execSQL("DROP VIEW "+VIEW);
	}

}
