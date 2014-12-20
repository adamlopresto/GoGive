package fake.domain.adamlopresto.gogive.db;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "GoGive";
	
	public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	
	/*
	 * Version history:
	 * 1: initial release
	 * 2: add GiftsStoresView
	 * 3: add recipient to GiftsStoresView
	 * 4: change RecipientsView
	 */	
	private static final int CURRENT_VERSION = 4;
	
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
		super(context, context.getExternalFilesDir(null)+"/"+DATABASE_NAME, null, CURRENT_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		RecipientsTable.onCreate(db);
		GiftsTable.onCreate(db);
		RecipientsView.onCreate(db);
		StoresTable.onCreate(db);
		GiftsStoresTable.onCreate(db);
		GiftsStoresView.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		RecipientsTable.onUpgrade(db, oldVersion, newVersion);
		GiftsTable.onUpgrade(db, oldVersion, newVersion);
		RecipientsView.onUpgrade(db, oldVersion, newVersion);
		StoresTable.onUpgrade(db, oldVersion, newVersion);
		GiftsStoresTable.onUpgrade(db, oldVersion, newVersion);
		GiftsStoresView.onUpgrade(db, oldVersion, newVersion);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db){
		db.execSQL("PRAGMA foreign_keys = ON;");
	}

}
