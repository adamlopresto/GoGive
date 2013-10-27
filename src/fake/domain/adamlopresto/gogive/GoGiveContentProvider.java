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
import fake.domain.adamlopresto.gogive.db.GiftsTable;
import fake.domain.adamlopresto.gogive.db.RecipientsTable;
/*
import fake.domain.adamlopresto.godo.db.ContextsTable;
import fake.domain.adamlopresto.godo.db.DatabaseHelper;
import fake.domain.adamlopresto.godo.db.InstanceDependencyTable;
import fake.domain.adamlopresto.godo.db.InstancesTable;
import fake.domain.adamlopresto.godo.db.InstancesView;
import fake.domain.adamlopresto.godo.db.RepetitionRulesTable;
import fake.domain.adamlopresto.godo.db.TasksTable;
*/

public class GoGiveContentProvider extends ContentProvider {

	private DatabaseHelper helper;

	// Used for the UriMatcher
	// Odd numbers have an ID, evens don't.
	private static final int RECIPIENTS = 0;
	private static final int RECIPIENT_ID = 1;
	private static final int GIFTS = 2;
	private static final int GIFT_ID = 3;
	

	public static final String AUTHORITY = "fake.domain.adamlopresto.gogive.contentprovider";
	
	public static final Uri BASE = Uri.parse("content://"+AUTHORITY);

	private static final String RECIPIENT_BASE_PATH = "recipients";
	public static final Uri RECIPIENT_URI = Uri.withAppendedPath(BASE, RECIPIENT_BASE_PATH);
	private static final String GIFT_BASE_PATH = "gifts";
	public static final Uri GIFT_URI = Uri.withAppendedPath(BASE, GIFT_BASE_PATH);
	
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
			queryBuilder.setTables(RecipientsTable.TABLE);
			break;
		case GIFTS:
			queryBuilder.setTables(GiftsTable.TABLE);
			break;
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
			return rowsUpdated;
		case GIFTS:
			rowsUpdated = sqlDB.delete(GiftsTable.TABLE, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(GIFT_URI, null);
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
			return rowsUpdated;
		case GIFTS:
			rowsUpdated = sqlDB.update(GiftsTable.TABLE, values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(GIFT_URI, null);
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
