package fake.domain.adamlopresto.gogive;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import fake.domain.adamlopresto.gogive.db.DatabaseHelper;
import fake.domain.adamlopresto.gogive.db.GiftsStoresTable;
import fake.domain.adamlopresto.gogive.db.GiftsStoresView;
import fake.domain.adamlopresto.gogive.db.GiftsTable;
import fake.domain.adamlopresto.gogive.db.RecipientsTable;
import fake.domain.adamlopresto.gogive.db.RecipientsView;
import fake.domain.adamlopresto.gogive.db.StoresTable;

public class GoGiveContentProvider extends ContentProvider {

	private DatabaseHelper helper;

	// Used for the UriMatcher
	// Odd numbers have an ID, evens don't.
	private static final int RECIPIENTS = 0;
	private static final int RECIPIENT_ID = 1;
	private static final int GIFTS = 2;
	private static final int GIFT_ID = 3;
	private static final int GIFTS_STORES = 4;
	private static final int GIFTS_STORES_ID = 5;
	private static final int STORES = 6;
	private static final int STORES_ID = 7;
	private static final int SHOPPING = 8;
	

	public static final String AUTHORITY = "fake.domain.adamlopresto.gogive.contentprovider";
	
	public static final Uri BASE = Uri.parse("content://"+AUTHORITY);

	private static final String RECIPIENT_BASE_PATH = "recipients";
	public static final Uri RECIPIENT_URI = Uri.withAppendedPath(BASE, RECIPIENT_BASE_PATH);
	private static final String GIFT_BASE_PATH = "gifts";
	public static final Uri GIFT_URI = Uri.withAppendedPath(BASE, GIFT_BASE_PATH);
	private static final String GIFTS_STORES_BASE_PATH = "gifts_stores";
	public static final Uri GIFTS_STORES_URI = Uri.withAppendedPath(BASE, GIFTS_STORES_BASE_PATH);
	private static final String STORES_BASE_PATH = "stores";
	public static final Uri STORES_URI = Uri.withAppendedPath(BASE, STORES_BASE_PATH);
	private static final String SHOPPING_BASE_PATH = "shopping";
	public static final Uri SHOPPING_URI = Uri.withAppendedPath(BASE, SHOPPING_BASE_PATH);

	
	/*
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/GoShopItems";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/GoShopItem";
	 */

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	
	static {
		sURIMatcher.addURI(AUTHORITY, RECIPIENT_BASE_PATH, RECIPIENTS);
		sURIMatcher.addURI(AUTHORITY, RECIPIENT_BASE_PATH+"/#", RECIPIENT_ID);
		sURIMatcher.addURI(AUTHORITY, GIFT_BASE_PATH, GIFTS);
		sURIMatcher.addURI(AUTHORITY, GIFT_BASE_PATH+"/#", GIFT_ID);
		sURIMatcher.addURI(AUTHORITY, GIFTS_STORES_BASE_PATH, GIFTS_STORES);
		sURIMatcher.addURI(AUTHORITY, GIFTS_STORES_BASE_PATH+"/#", GIFTS_STORES_ID);
		sURIMatcher.addURI(AUTHORITY, STORES_BASE_PATH, STORES);
		sURIMatcher.addURI(AUTHORITY, STORES_BASE_PATH+"/#", STORES_ID);
		sURIMatcher.addURI(AUTHORITY, SHOPPING_BASE_PATH, SHOPPING);
	}

	@Override
	public boolean onCreate() {
		helper = DatabaseHelper.getInstance(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		int uriType = sURIMatcher.match(uri);
		
		//If it's odd, then it has an ID appended.
		if ((uriType % 2) == 1){
			String id = uri.getLastPathSegment();
			selection = appendSelection(selection, "_id = ?");
			selectionArgs = appendSelectionArg(selectionArgs, id);
			uriType--;
		}
		
		switch (uriType) {
		case RECIPIENTS:
			queryBuilder.setTables(RecipientsView.VIEW);
			uri=RECIPIENT_URI;
			break;
		case GIFTS:
			queryBuilder.setTables(GiftsTable.TABLE);
			uri=GIFT_URI;
			break;
		case STORES:
			queryBuilder.setTables(StoresTable.TABLE);
			uri=STORES_URI;
			break;
		case GIFTS_STORES: 
			queryBuilder.setTables(GiftsStoresView.VIEW);
			uri=GIFTS_STORES_URI;
			break;
		case SHOPPING: {
			queryBuilder.setTables(GiftsStoresView.VIEW);
			Cursor cursor = queryBuilder.query(helper.getReadableDatabase(), 
					new String[]{"store as _id", "store_name", 
					"count(case when status='Purchased' then gift end) as purchased",
					"count(case when status='Planned' then gift end) as planned",
					}, 
					selection, selectionArgs, 
					"store, store_name", "purchased > 0 OR planned > 0", sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), GIFTS_STORES_URI);
			return cursor;
		}
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = helper.getWritableDatabase();
		int rowsUpdated = 0;
		
		//If it's odd, then it has an ID appended.
		if ((uriType % 2) == 1){
			String id = uri.getLastPathSegment();
			selection = appendSelection(selection, "_id = ?");
			selectionArgs = appendSelectionArg(selectionArgs, id);
			uriType--;
		}
		
		switch (uriType) {
		case RECIPIENTS:
			rowsUpdated = sqlDB.delete(RecipientsTable.TABLE, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(RECIPIENT_URI, null);
			getContext().getContentResolver().notifyChange(GIFT_URI, null);
			getContext().getContentResolver().notifyChange(GIFTS_STORES_URI, null);
			return rowsUpdated;
		case GIFTS:
			rowsUpdated = sqlDB.delete(GiftsTable.TABLE, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(GIFT_URI, null);
			getContext().getContentResolver().notifyChange(GIFTS_STORES_URI, null);
			return rowsUpdated;
		case GIFTS_STORES:
			rowsUpdated = sqlDB.delete(GiftsStoresTable.TABLE, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(GIFTS_STORES_URI, null);
			return rowsUpdated;
		case STORES:
			rowsUpdated = sqlDB.delete(StoresTable.TABLE, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(STORES_URI, null);
			getContext().getContentResolver().notifyChange(GIFTS_STORES_URI, null);
			return rowsUpdated;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = helper.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case RECIPIENTS:
			id = sqlDB.insertOrThrow(RecipientsTable.TABLE, null, values);
			break;
		case GIFTS:
			id = sqlDB.insertOrThrow(GiftsTable.TABLE, null, values);
			break;
		case GIFTS_STORES:
			id = sqlDB.insertOrThrow(GiftsStoresTable.TABLE, null, values);
			break;
		case STORES:
			id = sqlDB.insertOrThrow(StoresTable.TABLE, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.withAppendedPath(uri, String.valueOf(id));
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = helper.getWritableDatabase();
		int rowsUpdated = 0;
		
		//If it's odd, then it has an ID appended.
		if ((uriType % 2) == 1){
			String id = uri.getLastPathSegment();
			selection = appendSelection(selection, "_id = ?");
			selectionArgs = appendSelectionArg(selectionArgs, id);
			uriType--;
		}
		
		switch (uriType) {
		case RECIPIENTS:
			rowsUpdated = sqlDB.update(RecipientsTable.TABLE, values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(RECIPIENT_URI, null);
			getContext().getContentResolver().notifyChange(GIFT_URI, null);
			getContext().getContentResolver().notifyChange(GIFTS_STORES_URI, null);
			return rowsUpdated;
		case GIFTS:
			rowsUpdated = sqlDB.update(GiftsTable.TABLE, values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(GIFT_URI, null);
			getContext().getContentResolver().notifyChange(GIFTS_STORES_URI, null);
			return rowsUpdated;
		case GIFTS_STORES:
			rowsUpdated = sqlDB.update(GiftsStoresTable.TABLE, values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(GIFTS_STORES_URI, null);
			return rowsUpdated;
		case STORES:
			rowsUpdated = sqlDB.update(StoresTable.TABLE, values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(STORES_URI, null);
			getContext().getContentResolver().notifyChange(GIFTS_STORES_URI, null);
			return rowsUpdated;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	private static String appendSelection(String original, String newSelection){
		return DatabaseUtils.concatenateWhere(original, newSelection);
	}
	
	private static String[] appendSelectionArgs(String originalValues[], String newValues[]){
		if (originalValues == null){
			return newValues;
		}
		if (newValues == null){
			return originalValues;
		}
		return DatabaseUtils.appendSelectionArgs(originalValues, newValues);
	}
	
	private static String[] appendSelectionArg(String[] originalValues, String newValue){
		return appendSelectionArgs(originalValues, new String[]{newValue});
	}
	

}
