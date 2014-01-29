package com.fezzee.service;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
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
import com.fezzee.types.XMPPTypes;


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
public class XMPPService extends Service implements Observable {
	
	private final IBinder myBinder = new ConnectionBinder();
	private XMPPConnection connection;
	private SmackAndroid asmk;
	
	//private ConnectionObserver connObserver;
	private ChatMediator chatMediator;
	
	private final Object MUTEX= new Object();
	
	private ArrayList<Observer> connObservers = new ArrayList<Observer>();
	private ArrayList<Observer> chatObservers = new ArrayList<Observer>();
	
	private volatile String message0; //Connection Message
	private volatile boolean changed0;
	private volatile String message1; //Chat Message
	private volatile boolean changed1;
	
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
		//connObserver = new ConnectionObserver(this); //connectionserver and connectionobserver inappropriately TIGHTLY coupled
		 //TODO: IN PROGRESS
		// Create a connection
		this.getSetupVals();
		ConnectionConfiguration connConfig = new ConnectionConfiguration(HOST, PORT, SERVICE);
		
		connConfig.setSASLAuthenticationEnabled(true);
		connConfig.setReconnectionAllowed(true);
		
		connection = new XMPPConnection(connConfig);
		connection.addConnectionListener(new ConnectionObserver(this));
		
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
			
			connect();
		}
		return Service.START_NOT_STICKY;
	}
	
	/*
	 * Inner class
	 */
	public class ConnectionBinder extends Binder { 
		
		public XMPPService getService() {            
			return XMPPService.this;        
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
	    		
	    		if (connection == null){
	    			Log.e(TAG, "Connection is NULL");
	    			postMessage("Connection is NULL",XMPPTypes.CONNECTION);
	    			return;
	    		}
	    		
	    		try {
	        	 
	    			connection.connect();
	    			
	    			connection.login(USERNAME, PASSWORD, RESOURCE);
	    			
	    			
	            
	    			if (connection.isConnected()) 
	    				//Log.d(TAG,  "Connected to " + connection.getHost());
	    			    postMessage("Connected to " + connection.getHost(),XMPPTypes.CONNECTION);
	    			else {
	    				postMessage("Not Connected",XMPPTypes.CONNECTION);
	    				return;
	    			}
	    			
	    			if (connection.isAuthenticated()) 
	    				postMessage("Authenticted as " + connection.getUser(),XMPPTypes.CONNECTION);
	    			else {
	    				postMessage("Not Authenticated",XMPPTypes.CONNECTION);
	    				return;
	    			}
	            
	    			// Set My status to available
	    			Presence presence = new Presence(Presence.Type.available);
	    			connection.sendPacket(presence);
	    			
	    			
	    			ChatManager chatman = connection.getChatManager();
	    			chatMediator = new ChatMediator(chatman, XMPPService.this);
	    			chatman.addChatListener(new ChatObserver());
	    			
	    			//connection.getChatManager().addChatListener(new ChatObserver());
	    			
	    			/*
	    			connection.getChatManager().addChatListener(new ChatManagerListener() {
	    				@Override
	    				public void chatCreated(final Chat chat, final boolean createdLocally) {
	    					if (!createdLocally) {
	    						chat.addMessageListener(new MyMessageListener());
	    					}
	    				}
	    			});
	                */
	    			
	    			sendLocation();
	            
	    		} catch (XMPPException ex) {
	                Log.e(TAG, "Failed to log in as "+  USERNAME);
	                Log.e(TAG, ex.toString());
	                postMessage("Exception Ln 242: " + ex.toString(),XMPPTypes.CONNECTION);
	                //connection = null;
	                return;
	    		} catch (Exception e) {
	              //all other exceptions
	        	   Log.e(TAG, "Unhandled Exception"+  e.getMessage()); 
	        	   e.printStackTrace();
	        	   postMessage("Exception Ln 249: " + e.toString(),XMPPTypes.CONNECTION);
	        	   //connection = null;
	        	   return;
	    		}
	         
	    	}// end of run
	    }); // end of thread Runnable

	    t.start();   
	} //end of connect()
	
	public void createChat(String jid)
	{
		chatMediator.registerChat(jid);
		Log.d(TAG,"Called CreateChat in XMPP Service");
	}
	
	
	//if connection not valid, exit
	private void sendLocation()
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
				postMessage("SetLocation exitied: Bad connection",XMPPTypes.CONNECTION);
				return;
			}
			// Create a pubsub manager using an existing Connection
			PubSubManager mgr = new PubSubManager(connection,"pubsub." + HOST);
						
			postMessage("RTN PubSubMgr: " + mgr.toString(),XMPPTypes.CONNECTION);
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
				
				Thread.sleep(330000); //waits 5-1/2 mins

				//'note' will need to be a reserved word that can not be used as a field name when creating a 'notification' schema
				//if you change the value of 'note' then you have to change getValue in PubSubItem
				SimplePayload payload = new SimplePayload("note","pubsub:test:note", "<note xmlns='pubsub:test:note'><rating type='choice' length='3'>" + "fVal" + "</rating><description type='' length='' validation='regex'>" +  "fVal2" + "</description></note>");
				PayloadItem<SimplePayload> item2 = new PayloadItem<SimplePayload>("note" + System.currentTimeMillis(), payload);
         
				node.publish(item2);
			
				postMessage("SendLocation: " + item2.toString(),XMPPTypes.CONNECTION);
			
			}
			
		} catch (ClassCastException cce) {
			Log.e("PublishActivity::onCreate", "Did you register Smack's XMPP Providers and Extensions in advance? - " +
        		"SmackAndroid.init(this)?\n" + cce.getMessage());
			cce.printStackTrace();
			postMessage("Exception Ln 316: " + cce.toString(),XMPPTypes.CONNECTION);
			//connection = null;
		} catch (XMPPException ex) {
			Log.e("PublishActivity::onCreate", "XMPPException for '"+  USERNAME + "'");
			ex.printStackTrace();
			postMessage("Exception Ln 321: " + ex.toString(),XMPPTypes.CONNECTION);
			//connection = null;    
		} catch (Exception e) {
			//all other exceptions
			Log.e("PublishActivity::onCreate", "Unhandled Exception"+  e.getMessage()); 
			e.printStackTrace();
			postMessage("Exception Ln 327: " + e.toString(),XMPPTypes.CONNECTION);
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
	
	//the next 3 methods need a Type too
	public void register(Observer obj, XMPPTypes type) {
		if(obj == null) throw new NullPointerException("Null Observer");
		
		switch (type)
		{
			case CONNECTION:
				if(!connObservers.contains(obj)) {
					Log.d(TAG,"Conn Observer Added");
					connObservers.add(obj);
				}
				break;
			case CHAT:
				if(!chatObservers.contains(obj)) {
					Log.d(TAG,"Chat Observer Added");
					chatObservers.add(obj);
				}
				break;
		
		}
	}
	

	public void unregister(Observer obj, XMPPTypes type){
		
		switch (type)
		{
			case CONNECTION:
				connObservers.remove(obj);
				break;
			case CHAT:
				chatObservers.remove(obj);
				break;
		
		}
	}

	//method to notify observers of change
	public void notifyObservers(XMPPTypes type){

		List<Observer> observersLocal = null;
		
		switch (type)
		{
		
			case CONNECTION:
				//synchronization is used to make sure any observer registered after message is received is not notified
				synchronized (MUTEX) {
			
					if (!changed0)
						return;
					Log.d(TAG,"notify 0 called OK");
					observersLocal = new ArrayList<Observer>(this.connObservers);
					this.changed0=false;
				}
		
				int i = 0;
				for (Observer obj : observersLocal) {
					Log.d(TAG,"Obj 0 notified: " + i);
					obj.update();
					i++;
				}
				break;
				
			case CHAT:
				//synchronization is used to make sure any observer registered after message is received is not notified
				synchronized (MUTEX) {
			
					if (!changed1)
						return;
					Log.d(TAG,"notify 1 called OK");
					observersLocal = new ArrayList<Observer>(this.chatObservers);
					this.changed1=false;
				}
		
				int j = 0;
				for (Observer obj : observersLocal) {
					Log.d(TAG,"Obj 1 notified: " + j);
					obj.update();
					j++;
				}
				break;
			
		}
		
	}
 

	/*
	 * method to get updates from observer
	 * 0 = Connection, 1=Chat
	 */

	public Object getUpdate(Observer obj, XMPPTypes type){
		Object o = null;
		synchronized (MUTEX) {
			switch (type)
			{
				case CONNECTION:
					o = this.message0;
					break;
				case CHAT:
					o = this.message1;
					break;
			}
		}
		return o;
	}
	

    
	/*
	 * method to post message to the observer
	 * 0 = Connection, 1=Chat
	 */
	public void postMessage(String msg, XMPPTypes type){
		
		synchronized (MUTEX) {
			DateFormat df = DateFormat.getTimeInstance();
			df.setTimeZone(TimeZone.getTimeZone("gmt"));
			String gmtTime = df.format(new Date());
			
			switch (type)
			{
				case CONNECTION:
					Log.v("POST MESSAGE",msg);
					this.message0= "[" + gmtTime +"] "+ msg;
					this.changed0=true;
					break;
				case CHAT:
					Log.v("POST MESSAGE",msg);
					this.message1= "[" + gmtTime +"] "+ msg;
					this.changed1=true;
					break;
			}

			notifyObservers(type);
		}

	}


} // end of ConnectionService



