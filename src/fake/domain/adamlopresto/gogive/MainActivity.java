package fake.domain.adamlopresto.gogive;

import java.text.NumberFormat;

import android.app.ExpandableListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import fake.domain.adamlopresto.gogive.db.GiftsTable;
import fake.domain.adamlopresto.gogive.db.RecipientsTable;

public class MainActivity extends ExpandableListActivity implements LoaderManager.LoaderCallbacks<Cursor>{
	
	private ExpandableListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		
		adapter = new ExpandableListAdapter(this, getLoaderManager(), 
				R.layout.main_item, 
				new String[]{RecipientsTable.COLUMN_NAME, RecipientsTable.COLUMN_DONE, RecipientsTable.COLUMN_NOTES, "spend", "summary"}, 
				new int[]{R.id.recipient, R.id.recipient, R.id.recipient_notes, R.id.total_spend, R.id.summary},
				R.layout.gift_item, 
				new String[]{GiftsTable.COLUMN_STATUS, GiftsTable.COLUMN_NAME, GiftsTable.COLUMN_PRICE, GiftsTable.COLUMN_NOTES}, 
				new int[]{R.id.status, R.id.name, R.id.price, R.id.notes}
				);
		
		setListAdapter(adapter);
		
	}
	
	@Override
	protected void onResume() {
		getLoaderManager().restartLoader(-1, null, this);
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, GoGiveContentProvider.RECIPIENT_URI, 
				new String[]{RecipientsTable.COLUMN_ID, RecipientsTable.COLUMN_NAME, RecipientsTable.COLUMN_NOTES, RecipientsTable.COLUMN_DONE, 
				"42 as spend", "'Planned: 5, purchased: 3' as summary"}, 
				"hidden IS NULL OR NOT hidden", null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		adapter.setGroupCursor(c);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.setGroupCursor(null);
	}

}

class ExpandableListAdapter extends SimpleCursorTreeAdapter implements LoaderManager.LoaderCallbacks<Cursor> {
    private Context mContext;
    private LoaderManager mManager;

    public ExpandableListAdapter(
            Context context, LoaderManager manager, 
            int groupLayout, String[] groupFrom, int[] groupTo,
            int childLayout, String[] childFrom, int[] childTo) {
        super(context, null, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        mContext  = context;
        mManager  = manager;
    }
    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
		DatabaseUtils.dumpCursor(groupCursor);
		Log.e("GoGive", "current position: "+groupCursor.getPosition());
        final long idGroup = groupCursor.getLong(groupCursor.getColumnIndex("_id"));
        Bundle bundle = new Bundle();
        bundle.putLong("idGroup", idGroup);
        int groupPos = groupCursor.getPosition();
        if (mManager.getLoader(groupPos) != null && !mManager.getLoader(groupPos).isReset()) {
            mManager.restartLoader(groupPos, bundle, this);
        }
        else {
            mManager.initLoader(groupPos, bundle, this);
        }
        return null;
    }
    @Override
    public Loader<Cursor> onCreateLoader(int groupPos, Bundle bundle) {
    	Log.e("GoGive", "creating loader for pos "+groupPos+" and bundle" + bundle.toString());
        long idGroup = bundle.getLong("idGroup");
        return new CursorLoader(
                mContext,
                GoGiveContentProvider.GIFT_URI,
				new String[]{GiftsTable.COLUMN_ID, GiftsTable.COLUMN_STATUS, 
                		GiftsTable.COLUMN_NAME, GiftsTable.COLUMN_PRICE, 
                		GiftsTable.COLUMN_NOTES}, 
                GiftsTable.COLUMN_RECIPIENT + " = ?",
                new String[]{String.valueOf(idGroup)},
                null //sort order
        );
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    	Log.e("GoGive", "Loader id: "+loader.getId());
        setChildrenCursor(loader.getId(), cursor);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
	/* (non-Javadoc)
	 * @see android.widget.SimpleCursorTreeAdapter#bindChildView(android.view.View, android.content.Context, android.database.Cursor, boolean)
	 */
	@Override
	protected void bindChildView(View view, Context context, Cursor cursor,
			boolean isLastChild) {
		super.bindChildView(view, context, cursor, isLastChild);
		
		((TextView)view.findViewById(R.id.status)).setText(cursor.getString(1));
		((TextView)view.findViewById(R.id.name)).setText(cursor.getString(2));
		((TextView)view.findViewById(R.id.price)).setText(NumberFormat.getCurrencyInstance().format(cursor.getDouble(3)));
		TextView notes = ((TextView)view.findViewById(R.id.notes));
		String notesStr = cursor.getString(4);
		if (TextUtils.isEmpty(notesStr)){
			notes.setVisibility(View.GONE);
		} else {
			notes.setVisibility(View.VISIBLE);
			notes.setText(notesStr);
		}


	}

	/* (non-Javadoc)
	 * @see android.widget.SimpleCursorTreeAdapter#bindGroupView(android.view.View, android.content.Context, android.database.Cursor, boolean)
	 */
	@Override
	protected void bindGroupView(View view, Context context, Cursor cursor,
			boolean isExpanded) {
		super.bindGroupView(view, context, cursor, isExpanded);
		CheckBox rcptBox = (CheckBox)view.findViewById(R.id.recipient);
		rcptBox.setText(cursor.getString(1));
		rcptBox.setChecked(cursor.getInt(3) == 0);
		TextView notes = (TextView)view.findViewById(R.id.recipient_notes);
		String notesStr = cursor.getString(2);
		if (TextUtils.isEmpty(notesStr)){
			notes.setVisibility(View.GONE);
		} else {
			notes.setVisibility(View.VISIBLE);
			notes.setText(notesStr);
		}
		((TextView)view.findViewById(R.id.total_spend)).setText(NumberFormat.getCurrencyInstance().format(cursor.getDouble(4)));
		((TextView)view.findViewById(R.id.summary)).setText(cursor.getString(5));
		
	}
    
    
}

