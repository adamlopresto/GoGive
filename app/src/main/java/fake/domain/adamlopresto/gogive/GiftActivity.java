package fake.domain.adamlopresto.gogive;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import fake.domain.adamlopresto.gogive.db.DatabaseHelper;
import fake.domain.adamlopresto.gogive.db.GiftsStoresTable;
import fake.domain.adamlopresto.gogive.db.GiftsStoresView;
import fake.domain.adamlopresto.gogive.db.GiftsTable;
import fake.domain.adamlopresto.gogive.db.StoresTable;

public class GiftActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	public static final String RECIPIENT_KEY = "recipient";
	public static final String GIFT_KEY = "gift";

	private long id = -1;
	private long recipient = -1;

	private EditText name;
	private EditText price;
	private EditText notes;
	private Spinner status;

	private boolean deleting = false;

	private SimpleCursorAdapter adapter;

	private static final int LOADER_GIFTS_STORES = 1;
	private static final int LOADER_STORES = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View header = getLayoutInflater().inflate(
				R.layout.activity_gift_header, null);
		ListView lv = getListView();

		lv.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		lv.addHeaderView(header, null, false);

		// Show the Up button in the action bar.
		setupActionBar();

		name = (EditText) header.findViewById(R.id.name);
		price = (EditText) header.findViewById(R.id.price);
		notes = (EditText) header.findViewById(R.id.notes);
		status = (Spinner) header.findViewById(R.id.status);

		View footer = getLayoutInflater().inflate(
				R.layout.activity_gift_footer, null);
		lv.addFooterView(footer, null, false);
		footer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getLoaderManager().restartLoader(LOADER_STORES, null,
						GiftActivity.this);
			}
		});

		// setListAdapter(new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, new String[]{"Walmart",
		// "Amazon"}));
		adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, null,
				new String[]{GiftsStoresView.COLUMN_STORE_NAME},
				new int[]{android.R.id.text1}, 0);
		setListAdapter(adapter);

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
										   int arg2, long giftStoreId) {
				getContentResolver().delete(GoGiveContentProvider.GIFTS_STORES_URI, "_id = ?", new String[]{String.valueOf(giftStoreId)});
				return true;
			}
		});

		if (!extractFromBundle(savedInstanceState))
			extractFromBundle(getIntent().getExtras());
	}

	@Override
	protected void onPause() {
		super.onPause();
		save();
	}

	private void save() {
		if (!deleting) {
			ContentValues cv = new ContentValues(6);
			cv.put(GiftsTable.COLUMN_RECIPIENT, recipient);
			cv.put(GiftsTable.COLUMN_NAME, name.getText().toString());
			cv.put(GiftsTable.COLUMN_PRICE, price.getText().toString());
			cv.put(GiftsTable.COLUMN_NOTES, notes.getText().toString());
			cv.put(GiftsTable.COLUMN_STATUS,
					Status.values()[status.getSelectedItemPosition()].name());
			cv.put(GiftsTable.COLUMN_DATE,
					DatabaseHelper.format.format(new Date()));

			if (id == -1L) {
				// new
				id = Long.parseLong(getContentResolver().insert(
						GoGiveContentProvider.GIFT_URI, cv)
						.getLastPathSegment());
			} else {
				// update
				getContentResolver().update(GoGiveContentProvider.GIFT_URI, cv,
						GiftsTable.COLUMN_ID + "=?",
						new String[]{String.valueOf(id)});
			}
		}
	}

	@Override
	protected void onResume() {
		getLoaderManager().restartLoader(LOADER_GIFTS_STORES, null, this);

		super.onResume();
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

	private boolean extractFromBundle(Bundle b) {
		if (b == null)
			return false;
		long tmpId;
		if ((tmpId = b.getLong(GIFT_KEY, -1L)) != -1L) {
			id = tmpId;
			Log.e("GoDo", "Opening item " + id);
			Cursor c = getContentResolver().query(
					GoGiveContentProvider.GIFT_URI,
					new String[]{GiftsTable.COLUMN_NAME,
							GiftsTable.COLUMN_NOTES, GiftsTable.COLUMN_PRICE,
							GiftsTable.COLUMN_RECIPIENT,
							GiftsTable.COLUMN_STATUS}, "_id = ?",
					new String[]{String.valueOf(id)}, null);
			c.moveToFirst();

			name.setText(c.getString(0));
			notes.setText(c.getString(1));
			price.setText(c.getString(2));
			recipient = c.getLong(3);
			status.setSelection(Status.valueOf(c.getString(4)).ordinal());

			return true;
		} else if ((tmpId = b.getLong(RECIPIENT_KEY, -1L)) != -1L) {
			recipient = tmpId;
		}

		return false;
	}

	/**
	 * Set up the {@link ActionBar}.
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
				// NavUtils.navigateUpFromSameTask(this);
				finish();
				return true;
			case R.id.delete:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.delete);
				builder.setMessage(R.string.confirm_delete);
				builder.setPositiveButton(R.string.delete,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								deleting = true;
								getContentResolver().delete(
										GoGiveContentProvider.GIFT_URI,
										GiftsTable.COLUMN_ID + "=?",
										new String[]{String.valueOf(id)});
								finish();
							}
						});
				builder.setNegativeButton(android.R.string.cancel, null);
				builder.show();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case LOADER_GIFTS_STORES:
				return new CursorLoader(this,
						GoGiveContentProvider.GIFTS_STORES_URI, new String[]{
						GiftsStoresView.COLUMN_ID,
						GiftsStoresView.COLUMN_STORE_NAME},
						GiftsStoresView.COLUMN_GIFT + "=? AND " + GiftsStoresView.COLUMN_STORE_NAME
								+ " IS NOT NULL",
						new String[]{String.valueOf(this.id)},
						GiftsStoresView.COLUMN_STORE_NAME);
			case LOADER_STORES:
				return new CursorLoader(this, GoGiveContentProvider.STORES_URI,
						new String[]{StoresTable.COLUMN_ID,
								StoresTable.COLUMN_NAME}, null, null,
						StoresTable.COLUMN_NAME);
			default:
				return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
		switch (loader.getId()) {
			case LOADER_GIFTS_STORES:
				adapter.swapCursor(cursor);
				return;
			case LOADER_STORES:
				save();
				final AlertDialog.Builder builder = new AlertDialog.Builder(GiftActivity.this);
				builder.setCursor(cursor, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						cursor.moveToPosition(which);
						ContentValues values = new ContentValues(2);
						values.put(GiftsStoresTable.COLUMN_GIFT, GiftActivity.this.id);
						values.put(GiftsStoresTable.COLUMN_STORE, cursor.getLong(0));
						try {
							getContentResolver().insert(GoGiveContentProvider.GIFTS_STORES_URI, values);
							getLoaderManager().restartLoader(LOADER_GIFTS_STORES, null, GiftActivity.this);
						} catch (SQLiteConstraintException ignored) {
							//NOOP
						}
						cursor.close();
					}
				}, StoresTable.COLUMN_NAME);
				builder.setTitle(R.string.choose_a_store);
				builder.setNegativeButton(android.R.string.cancel, null);
				builder.setNeutralButton(R.string.create_a_new_store, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, int which) {
						AlertDialog.Builder innerBuilder = new AlertDialog.Builder(GiftActivity.this);
						final TextView newStoreName = (TextView) getLayoutInflater().inflate(R.layout.new_store_dialog, null);
						innerBuilder.setView(newStoreName);
						innerBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface innerDialog, int which) {
								String name = newStoreName.getText().toString();
								ContentValues values = new ContentValues(2);
								values.put(StoresTable.COLUMN_NAME, name);
								try {
									Uri newStore = getContentResolver().insert(GoGiveContentProvider.STORES_URI, values);
									long storeId = Long.parseLong(newStore.getLastPathSegment());
									values.clear();
									values.put(GiftsStoresTable.COLUMN_STORE, storeId);
									values.put(GiftsStoresTable.COLUMN_GIFT, id);
									getContentResolver().insert(GoGiveContentProvider.GIFTS_STORES_URI, values);
									getLoaderManager().restartLoader(LOADER_GIFTS_STORES, null, GiftActivity.this);
									Toast.makeText(GiftActivity.this, "Added to store", Toast.LENGTH_SHORT).show();
								} catch (Exception e) {
									Toast.makeText(GiftActivity.this, "Failed to create store: " + e, Toast.LENGTH_LONG).show();
									Log.e("GoGive", "Failed to create store: " + e);
								}
								dialog.dismiss();
							}
						});
						innerBuilder.setNegativeButton(android.R.string.cancel, null);
						innerBuilder.show();
					}
				});
				builder.show();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
			case LOADER_GIFTS_STORES:
				adapter.swapCursor(null);
			default:
				// NOOP
		}
	}
}
