package fake.domain.adamlopresto.gogive;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import fake.domain.adamlopresto.gogive.db.GiftsTable;

public class GiftActivity extends Activity {
	public static final String RECIPIENT_KEY = "recipient";
	public static final String GIFT_KEY = "gift";
	
	private long id = -1;
	private long recipient = -1;
	
	private EditText name;
	private EditText price;
	private EditText notes;
	private Spinner status;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gift);
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		name = (EditText)findViewById(R.id.name);
		price = (EditText)findViewById(R.id.price);
		notes = (EditText)findViewById(R.id.notes);
		status = (Spinner)findViewById(R.id.status);

		if (!extractFromBundle(savedInstanceState))
			extractFromBundle(getIntent().getExtras());
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		id = savedInstanceState.getLong(GIFT_KEY, -1L);
		recipient = savedInstanceState.getLong(RECIPIENT_KEY, -1L);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(GIFT_KEY, id);
		outState.putLong(RECIPIENT_KEY, recipient);
		super.onSaveInstanceState(outState);
	}

	private boolean extractFromBundle(Bundle b){
		if (b == null)
			return false;
		long tmpId;
		if ((tmpId = b.getLong(GIFT_KEY, -1L)) != -1L){
			id = tmpId;
			Cursor c = getContentResolver().query(GoGiveContentProvider.GIFT_URI, 
					new String[]{GiftsTable.COLUMN_NAME, GiftsTable.COLUMN_NOTES, 
						GiftsTable.COLUMN_PRICE, GiftsTable.COLUMN_RECIPIENT, 
						GiftsTable.COLUMN_STATUS
					}, 
					"_id = ?", new String[]{String.valueOf(id)}, null);
			c.moveToFirst();
			
			name.setText(c.getString(0));
			notes.setText(c.getString(1));
			price.setText(c.getString(2));
			recipient = c.getLong(3);
			String statStr = c.getString(4);
			Status stat = Status.valueOf(statStr);
			int ord = stat.ordinal();
			Log.e("GoGive", "String: '"+statStr+"', Status: '"+stat+"', ordinal: "+ord);
			Log.e("GoGive", "Values "+Status.Given+Status.Given.ordinal());
			Log.e("GoGive", "Values "+Status.Idea+Status.Idea.ordinal());
			Log.e("GoGive", "Values "+Status.Planned+Status.Planned.ordinal());
			Log.e("GoGive", "Values "+Status.Purchased+Status.Purchased.ordinal());
			Log.e("GoGive", "Values "+Status.Rejected+Status.Rejected.ordinal());
			status.setSelection(Status.valueOf(c.getString(4)).ordinal());

			return true;
		} else if ((tmpId = b.getLong(RECIPIENT_KEY, -1L)) != -1L){
			recipient = tmpId;
			status.setSelection(1);
		}
		
		return false;
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gift, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
//			NavUtils.navigateUpFromSameTask(this);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
