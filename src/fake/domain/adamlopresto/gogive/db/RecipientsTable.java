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
	public static final String COLUMN_DONE = "done";
	public static final String COLUMN_HIDDEN = "hidden";
	
	public static final String[] ALL_COLUMNS = { COLUMN_ID, COLUMN_NAME, COLUMN_NOTES, COLUMN_DONE, COLUMN_HIDDEN };
	private static final HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(ALL_COLUMNS));

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "+TABLE
				+ "(" 
				+ COLUMN_ID     + " integer primary key autoincrement, "
				+ COLUMN_NAME   + " text not null collate nocase unique, "
				+ COLUMN_NOTES  + " text, "
				+ COLUMN_DONE   + " boolean, "
				+ COLUMN_HIDDEN + " boolean"
				+ ")"
		);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}

	public static void checkColumns(String[] projection) {
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}
}
