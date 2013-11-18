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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import fake.domain.adamlopresto.gogive.db.GiftsTable;
import fake.domain.adamlopresto.gogive.db.RecipientsView;

public class MainActivity extends ExpandableListActivity implements LoaderManager.LoaderCallbacks<Cursor>{
	
	private ExpandableListAdapter adapter;
	private boolean showEveryone = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new ExpandableListAdapter(this, getLoaderManager());
		
		ExpandableListView lv = getExpandableListView();
		View footer = View.inflate(this, R.layout.main_footer, null);
		lv.addFooterView(footer, null, false);
		footer.findViewById(R.id.new_recipient).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, RecipientActivity.class));
			}
		});

		setListAdapter(adapter);
		
		getExpandableListView().setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				long packedPosition = getExpandableListView().getExpandableListPosition(position);
				
				if (ExpandableListView.getPackedPositionType(packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_GROUP){
//					Cursor o = (Cursor)getExpandableListAdapter().getGroup(ExpandableListView.getPackedPositionGroup(id));

					long recipientId = adapter.getGroupId(ExpandableListView.getPackedPositionGroup(packedPosition));
					startActivity(new Intent(MainActivity.this, RecipientActivity.class).putExtra(RecipientActivity.KEY, recipientId));

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
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		startActivity(new Intent(this, GiftActivity.class).putExtra(GiftActivity.GIFT_KEY, id));
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
		switch(item.getItemId()){
		case R.id.action_show:
			showEveryone = !showEveryone;
			item.setChecked(showEveryone);
			getLoaderManager().restartLoader(-1, null, this);
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, GoGiveContentProvider.RECIPIENT_URI, 
				new String[]{RecipientsView.COLUMN_ID, RecipientsView.COLUMN_NAME, RecipientsView.COLUMN_NOTES, RecipientsView.COLUMN_DONE, 
				RecipientsView.COLUMN_SPEND, RecipientsView.COLUMN_PLANNED, RecipientsView.COLUMN_PURCHASED}, 
				showEveryone ? null : "hidden IS NULL OR NOT hidden", 
				null, RecipientsView.COLUMN_NAME);
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
            Context context, LoaderManager manager) {
        super(context, null, R.layout.main_item, null, null, 
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
	protected void bindGroupView(View view, final Context context, final Cursor cursor,
			boolean isExpanded) {
		CheckBox rcptBox = (CheckBox)view.findViewById(R.id.recipient);
		rcptBox.setText(cursor.getString(1));
		rcptBox.setChecked(cursor.getInt(3) != 0);
		TextView notes = (TextView)view.findViewById(R.id.recipient_notes);
		String notesStr = cursor.getString(2);
		if (TextUtils.isEmpty(notesStr)){
			notes.setVisibility(View.GONE);
		} else {
			notes.setVisibility(View.VISIBLE);
			notes.setText(notesStr);
		}
		((TextView)view.findViewById(R.id.total_spend)).setText(NumberFormat.getCurrencyInstance().format(cursor.getDouble(4)));
		int planned = cursor.getInt(5);
		int purchased = cursor.getInt(6);
		((TextView)view.findViewById(R.id.summary)).setText("Planned: "+planned+", purchased: "+purchased);
		
		ImageButton create = (ImageButton)view.findViewById(R.id.create_new);
		if (isExpanded){
			create.setVisibility(View.VISIBLE);
			final long recipient = cursor.getLong(0);
			create.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					context.startActivity(new Intent(context, GiftActivity.class)
					                   .putExtra(GiftActivity.RECIPIENT_KEY, recipient));
				}
			});
			
		} else {
			create.setVisibility(View.GONE);
		}

		
	}
    
    
}

