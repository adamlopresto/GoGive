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
				+ "(SELECT COUNT(*) FROM gifts WHERE recipient = r._id AND status='Planned') as planned, "
                + "(SELECT COUNT(*) FROM gifts WHERE recipient = r._id AND status='Purchased') as purchased, "
				+ "COALESCE(" +
                        "(SELECT SUM("+GiftsTable.COLUMN_PRICE+") " +
                        "FROM gifts " +
                        "WHERE recipient = r._id " +
                        "AND status in ('Planned', 'Purchased')" +
                        "),0) as spend "
				+ "FROM " + RecipientsTable.TABLE + " AS r " 
				);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        if (oldVersion < 4){
            db.execSQL("DROP VIEW "+VIEW);
            onCreate(db);
        }
	}
}
