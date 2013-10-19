package fake.domain.adamlopresto.gogive.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "GoGive";
	
	/*
	 * Version history:
	 * 1: initial release
	 */	
	private static final int CURRENT_VERSION = 1;
	
	private static DatabaseHelper mInstance;
	
	public static DatabaseHelper getInstance(Context ctx) {
		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (mInstance == null) {
			mInstance = new DatabaseHelper(ctx.getApplicationContext());
		}
		return mInstance;
	}	
	
	
	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, CURRENT_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		RecipientsTable.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		RecipientsTable.onUpgrade(db, oldVersion, newVersion);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db){
		db.execSQL("PRAGMA foreign_keys = ON;");
	}

}
