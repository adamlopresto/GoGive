package fake.domain.adamlopresto.gogive.db;

import java.util.Arrays;
import java.util.HashSet;

import fake.domain.adamlopresto.gogive.Status;

import android.database.sqlite.SQLiteDatabase;

public class GiftsTable {
	public static final String TABLE = "gifts";
	
	//as of version 1
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	
	//The status references the Status enum.
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_RECIPIENT = "recipient";

	//Price is stored in cents, because integers are nicer to deal with.
	public static final String COLUMN_PRICE = "price";
	public static final String COLUMN_NOTES = "notes";
	
	//the most date planned, given, or rejected
	public static final String COLUMN_DATE = "date";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE "+TABLE
				+ "(" 
				+ COLUMN_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_NAME  + " TEXT NOT NULL COLLATE NOCASE, "
				+ COLUMN_STATUS + " TEXT NOT NULL DEFAULT '"+ Status.Idea.toString()+"',"
				+ COLUMN_RECIPIENT + " INTEGER NOT NULL REFERENCES "+RecipientsTable.TABLE+" ON DELETE CASCADE, "
				+ COLUMN_PRICE + " INTEGER, "
				+ COLUMN_NOTES + " TEXT, "
				+ COLUMN_DATE + " TEXT DEFAULT CURRENT_DATE"
				+ ")"
		);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}

	public static void checkColumns(String[] projection) {
		String[] available = { COLUMN_ID, COLUMN_NAME, COLUMN_STATUS, 
				COLUMN_RECIPIENT, COLUMN_PRICE, COLUMN_NOTES };
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
