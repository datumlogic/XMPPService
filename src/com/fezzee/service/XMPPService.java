package com.fezzee.service;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
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

import com.fezzee.data.ChatCollection;
import com.fezzee.data.ChatCollection.ChatObject;
import com.fezzee.data.XMPPListenerTypes;
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
public class XMPPService extends Service implements Observable {
	
	private final IBinder myBinder = new ConnectionBinder();
	protected XMPPConnection connection;
	private SmackAndroid asmk;
	
	//private ConnectionObserver connObserver;
	private XMPPChatMediator chatMediator;
	private XMPPPresenceMediator presenceMediator;
	
	private final Object MUTEX= new Object();
	
	private ArrayList<Observer> connObservers = new ArrayList<Observer>();
	private ArrayList<Observer> chatObservers = new ArrayList<Observer>();
	private ArrayList<Observer> rosterObservers = new ArrayList<Observer>();
	
	/*
	private volatile String message0; //Connection Message
	private volatile boolean changed0;
	private volatile String message1; //Chat Message
	private volatile boolean changed1;
	private volatile String message2; //Roster Message
	private volatile boolean changed2;
	*/
	
	private final String TAG = "XMPPConnection[Service]";
	
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
		connection.addConnectionListener(new XMPPConnectionListener(this));
		
		//fetch the roster from the server   
        Roster roster = connection.getRoster();
        //Auto subscribe is the default
        roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        
        
        presenceMediator = new XMPPPresenceMediator(roster,this);
        connection.addPacketListener(
        		new XMPPPresenceMediator.PresenceSubscriptionListener(), 
        		new XMPPPresenceMediator.SubscriptionFilter());
        
        
        roster.addRosterListener(new XMPPPresenceListener(presenceMediator,this));
		
		ChatManager chatman = connection.getChatManager();
		chatMediator = new XMPPChatMediator(chatman, XMPPService.this);
		
		chatman.addChatListener(new XMPPChatListener(XMPPService.this));
		
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
	 * Returns message Database
	 */
	public ChatCollection getChatDatabase() 
	{
		if (chatMediator == null)
		{
			throw new IllegalStateException("ChatMediator is NULL");
		
		}
		return chatMediator.collChatObjects;
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
	    			setState("Connection is NULL",XMPPListenerTypes.CONNECTION);
	    			return;
	    		}
	    		
	    		try {
	        	 
	    			connection.connect();
	    			
	    			connection.login(USERNAME, PASSWORD, RESOURCE);
	    			
	    			
	            
	    			if (connection.isConnected()) 
	    				//Log.d(TAG,  "Connected to " + connection.getHost());
	    			    setState("Connected to " + connection.getHost(),XMPPListenerTypes.CONNECTION);
	    			else {
	    				setState("Not Connected",XMPPListenerTypes.CONNECTION);
	    				return;
	    			}
	    			
	    			if (connection.isAuthenticated()) 
	    				setState("Authenticted as " + connection.getUser(),XMPPListenerTypes.CONNECTION);
	    			else {
	    				setState("Not Authenticated",XMPPListenerTypes.CONNECTION);
	    				return;
	    			}
	            
	    			//TODO: Should set this to your last choosen status/presence
	    			//Currently setting My status to available
	    			Presence presence = new Presence(Presence.Type.available);
	    			connection.sendPacket(presence);
	    			
	    			
	    			
	    			
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
	                setState("Exception Ln 242: " + ex.toString(),XMPPListenerTypes.CONNECTION);
	                //connection = null;
	                return;
	    		} catch (Exception e) {
	              //all other exceptions
	        	   Log.e(TAG, "Unhandled Exception"+  e.getMessage()); 
	        	   e.printStackTrace();
	        	   setState("Exception Ln 249: " + e.toString(),XMPPListenerTypes.CONNECTION);
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
	
	public CopyOnWriteArrayList<ChatObject> getContacts()
    {
    	//
    	return presenceMediator.contacts;
    
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
				setState("SetLocation exitied: Bad connection",XMPPListenerTypes.CONNECTION);
				return;
			}
			// Create a pubsub manager using an existing Connection
			PubSubManager mgr = new PubSubManager(connection,"pubsub." + HOST);
						
			setState("RTN PubSubMgr: " + mgr.toString(),XMPPListenerTypes.CONNECTION);
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
			
				setState("SendLocation: " + item2.toString(),XMPPListenerTypes.CONNECTION);
			
			}
			
		} catch (ClassCastException cce) {
			Log.e("PublishActivity::onCreate", "Did you register Smack's XMPP Providers and Extensions in advance? - " +
        		"SmackAndroid.init(this)?\n" + cce.getMessage());
			cce.printStackTrace();
			setState("Exception Ln 316: " + cce.toString(),XMPPListenerTypes.CONNECTION);
			//connection = null;
		} catch (XMPPException ex) {
			Log.e("PublishActivity::onCreate", "XMPPException for '"+  USERNAME + "'");
			ex.printStackTrace();
			setState("Exception Ln 321: " + ex.toString(),XMPPListenerTypes.CONNECTION);
			//connection = null;    
		} catch (Exception e) {
			//all other exceptions
			Log.e("PublishActivity::onCreate", "Unhandled Exception"+  e.getMessage()); 
			e.printStackTrace();
			setState("Exception Ln 327: " + e.toString(),XMPPListenerTypes.CONNECTION);
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
	public void register(Observer obj, XMPPListenerTypes type) {
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
			case PERSISTANCE:
				if(!rosterObservers.contains(obj)) {
					Log.v(TAG,"Persistance Observer Added");
					rosterObservers.add(obj);
				}
				break;
		
		}
	}
	

	public void unregister(Observer obj, XMPPListenerTypes type){
		
		switch (type)
		{
			case CONNECTION:
				connObservers.remove(obj);
				break;
			case CHAT:
				chatObservers.remove(obj);
				break;
			case PERSISTANCE:
				rosterObservers.remove(obj);
				break;
		
		}
	}

	//method to notify observers of change
	//I think this has been changed to not use message 0, message 1 , etc
	public void notifyObservers(XMPPListenerTypes type, Object msg){

		List<Observer> observersLocal = null;
		
		switch (type)
		{
		
			case CONNECTION:
				//synchronization is used to make sure any observer registered after message is received is not notified
				synchronized (MUTEX) {
			
					//if (!changed0)
					//	return;
					Log.d(TAG,"notify CONNECTION called OK");
					observersLocal = new ArrayList<Observer>(this.connObservers);
					//this.changed0=false;
				}
		
				int i = 0;
				for (Observer obj : observersLocal) {
					Log.d(TAG,"CONNECTION notified: " + i);
					obj.update(msg);
					i++;
				}
				break;
				
			case CHAT:
				//synchronization is used to make sure any observer registered after message is received is not notified
				synchronized (MUTEX) {
			
					//if (!changed1)
					//	return;
					Log.d(TAG,"notify CHAT called OK");
					observersLocal = new ArrayList<Observer>(this.chatObservers);
					//this.changed1=false;
				}
		
				int j = 0;
				for (Observer obj : observersLocal) {
					Log.d(TAG,"CHAT notified: " + ((org.jivesoftware.smack.packet.Message)msg).getFrom() + " : " + ((org.jivesoftware.smack.packet.Message)msg).getBody()  );
					Log.e(TAG,"CHAT notified CLASS: " +obj.getClass());
					obj.update(msg);
					j++;
				}
				break;
				
			case PERSISTANCE:
				//synchronization is used to make sure any observer registered after message is received is not notified
				synchronized (MUTEX) {
			
					//if (!changed1)
					//	return;
					Log.d(TAG,"notify PERSISTANCE called OK");
					observersLocal = new ArrayList<Observer>(this.rosterObservers);
					//this.changed1=false;
				}
		
				int k = 0;
				for (Observer obj : observersLocal) {
					Log.d(TAG,"PERSISTANCE notified: " + k);
					obj.update(msg);
					k++;
				}
		}
 
	}

 

    
	/*
	 * method to post message to the observer
	 * The XMPPType is passed through to the Notify method
	 * TODO: I can probably get rid of this.
	 */
	public void setState(Object msg, XMPPListenerTypes type){
		
			
			notifyObservers(type, msg);

	}


} // end of ConnectionService



