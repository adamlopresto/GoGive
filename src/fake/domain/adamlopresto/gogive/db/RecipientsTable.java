package fake.domain.adamlopresto.gogive.db;

import java.util.Arrays;
import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

public class RecipientsTable {
	public static final String TABLE = "recipients";
	
	//as of version 1
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_NOTES = "notes";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "+TABLE
				+ "(" 
				+ COLUMN_ID    + " integer primary key autoincrement, "
				+ COLUMN_NAME  + " text not null collate nocase unique, "
				+ COLUMN_NOTES + " text, "
				+ ")"
		);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}

	public static void checkColumns(String[] projection) {
		String[] available = { COLUMN_ID, COLUMN_NAME, COLUMN_NOTES };
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
