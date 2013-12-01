package fake.domain.adamlopresto.gogive;

import java.text.NumberFormat;

import android.app.ExpandableListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import fake.domain.adamlopresto.gogive.db.GiftsStoresView;
import fake.domain.adamlopresto.gogive.db.GiftsTable;
import fake.domain.adamlopresto.gogive.db.StoresTable;

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
		
		/*
		getExpandableListView().setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				long packedPosition = getExpandableListView().getExpandableListPosition(position);
				
				if (ExpandableListView.getPackedPositionType(packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_GROUP){
//					Cursor o = (Cursor)getExpandableListAdapter().getGroup(ExpandableListView.getPackedPositionGroup(id));
					
					lastPosition = ExpandableListView.getPackedPositionGroup(packedPosition);

					long recipientId = adapter.getGroupId(ExpandableListView.getPackedPositionGroup(packedPosition));
					startActivity(new Intent(ShoppingActivity.this, RecipientActivity.class).putExtra(RecipientActivity.KEY, recipientId));

					return true;
				} 
				return false;
			}
		});
		*/
		
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*
		switch(item.getItemId()){
		case R.id.action_show:
			showEveryone = !showEveryone;
			item.setChecked(showEveryone);
			getLoaderManager().restartLoader(-1, null, this);
			return true;

		}
		*/
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, GoGiveContentProvider.STORES_URI,
				new String[]{StoresTable.COLUMN_ID, StoresTable.COLUMN_NAME}, 
				"_id IN (SELECT store FROM gifts_stores gs INNER JOIN gifts g ON g._id = gs.gift "+
				"WHERE g.status in ('Planned', 'Purchased'))", 
				null, StoresTable.COLUMN_NAME);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
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
        super(context, null, android.R.layout.simple_expandable_list_item_1, new String[]{StoresTable.COLUMN_NAME}, 
        		new int[]{android.R.id.text1}, 
        		R.layout.gift_item, null, null);
        mContext  = context;
        mManager  = manager;
    }
    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
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
        long idGroup = bundle.getLong("idGroup");
        return new CursorLoader(
                mContext,
                GoGiveContentProvider.GIFTS_STORES_URI,
				new String[]{GiftsStoresView.COLUMN_ID, GiftsTable.COLUMN_STATUS, 
                		GiftsStoresView.COLUMN_GIFT_NAME, GiftsTable.COLUMN_PRICE, 
                		GiftsStoresView.COLUMN_GIFT_NOTES, GiftsStoresView.COLUMN_GIFT}, 
               GiftsStoresView.COLUMN_STORE + " = ? AND status in ('"+Status.Purchased+"','"+
                		Status.Planned+"')",
                new String[]{String.valueOf(idGroup)},
                GiftsStoresView.COLUMN_STATUS
        );
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        setChildrenCursor(loader.getId(), cursor);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

	@Override
	protected void bindChildView(View view, Context context, Cursor cursor,
			boolean isLastChild) {
		
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
		
		view.setTag(Long.valueOf(cursor.getLong(5)));


	}
    
    
}
