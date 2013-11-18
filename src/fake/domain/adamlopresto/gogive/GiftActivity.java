package fake.domain.adamlopresto.gogive;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
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
	
	private boolean deleting = false;;

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
		super.onPause();
		if (!deleting){
			ContentValues cv = new ContentValues(5);
			cv.put(GiftsTable.COLUMN_RECIPIENT, recipient);
			cv.put(GiftsTable.COLUMN_NAME, name.getText().toString());
			cv.put(GiftsTable.COLUMN_PRICE, price.getText().toString());
			cv.put(GiftsTable.COLUMN_NOTES, notes.getText().toString());
			cv.put(GiftsTable.COLUMN_STATUS, Status.values()[status.getSelectedItemPosition()].name());

			if (id == -1L){
				//new
				id = Long.parseLong(getContentResolver().insert(GoGiveContentProvider.GIFT_URI, cv).getLastPathSegment());
			} else {
				//update
				getContentResolver().update(GoGiveContentProvider.GIFT_URI, cv, 
						GiftsTable.COLUMN_ID + "=?",
						new String[]{String.valueOf(id)});
			}
		}
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
			status.setSelection(Status.valueOf(c.getString(4)).ordinal());

			return true;
		} else if ((tmpId = b.getLong(RECIPIENT_KEY, -1L)) != -1L){
			recipient = tmpId;
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
		getMenuInflater().inflate(R.menu.delete, menu);
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
		case R.id.delete:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.delete);
			builder.setMessage(R.string.confirm_delete);
			builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleting = true;
					getContentResolver().delete(GoGiveContentProvider.GIFT_URI, 
							GiftsTable.COLUMN_ID+"=?", new String[]{String.valueOf(id)});
					finish();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, null);
			builder.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
