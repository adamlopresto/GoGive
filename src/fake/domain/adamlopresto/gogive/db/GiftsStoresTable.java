package fake.domain.adamlopresto.gogive.db;

import java.util.Arrays;
import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

public class GiftsStoresTable {
	public static final String TABLE = "gifts_stores";
	
	//as of version 1
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_GIFT = "gift";
	public static final String COLUMN_STORE = "store";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE "+TABLE
				+ "(" 
				+ COLUMN_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_GIFT + " INTEGER NOT NULL REFERENCES "+GiftsTable.TABLE+" ON DELETE CASCADE, "
				+ COLUMN_STORE + " INTEGER NOT NULL REFERENCES "+StoresTable.TABLE+" ON DELETE CASCADE "
				+ ")"
		);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}

	public static void checkColumns(String[] projection) {
		String[] available = { COLUMN_ID, COLUMN_GIFT, COLUMN_STORE };
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}
}
