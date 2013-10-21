package fake.domain.adamlopresto.gogive;

import fake.domain.adamlopresto.gogive.db.RecipientsTable;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

public class RecipientActivity extends Activity {
	
	private long id = -1L;
	private CheckBox done;
	private EditText name;
	private EditText notes;
	private CheckBox hidden;
	
	public static final String KEY = "recipient";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipient);
		// Show the Up button in the action bar.
		setupActionBar();
		
		done = (CheckBox)findViewById(R.id.done);
		name = (EditText)findViewById(R.id.name);
		notes = (EditText)findViewById(R.id.notes);
		hidden = (CheckBox)findViewById(R.id.hidden);
		
		if (!extractFromBundle(savedInstanceState)){
			if (getIntent() != null)
				extractFromBundle(getIntent().getExtras());
		}
	}
	
	private boolean extractFromBundle(Bundle b){
		if (b == null)
			return false;
		id = b.getLong(KEY, -1);
		if (id == -1L)
			return false;

		Cursor c = getContentResolver().query(GoGiveContentProvider.RECIPIENT_URI, 
				RecipientsTable.ALL_COLUMNS, RecipientsTable.COLUMN_ID + "=?", 
				new String[]{String.valueOf(id)}, null);
		
		c.moveToFirst();
		done.setText(c.getString(c.getColumnIndexOrThrow(RecipientsTable.COLUMN_DONE)));
		name.setText(c.getString(c.getColumnIndexOrThrow(RecipientsTable.COLUMN_NAME)));
		notes.setText(c.getString(c.getColumnIndexOrThrow(RecipientsTable.COLUMN_NOTES)));
		hidden.setText(c.getString(c.getColumnIndexOrThrow(RecipientsTable.COLUMN_HIDDEN)));

		return true;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		extractFromBundle(savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putLong(KEY, id);
		super.onSaveInstanceState(outState);
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
		getMenuInflater().inflate(R.menu.recipient, menu);
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
			//NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
