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
import android.view.View;
import android.widget.TextView;

import com.fezzee.patterns.Observable;
import com.fezzee.patterns.Observer;
import com.fezzee.service.connection.ConnectionService;
import com.fezzee.service.connection.R;
;

public class LocalBoundActivity extends Activity implements Observer{

	private ConnectionService myService;    
	private boolean isBound = false;
	private final String TAG = "LocalBoundActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_bound);
		
		TextView textView = (TextView) this.findViewById(R.id.myTextView);
		textView.setMovementMethod(new ScrollingMovementMethod());
		
		
		Log.d(TAG,  "onCreate called");
		
		Intent intent = new Intent(this, ConnectionService.class); 
		//this was added to the tutorial to keep the service running after the last activity is closed
		startService(intent);
		bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
		
		
	}
	
	/*
	 * This wasn't in my the tutorial I used but if you don't have this 
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
		//getMenuInflater().inflate(R.menu.local_bound, menu);
		return true;
	}
	
	/*
	 * instance variable defined with callback methods
	 */
	private ServiceConnection myConnection = new ServiceConnection() { 
		
			public void onServiceConnected(ComponentName className, IBinder service) {        
				ConnectionService.ConnectionBinder binder = (ConnectionService.ConnectionBinder) service;        
				   
				setObservable(binder.getService());
				myService.register(LocalBoundActivity.this);
				myService.postMessage("Service Started");
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
		this.myService = (ConnectionService)obj;
	}
	

	
	//can be called from any thread
	@Override
	public void update() {
		this.runOnUiThread(new Runnable() {
		    public void run() {
		    	String serviceVal = (String) myService.getUpdate(LocalBoundActivity.this);
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
