package com.fezzee.activity;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fezzee.data.ChatCollection;
import com.fezzee.data.ChatCollection.ChatObject;
import com.fezzee.data.XMPPListenerTypes;
import com.fezzee.patterns.Observable;
import com.fezzee.patterns.Observer;
import com.fezzee.service.connection.R;


public class FavoritesActivity extends Activity implements Observer {
	
    private static final String TAG = "Favorites Activity";
    
    private static CopyOnWriteArrayList<ChatObject> contacts;
    //private Handler mHandler = new Handler();
    private ListView listview;
    private FavoritesListAdapter adapter;
    //private Collection<RosterEntry> entries;
    private Handler mHandler = new Handler();
    //private Roster roster;
    Editable editable = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_favorites);
		
		 contacts = new CopyOnWriteArrayList<ChatObject>();
		
		Log.d("FavoritesActivity","Entered...");
		        
		listview = (ListView) this.findViewById(R.id.listContacts);
            
		this.setObservable(MainConnectionActivity.myService);
		
		mHandler.post(new Runnable() {
    		public void run() {
    			FavoritesActivity.contacts = MainConnectionActivity.myService.getContacts();
    			for (int i = 0; i < FavoritesActivity.contacts.size();i++)
    			{
    				ChatObject item = FavoritesActivity.contacts.get(i);
    				Log.v(TAG+"::update", "Item: " + item.getPresence());
    			
    			}
    		    setListAdapter();
    		}
    	});
		
	    
	}
	
	
	private void setListAdapter() 
	{
		Log.v(TAG+"::setListAdapter", "ENTERED");
        adapter = new FavoritesListAdapter(this,
                R.layout.listitem_favs2, contacts);
        
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem_favorites, contacts);
		listview.setAdapter(adapter);
		// React to user clicks on item)
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				     public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
				                             long id) {
				    	 
				    
				    	ChatObject fav = contacts.get(position);
				        Intent newActivity = new Intent(FavoritesActivity.this, ChatHistoryActivity.class); 
				        ChatCollection db = (ChatCollection)MainConnectionActivity.myService.getChatDatabase();
				        ChatHistoryActivity.setChatDatabase(db.reorder(fav.getJID()));
				        newActivity.putExtra("JID", fav.getJID());
			            startActivity(newActivity);  
				     }
				});
	}
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.local_bound, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        //case R.id.create_new:
	        //	newContact();
	        //    return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
    /*
	 * 
	 */
	@Override
	public void setObservable(Observable obj)
	{
		//if (obj==null) 
		//{
		//	Log.e(TAG+"::setObservable","obj is NULL: " + obj);
		//	return;
		//}
	    obj.register(this, XMPPListenerTypes.PERSISTANCE);
	}

	 /*
     * THIS IS THE MOST IMPORTANT METHOD IN THIS CLASS
     * this HACK came from- see my note inline in comments as well
     * http://stackoverflow.com/questions/12705342/refreshing-a-view-inside-a-fragment
     */
	@Override
	public void update(final Object msg) {
		
		FavoritesActivity.contacts = ((CopyOnWriteArrayList<ChatObject>)msg);
		//for (int i = 0; i < FavoritesActivity.contacts.size();i++)
		//     Log.v(TAG+"::update", "Item: " + FavoritesActivity.contacts.get(i).getPresence());

		//XXX: MUST be done inside the handler or doesn't update
		//TODO: Check to see if thats the issue with Chat as well!
		//private Handler mHandler = new Handler();
		mHandler.post(new Runnable() {
    		public void run() {
    			adapter.notifyDataSetChanged();
    		}
    	});
		
		
	}

}
