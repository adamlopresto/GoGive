package fake.domain.adamlopresto.gogive;

import java.text.NumberFormat;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import fake.domain.adamlopresto.gogive.db.GiftsStoresView;
import fake.domain.adamlopresto.gogive.db.GiftsTable;

public class ShoppingActivity extends ExpandableListActivity implements LoaderManager.LoaderCallbacks<Cursor>{
	
	private ShoppingExpandableListAdapter adapter;
	
	private int lastPosition = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null)
			lastPosition = savedInstanceState.getInt("lastPosition", -1);

		adapter = new ShoppingExpandableListAdapter(this, getLoaderManager());
		
		setListAdapter(adapter);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		getExpandableListView().setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				final long realId = ((Long)view.getTag()).longValue();
				long packedPosition = getExpandableListView().getExpandableListPosition(position);
				
				if (ExpandableListView.getPackedPositionType(packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_GROUP
						&& realId != -2L){
					
					AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingActivity.this)
						.setTitle("Delete store")
						.setMessage("Are you sure you want to delete this store? id is "+realId)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								getContentResolver().delete(GoGiveContentProvider.STORES_URI, 
										"_id = ?", new String[]{String.valueOf(realId)});
								adapter.setGroupCursor(null);
								getLoaderManager().restartLoader(-1, null, ShoppingActivity.this);
							}
						});
					builder.show();
					

					return true;
				} 
				return false;
			}
		});
		
	}
	
	@Override
	protected void onResume() {
		getLoaderManager().restartLoader(-1, null, this);
		
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("lastPosition", lastPosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		lastPosition = groupPosition;
		startActivity(new Intent(this, GiftActivity.class).putExtra(GiftActivity.GIFT_KEY, 
				((Long)v.getTag()).longValue()));
		return true;
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, GoGiveContentProvider.SHOPPING_URI,
				new String[]{"_id", "store_name", "purchased", "planned"}, 
				null, null, "store_name is null, store_name");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		/*
		MatrixCursor lastStore = new MatrixCursor(new String[]{StoresTable.COLUMN_ID, StoresTable.COLUMN_NAME}, 1);
		lastStore.addRow(new Object[]{-2L, "No store"});
		MergeCursor mergeCursor = new MergeCursor(new Cursor[]{c, lastStore});
		adapter.setGroupCursor(mergeCursor);
		*/
		adapter.setGroupCursor(c);

		if (lastPosition != -1){
			ExpandableListView lv = getExpandableListView();
			lv.expandGroup(lastPosition);
			lv.setSelection(lv.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(lastPosition)));
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.setGroupCursor(null);
	}

}

class ShoppingExpandableListAdapter extends SimpleCursorTreeAdapter implements LoaderManager.LoaderCallbacks<Cursor> {
    private Context mContext;
    private LoaderManager mManager;

    public ShoppingExpandableListAdapter(
            Context context, LoaderManager manager) {
        super(context, null, android.R.layout.simple_expandable_list_item_1, new String[]{"store_name"}, 
        		new int[]{android.R.id.text1}, 
        		R.layout.shopping_gift_item, null, null);
        mContext  = context;
        mManager  = manager;
    }
    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
    	int col = groupCursor.getColumnIndex("_id");
    	final long idGroup = groupCursor.isNull(col) 
    			? -2L
    			: groupCursor.getLong(col);
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
        long idGroup = bundle.getLong("idGroup");
        if (idGroup == -2L){
        	return new CursorLoader(
                mContext,
                GoGiveContentProvider.GIFTS_STORES_URI,
				new String[]{GiftsStoresView.COLUMN_ID, GiftsTable.COLUMN_STATUS, 
                		GiftsStoresView.COLUMN_GIFT_NAME, GiftsTable.COLUMN_PRICE, 
                		GiftsStoresView.COLUMN_GIFT_NOTES, GiftsStoresView.COLUMN_GIFT, 
                		GiftsStoresView.COLUMN_RECIPIENT_NAME}, 
               GiftsStoresView.COLUMN_STORE + " IS NULL AND status in ('"+Status.Purchased+"','"+
                		Status.Planned+"')",
                null,
                GiftsStoresView.COLUMN_STATUS
	        );
        } else {
        	return new CursorLoader(
                mContext,
                GoGiveContentProvider.GIFTS_STORES_URI,
				new String[]{GiftsStoresView.COLUMN_ID, GiftsTable.COLUMN_STATUS, 
                		GiftsStoresView.COLUMN_GIFT_NAME, GiftsTable.COLUMN_PRICE, 
                		GiftsStoresView.COLUMN_GIFT_NOTES, GiftsStoresView.COLUMN_GIFT, 
                		GiftsStoresView.COLUMN_RECIPIENT_NAME}, 
               GiftsStoresView.COLUMN_STORE + " = ? AND status in ('"+Status.Purchased+"','"+
                		Status.Planned+"')",
                new String[]{String.valueOf(idGroup)},
                GiftsStoresView.COLUMN_STATUS
	        );
        }

    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        setChildrenCursor(loader.getId(), cursor);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

	/* (non-Javadoc)
	 * @see android.widget.SimpleCursorTreeAdapter#bindGroupView(android.view.View, android.content.Context, android.database.Cursor, boolean)
	 */
	@Override
	protected void bindGroupView(View view, Context context, Cursor cursor,
			boolean isExpanded) {
		
		super.bindGroupView(view, context, cursor, isExpanded);

		view.setTag(Long.valueOf(cursor.getLong(0)));
		TextView tv = (TextView)view.findViewById(android.R.id.text1);
		if (cursor.getInt(cursor.getColumnIndexOrThrow("planned")) > 0)
			tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
		else
			tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		
		if (cursor.isNull(cursor.getColumnIndexOrThrow("store_name"))){
			tv.setText("No store");
		}

	}

	@Override
	protected void bindChildView(View view, Context context, Cursor cursor,
			boolean isLastChild) {
		
		boolean strikethru = Status.Purchased.toString()
				.equals(cursor.getString(
						cursor.getColumnIndexOrThrow(GiftsStoresView.COLUMN_STATUS)));

		updateText(view, R.id.recipient, cursor.getString(6), strikethru);
		updateText(view, R.id.name,      cursor.getString(2), strikethru);
		updateText(view, R.id.price, NumberFormat.getCurrencyInstance().format(cursor.getDouble(3)), 
				strikethru);
		updateText(view, R.id.notes,     cursor.getString(4), strikethru);
		
		view.setTag(Long.valueOf(cursor.getLong(5)));
	}
	
	private void updateText(View parent, int resource, String text, boolean strikethru){
		TextView tv = ((TextView)parent.findViewById(resource));
		if (TextUtils.isEmpty(text))
			tv.setVisibility(View.GONE);
		else {
			tv.setVisibility(View.VISIBLE);
			tv.setText(text);
			if (strikethru)
				tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			else
				tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
		}
		
	}
    
    
}
