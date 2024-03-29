package com.fezzee.activity;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fezzee.data.ChatCollection;
import com.fezzee.data.XMPPListenerTypes;
import com.fezzee.patterns.Observable;
import com.fezzee.patterns.Observer;
import com.fezzee.service.XMPPService;
import com.fezzee.service.connection.R;


public class MainConnectionActivity extends Activity implements Observer{

	/*
	 * THIS IS A CRUCIAL VARIABLE- As the LauncherActivity is never disposed
	 * while the App is running, this static variable keeps the Service avail
	 * without requiring it to be Bound in various places. 
	 * TODO: Investigate this approach
	*/
	public static XMPPService myService; 
	
	private boolean isBound = false;
	private final String TAG = "LocalBoundActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_connection);
		
		TextView textView = (TextView) this.findViewById(R.id.myTextView);
		textView.setMovementMethod(new ScrollingMovementMethod());
		
		
		Log.d(TAG,  "onCreate called");
		
		/*
		 * This is the Biding that is crucial
		 */
		Intent intent = new Intent(this, XMPPService.class); 
		//If you start the service AND bind it, we keep the service running 
		//even if the last activity is closed
		startService(intent);
		bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
		
		
	}
	
	/*
	 * If you don't unbind onDestroy, 
	 * you leak resources when you close the app/UI.
	 */
	@Override
    public void onDestroy() {
        super.onDestroy();
        
        Log.d(TAG,  "onDestroy called");
        
        if (isBound) unbindService(myConnection);
    }
	
	@Override
	public void onPause() {
	    super.onPause();  

	    Log.d(TAG,  "onPause called");
	}
	
	@Override
	public void onResume() {
	    super.onResume();  

	    Log.d(TAG,  "onResume called");
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
	   
	    	case R.id.action_chat:
	    		
	    		Intent i = new Intent(getBaseContext(),ChatHistoryActivity.class);
	    		ChatHistoryActivity.setChatDatabase((ChatCollection)myService.getChatDatabase());
	    		new Intent();
	    		i.putExtra("JID", "gene");
	    		startActivity(i);
	    		return true;
	    		
	    	case R.id.action_favs:
	    		
	    		Intent i2 = new Intent(getBaseContext(),FavoritesActivity.class);
	    		//FavoritesActivity.setChatDatabase((PseudoDB)myService.getChatDatabase());
	    		new Intent();
	    		//i2.putExtra("JID", "gene");
	    		startActivity(i2);
	    		return true;
	    
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/*
	 * instance variable defined with callback methods
	 */
	private ServiceConnection myConnection = new ServiceConnection() { 
		
			public void onServiceConnected(ComponentName className, IBinder service) {        
				XMPPService.ConnectionBinder binder = (XMPPService.ConnectionBinder) service;        
				   
				setObservable(binder.getService());
				myService.register(MainConnectionActivity.this, XMPPListenerTypes.CONNECTION);
				myService.setState("Service Started",XMPPListenerTypes.CONNECTION);
				isBound = true;    
			}        
		
			public void onServiceDisconnected(ComponentName arg0) {        
				isBound = false;    
			}       
	}; // end of myConnection initialisation
	
	
	/*
	 * adds clarity to the process?
	 * Replaced 
	 * 		myService = binder.getService();
	 * with
	 * 		setObservable(binder.getService());
	 * in 
	 * 		ServiceConnection::onServiceConnected
	 */
	@Override
	public void setObservable(Observable obj)
	{
		this.myService = (XMPPService)obj;
	}
	
	
	//@Override
    //public int getId() {
	//	return this.getId();
	//}
	
	//can be called from any thread
	@Override
	public void update(final Object msg) {
		this.runOnUiThread(new Runnable() {
		    public void run() {
		    	String serviceVal = msg.toString();//(String) myService.getState(MainConnectionActivity.this,XMPPTypes.CONNECTION);
				final TextView myTextView = (TextView)findViewById(R.id.myTextView);  
				myTextView.append(serviceVal+"\n");//
				if (myTextView.getLineCount()>0)
				{
					int scroll_amount = (int) (myTextView.getLineCount() * myTextView.getLineHeight()) - (myTextView.getBottom() - myTextView.getTop());
					myTextView.scrollTo(0, scroll_amount);
				} 
		    }
		});
		
	}
	
	public void getConnectionStatus(View view){  

		DateFormat df = DateFormat.getTimeInstance();
		df.setTimeZone(TimeZone.getTimeZone("gmt"));
		String gmtTime = df.format(new Date());
		
		String serviceVal = "[" + gmtTime + "] " + myService.getConnStatus();   
		final TextView myTextView = (TextView)findViewById(R.id.myTextView);  
		myTextView.append(serviceVal+"\n");//
		if (myTextView.getLineCount()>0)
		{
			int scroll_amount = (int) (myTextView.getLineCount() * myTextView.getLineHeight()) - (myTextView.getBottom() - myTextView.getTop());
			myTextView.scrollTo(0, scroll_amount);
		} 
	}
	

}
