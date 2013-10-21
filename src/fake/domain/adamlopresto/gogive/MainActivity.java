package fake.domain.adamlopresto.gogive;

import java.text.NumberFormat;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import fake.domain.adamlopresto.gogive.db.RecipientsTable;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>{
	
	private SimpleCursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		adapter = new SimpleCursorAdapter(this, R.layout.main_item, null,
				new String[]{RecipientsTable.COLUMN_NAME, RecipientsTable.COLUMN_NOTES, RecipientsTable.COLUMN_DONE, "spend", "summary"}, 
				new int[]{R.id.recipient, R.id.recipient_notes, R.id.recipient, R.id.total_spend, R.id.summary}, 0);
		
		adapter.setViewBinder(new ViewBinder(){
			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				switch (columnIndex){
				case 2:
					view.setVisibility(TextUtils.isEmpty(cursor.getString(columnIndex)) ? View.GONE : View.VISIBLE);
					return false;
				case 3:
					((CheckBox)view).setChecked(!cursor.isNull(columnIndex) && cursor.getInt(columnIndex) != 0);
					return true;
				case 4:
					((TextView)view).setText(NumberFormat.getCurrencyInstance().format(cursor.getDouble(columnIndex)));
					return true;
				}
				return false;
			}
			
		});
		
		setListAdapter(adapter);
		
	}
	
	/* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		startActivity(new Intent(this, RecipientActivity.class).putExtra(RecipientActivity.KEY, id));
		super.onListItemClick(l, v, position, id);
	}

	@Override
	protected void onResume() {
		getLoaderManager().restartLoader(0, null, this);
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
				new String[]{RecipientsTable.COLUMN_ID, RecipientsTable.COLUMN_NAME, RecipientsTable.COLUMN_NOTES, RecipientsTable.COLUMN_DONE, "42 as spend", "'Planned: 5, purchased: 3' as summary"}, 
				"hidden IS NULL OR NOT hidden", null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		DatabaseUtils.dumpCursor(c);
		adapter.swapCursor(c);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

}
