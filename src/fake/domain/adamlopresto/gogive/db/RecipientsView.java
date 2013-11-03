package fake.domain.adamlopresto.gogive.db;

import android.database.sqlite.SQLiteDatabase;
import fake.domain.adamlopresto.gogive.Status;

public class RecipientsView {
	public static final String VIEW = "recipients_view";
	
	//as of version 1
	public static final String COLUMN_ID = RecipientsTable.COLUMN_ID;
	public static final String COLUMN_NAME = RecipientsTable.COLUMN_NAME;
	public static final String COLUMN_NOTES = RecipientsTable.COLUMN_NOTES;
	public static final String COLUMN_DONE = RecipientsTable.COLUMN_DONE;
	public static final String COLUMN_HIDDEN = RecipientsTable.COLUMN_HIDDEN;
	public static final String COLUMN_PLANNED = "planned";
	public static final String COLUMN_PURCHASED = "purchased";
	public static final String COLUMN_SPEND = "spend";
	
	public static final String[] ALL_COLUMNS = { COLUMN_ID, COLUMN_NAME, COLUMN_NOTES, COLUMN_DONE, COLUMN_HIDDEN, COLUMN_PLANNED, COLUMN_PURCHASED, COLUMN_SPEND };
	//private static final HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(ALL_COLUMNS));

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE VIEW "+VIEW + " AS SELECT "
				+ "r."+COLUMN_ID     + ", "
				+ "r."+COLUMN_NAME   + ", "
				+ "r."+COLUMN_NOTES  + ", "
				+ "r."+COLUMN_DONE   + ", "
				+ "r."+COLUMN_HIDDEN + ", "
				+ "COUNT(g1."+GiftsTable.COLUMN_ID+") as planned, "
				+ "COUNT(g2."+GiftsTable.COLUMN_ID+") as purchased, "
				+ "COALESCE(SUM(g1."+GiftsTable.COLUMN_PRICE+"),0)+COALESCE(SUM(g2."+GiftsTable.COLUMN_PRICE+"),0) as spend "
				+ "FROM " + RecipientsTable.TABLE + " AS r " 
				+ "LEFT OUTER JOIN " + GiftsTable.TABLE + " AS g1 "
				+ "ON r."+RecipientsTable.COLUMN_ID +"= g1."+GiftsTable.COLUMN_RECIPIENT
				+ " AND g1."+GiftsTable.COLUMN_STATUS+"='"+Status.Planned.toString()+"' "
				+ "LEFT OUTER JOIN " + GiftsTable.TABLE + " AS g2 "
				+ "ON r."+RecipientsTable.COLUMN_ID +"= g2."+GiftsTable.COLUMN_RECIPIENT
				+ " AND g2."+GiftsTable.COLUMN_STATUS+"='"+Status.Purchased.toString()+"' "
				+ "GROUP BY "
				+ "r."+COLUMN_ID     + ", "
				+ "r."+COLUMN_NAME   + ", "
				+ "r."+COLUMN_NOTES  + ", "
				+ "r."+COLUMN_DONE   + ", "
				+ "r."+COLUMN_HIDDEN
				);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}
}
