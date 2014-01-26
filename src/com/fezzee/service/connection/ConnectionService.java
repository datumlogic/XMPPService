package com.fezzee.service.connection;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.fezzee.patterns.Observable;
import com.fezzee.patterns.Observer;


/*
 * This connection class opens a connection in-process to be shared amongst all activities
 * It was created as a Local service, and note a remote service, as the connection does not need to be shared
 * outside of our app (for now?). But this also means that is the app crashes, then the service crashes (and vice versa).
 * 
 * In order for this service to persist after the last activity is unBound, you must start it with startService AND bindService
 * 
 * The reconnection mechanism will try to reconnect periodically:

For the first minute it will attempt to connect once every ten seconds.
For the next five minutes it will attempt to connect once a minute.
If that fails it will indefinitely try to connect once every five minutes.
 */
public class ConnectionService extends Service implements Observable {
	
	private final IBinder myBinder = new ConnectionBinder();
	private XMPPConnection connection;
	private SmackAndroid asmk;
	
	private ConnectionObserver connObserver;
	private ArrayList<Observer> connObservers = new ArrayList<Observer>();
	
	private final Object MUTEX= new Object();
	private boolean changed;
	private volatile String message;
	
	private final String TAG = "ConnectionService";
	
	protected static String USERNAME;
	protected static String PASSWORD;
	protected static String HOST;
	protected static int PORT;
	protected static String SERVICE;
	protected static int TIMEOUT;
	protected static String RESOURCE;
	
	/*
	 * TODO: REPLACE HARDCODED VALES WITH VALUES READ FROM SETTINGS
	 */
	public void getSetupVals()
	{
		//setup XMPP to output debug info
		XMPPConnection.DEBUG_ENABLED=false;
		
		//recommended to initialise the static classes
		//SmackAndroid asmk = 
		asmk = SmackAndroid.init(getApplicationContext());
	
				
		USERNAME = "gene4";
		PASSWORD = "gene4";
		HOST = "ec2-54-201-47-27.us-west-2.compute.amazonaws.com";
		PORT = 5222;
		SERVICE = "ec2-54-201-47-27.us-west-2.compute.amazonaws.com";
		RESOURCE = "Smack01";
	}
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG,  "OnBind called");
		return myBinder;
	}
	
	@Override
	public boolean onUnbind(final Intent intent) {
		Log.d(TAG,  "UnBind called");
		return super.onUnbind(intent);
		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG,  "OnCreate called");
		//this.getSetupVals();
		//connect();
		//MOVED TO onStartCommand because this will be called after app restored from Bkground without a connection
		connObserver = new ConnectionObserver(this); //connectionserver and connectionobserver inappropriately TIGHTLY coupled
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (connection != null) {
			connection.disconnect();
			Log.d(TAG,  "Destroying service after disconnect");
		} else {
			Log.d(TAG,  "Destroying service");
		}
		
		Log.d(TAG,  "After super.destroy()");
	}
	
	/*
	 * Connect is called from here because this method is also called 
	 * after app restored from Bkground without a connection.
	 * 
	 * 
	 * START_NOT_STICKY 
	 * if this service's process is killed while it is started 
	 * (after returning from onStartCommand(Intent, int, int)), 
	 * and there are no new start intents to deliver to it, then 
	 * take the service out of the started state and don't 
	 * recreate until a future explicit call to 
	 * Context.startService(Intent).
	 * 
	 * This is also called every time the screen is rotated
	 */
	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		if (connection != null && connection.isConnected()) {
			Log.d(TAG,  "onStartCommand called. Connection: " + connection.isConnected());
		} else {
			Log.d(TAG,  "onStartCommand called. Creating connection");
			this.getSetupVals();
			connect();
		}
		return Service.START_NOT_STICKY;
	}
	
	/*
	 * Inner class
	 */
	public class ConnectionBinder extends Binder { 
		
		public ConnectionService getService() {            
			return ConnectionService.this;        
		} 
		
	}
	
	/*
	 * the the primary method
	 */
	public void connect()
	{

	    Thread t = new Thread(new Runnable() {
	    	@Override
	    	public void run() {
	    		
	    		
	    		// Create a connection
	    		ConnectionConfiguration connConfig = new ConnectionConfiguration(HOST, PORT, SERVICE);
	    		
	    		connConfig.setSASLAuthenticationEnabled(true);
	    		connConfig.setReconnectionAllowed(true);
	    		
	    		connection = new XMPPConnection(connConfig);
	    		try {
	        	 
	    			connection.connect();
	    			
	    			connection.login(USERNAME, PASSWORD, RESOURCE);
	    			
	    			
	            
	    			if (connection.isConnected()) 
	    				//Log.d(TAG,  "Connected to " + connection.getHost());
	    			    postMessage("Connected to " + connection.getHost());
	    			else {
	    				postMessage("Not Connected");
	    				return;
	    			}
	    			
	    			if (connection.isAuthenticated()) 
	    				postMessage("Authenticted as " + connection.getUser());
	    			else {
	    				postMessage("Not Authenticated");
	    				return;
	    			}
	            
	    			// Set My status to available
	    			Presence presence = new Presence(Presence.Type.available);
	    			connection.sendPacket(presence);
	    			
	    			
	    			
	    			 //TODO: IN PROGRESS
	    			if (connection.isConnected()) {
	    				connection.addConnectionListener(connObserver);
	    			}
	    			
	    			
	    			connection.getChatManager().addChatListener(new ChatManagerListener() {

	    				@Override
	    				public void chatCreated(final Chat chat, final boolean createdLocally) {
	    					if (!createdLocally) {
	    						chat.addMessageListener(new MyMessageListener());
	    					}
	    				}
	    			});
	            
	    			
	    			sendLocation();
	            
	    		} catch (XMPPException ex) {
	                Log.e(TAG, "Failed to log in as "+  USERNAME);
	                Log.e(TAG, ex.toString());
	                postMessage("Exception Ln 242: " + ex.toString());
	                //connection = null;
	                return;
	    		} catch (Exception e) {
	              //all other exceptions
	        	   Log.e(TAG, "Unhandled Exception"+  e.getMessage()); 
	        	   e.printStackTrace();
	        	   postMessage("Exception Ln 249: " + e.toString());
	        	   //connection = null;
	        	   return;
	    		}
	         
	    	}// end of run
	    }); // end of thread Runnable

	    t.start();   
	} //end of connect()
	
	
	//if connection not valid, exit
	public void sendLocation()
	{
		try
		{
			
			///while(connection==null || !connection.isConnected() || !connection.isAuthenticated())
			//{
			//	postMessage("Sleeping");
			//	Thread.sleep(5000);
			//}
			if (connection==null || !connection.isConnected() || !connection.isAuthenticated()) 
			{
				postMessage("SetLocation exitied: Bad connection");
				return;
			}
			// Create a pubsub manager using an existing Connection
			PubSubManager mgr = new PubSubManager(connection,"pubsub." + HOST);
						
			postMessage("RTN PubSubMgr: " + mgr.toString());
			LeafNode node =  mgr.getNode("Location");
						
		    if (node==null)
			{
				LeafNode newleaf = mgr.createNode("Location");//let the ID be auto assigned
				ConfigureForm form = new ConfigureForm(FormType.submit);
				form.setAccessModel(AccessModel.open);
				form.setDeliverPayloads(true);
				form.setNotifyRetract(true);
				form.setPersistentItems(true);
				form.setPublishModel(PublishModel.open);
				newleaf.sendConfigurationForm(form);
				node =  mgr.getNode("Location");
			}
						
			while (true)
			{
				
				Thread.sleep(330000);

				//'note' will need to be a reserved word that can not be used as a field name when creating a 'notification' schema
				//if you change the value of 'note' then you have to change getValue in PubSubItem
				SimplePayload payload = new SimplePayload("note","pubsub:test:note", "<note xmlns='pubsub:test:note'><rating type='choice' length='3'>" + "fVal" + "</rating><description type='' length='' validation='regex'>" +  "fVal2" + "</description></note>");
				PayloadItem<SimplePayload> item2 = new PayloadItem<SimplePayload>("note" + System.currentTimeMillis(), payload);
         
				node.publish(item2);
			
				postMessage("SendLocation: " + item2.toString());
			
			}
			
		} catch (ClassCastException cce) {
			Log.e("PublishActivity::onCreate", "Did you register Smack's XMPP Providers and Extensions in advance? - " +
        		"SmackAndroid.init(this)?\n" + cce.getMessage());
			cce.printStackTrace();
			postMessage("Exception Ln 316: " + cce.toString());
			//connection = null;
		} catch (XMPPException ex) {
			Log.e("PublishActivity::onCreate", "XMPPException for '"+  USERNAME + "'");
			ex.printStackTrace();
			postMessage("Exception Ln 321: " + ex.toString());
			//connection = null;    
		} catch (Exception e) {
			//all other exceptions
			Log.e("PublishActivity::onCreate", "Unhandled Exception"+  e.getMessage()); 
			e.printStackTrace();
			postMessage("Exception Ln 327: " + e.toString());
			//connection = null;
		}
	}
	
	//
	public String getConnStatus() {
		if (connection == null)
		{
			return "Connection is NULL";
		} else {
			return "Is Connected? " + connection.isConnected();
		}
	}
	
	
	public void register(Observer obj) {
		if(obj == null) throw new NullPointerException("Null Observer");
		if(!connObservers.contains(obj)) {
			Log.d(TAG,"Observer Added");
			connObservers.add(obj);
		}
	}
	

	public void unregister(Observer obj){
		connObservers.remove(obj);
	}

	//method to notify observers of change
	public void notifyObservers(){

		List<Observer> observersLocal = null;
		//synchronization is used to make sure any observer registered after message is received is not notified
		synchronized (MUTEX) {
			
			if (!changed)
				return;
			Log.d(TAG,"notify called OK");
			observersLocal = new ArrayList<Observer>(this.connObservers);
			this.changed=false;
		}
		int i = 0;
		for (Observer obj : observersLocal) {
			Log.d(TAG,"Obj notified: " + i);
			obj.update();
			i++;
		}
	}
 
	//method to get updates from observer
	public Object getUpdate(Observer obj){
		return this.message;
	}
	

    //method to post message to the topic
	public void postMessage(String msg){
		
		DateFormat df = DateFormat.getTimeInstance();
		df.setTimeZone(TimeZone.getTimeZone("gmt"));
		String gmtTime = df.format(new Date());

		Log.v("POST MESSAGE",msg);
		this.message= "[" + gmtTime +"] "+ msg;
		this.changed=true;
		notifyObservers();

	}
	
	private class MyMessageListener implements MessageListener {

		@Override
		public void processMessage(final Chat chat, final Message message) {
			Log.d(TAG, "Xmpp message received: '" + message.getBody() + "' on thread: " + getThreadSignature());
			// --> this is another thread ('Smack Listener Processor') not the
			// main thread!
			// you can parse the content of the message here
			// if you need to download something from the Internet, spawn a new
			// thread here and then sync with the main thread (via a
			// Handler)
		}
	}

	public static String getThreadSignature() {
		final Thread t = Thread.currentThread();
		return new StringBuilder(t.getName()).append("[id=").append(t.getId()).append(", priority=")
				.append(t.getPriority()).append("]").toString();
	}

} // end of ConnectionService

