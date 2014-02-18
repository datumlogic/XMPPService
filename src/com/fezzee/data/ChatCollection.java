package com.fezzee.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import android.util.Log;

import com.fezzee.patterns.Observable;
import com.fezzee.patterns.Observer;


/*
 * PseudoDB and FavoriteItem need to be refactored?
 * Because you can receive messages from those that aren't favorites, we should 
 * have a method distinguishes a chat user from a favorite friend(Roster entry)?
 * If a FavoriteItem doesn't have presence or status for instance, its a chat user
 * not a favorite friend
 * 
 * 
 */
public class ChatCollection implements Observer {
	
	private final Object MUTEX= new Object();


	//NEW Treadsafe Collection
	private CopyOnWriteArrayList<ChatObject> collChatThreadSafe = new CopyOnWriteArrayList<ChatObject>();
	private static final ChatCollection INSTANCE = new ChatCollection();
	
	private static final String TAG = "ChatCollection";

	// Private constructor prevents instantiation from other classes
	private ChatCollection() 
	{
		//XXX for testing only
		this.setMsg("gene@ec2-54-201-47-27.us-west-2.compute.amazonaws.com", "hello world");
	}

	public static ChatCollection getInstance() {
		return INSTANCE;
	}
	
	//setting message sets Timestamp in on message and lastTimestamp on Object
	//If msg is empty or just spaces, do not add message, but add user
	public void setMsg(String jidHost, String msg)
	{
		synchronized (MUTEX) {
			
			if (!jidHost.contains("@")) throw new IllegalArgumentException("Not a Valid JID");
			Iterator<ChatObject> o = collChatThreadSafe.iterator();
			while (o.hasNext()){
		   	      ChatObject user = o.next();
		   	      if (user.getJID().equals(jidHost)) 
		   	      {
		   	    	  if (!msg.trim().equals(""))
		   	    		  user.setMessage(msg);
		   	    	  return;
		   	      }
		    }
			//if not found, create user and add message
			collChatThreadSafe.add(new ChatObject(jidHost,msg));
		}
	}
	
	
	public String[] getNames(){
		
		synchronized (MUTEX) {
		
			ArrayList<String> ar = new ArrayList<String>();
			Iterator<ChatObject> users = collChatThreadSafe.iterator();
			while (users.hasNext()){
				ChatObject user = users.next();
	   	      	ar.add(user.getName());
			}
			return ar.toArray(new String[ar.size()]);
		}	
	}
	

	public String[] getJIDs(){
		
		synchronized (MUTEX) {
		
			ArrayList<String> ar = new ArrayList<String>();
			Iterator<ChatObject> users = collChatThreadSafe.iterator();
			while (users.hasNext()){
				ChatObject user = users.next();
	   	      	ar.add(user.getJID());
			}
			return ar.toArray(new String[ar.size()]);
		}	
	}
	
	
	//NEW VERSION!!
	public ChatCollection reorder(final String JID) {
		
		synchronized(MUTEX)
		{
			CopyOnWriteArrayList<ChatObject> copyDB = (CopyOnWriteArrayList<ChatObject>)this.collChatThreadSafe.clone();
			CopyOnWriteArrayList<ChatObject> newDB = null;
		 
		    Iterator<ChatObject> users = copyDB.iterator();
			while (users.hasNext()){
		   	      ChatObject user = users.next();
		   	      if (user.getJID().equals(JID)) 
		   	      {
		   	    	 //IF THE USER ALEADY IN DB
		   	    	 //if found remove it from the copy
		   	    	 copyDB.remove(user);
		   	    	 //make a new DB
		   	    	 newDB = new CopyOnWriteArrayList<ChatObject>();
		   	    	 //add the found object first
		   	    	 newDB.add(user);
		   	    	 //then put the rest after
		   	    	// newDB.putAll(copyDB);
		   	    	
					users = copyDB.iterator();
					while (users.hasNext()){
						ChatObject user2 = users.next();
						newDB.add(user2);
					}
		   	    	 break; //important to break out because we deleted an entry
		   	      }
		    }
			
		//If the User wasn't found
		if (newDB==null) 
		{
			newDB = new CopyOnWriteArrayList<ChatObject>();
			newDB.add(new ChatObject(JID));
			copyDB = (CopyOnWriteArrayList<ChatObject>)this.collChatThreadSafe.clone();
			users= copyDB.iterator();
			while (users.hasNext()){
				ChatObject user= users.next();
				newDB.add(user);
			}
		}
		//msgDatabase = (HashMap<ChatPerson,ArrayList<String>>)newDB.clone();
		collChatThreadSafe = (CopyOnWriteArrayList<ChatObject>)newDB.clone();
		return this;
		} //end of mutex	
	}
	

	public CopyOnWriteArrayList<ChatMessage> getMsgs(String jidHost)
	{
		synchronized (MUTEX) {
		
			Iterator<ChatObject> users = collChatThreadSafe.iterator();
		    while (users.hasNext()){
	   	      	ChatObject user = users.next();
	   	        if (user.getJID().equals(jidHost)) 
	   	        {
	   	        	return user.getMessages(jidHost);
	   	        }
		    }		
		    return null;
	    } //end of sync
	}

	public int size()
	{
		return collChatThreadSafe.size();
	}
	
	
   	@Override
	public void setObservable(Observable obj)
	{
	    obj.register(this, XMPPListenerTypes.CHAT);
	}
   	
	public void update(final Object msg) {
		
		//NOTE- getFrom() has the device appended- Need to strip it!
		String jid = ((org.jivesoftware.smack.packet.Message)msg).getFrom().split("/")[0];
		String body = ((org.jivesoftware.smack.packet.Message)msg).getBody();
		
		Iterator<ChatObject> chats = this.collChatThreadSafe.iterator();
		while(chats.hasNext()) {
			ChatObject chat = chats.next();
			Log.d(TAG+"::update**********","[chat.getJID]: " + chat.getJID() + "  [msg.getFrom()]: " + jid);
			if (chat.getJID().equals(jid)) {
				chat.setMessage(body);
				return;
			}
		}
		//if not found add it to the end
		this.setMsg(jid,body);
        
	}
	
	///////////////////////////////////
	
	public static enum PresenceState {
		UNAVILABLE, AVAILABLE, NOTIFIED 
	}
	
	///////////////////////////////////
	
	public class ChatObject  {
		
		private int imageId;
		private String jid;  //WO Device!
		private String name; //friendly name
		private PresenceState presence;
		private String status;
		private Date lastMessage;//UTC time
		
		private CopyOnWriteArrayList<ChatMessage> collMsgTreadSafe;

		public ChatObject(String jid, int imageId, String name, String status, PresenceState presence) {
			
		    this.imageId = imageId;
		    this.name = name;
		    this.status = status;//change this to an int?
		    this.presence = presence;
		    this.jid = jid;
		    //this.lastMessage = new Date();
		    collMsgTreadSafe = new CopyOnWriteArrayList<ChatMessage>();
		}
		
		//just set the JID
		public ChatObject(String jid) {
		    this.jid = jid;
		    this.name = jid.split("@")[0]; //FIXME: ??  
		    collMsgTreadSafe = new CopyOnWriteArrayList<ChatMessage>();
		}
		
		//set the JID and the initial message
		public ChatObject(String jid, String msg) {
			this.jid = jid;
			this.name = jid.split("@")[0]; //FIXME: ??  
			collMsgTreadSafe = new CopyOnWriteArrayList<ChatMessage>();
			if (!msg.trim().equals(""))
				this.setMessage(msg);
		}
		
		//Also sets the timestamp for the Object and the Message
		public void setMessage(String msg)
		{
			this.lastMessage = new Date();
			this.collMsgTreadSafe.add(new ChatMessage(msg,this.lastMessage));
		}
		
		//Also sets the timestamp for the Object and the Message
		//up to the user to put it in a thread safe collection or not?
		public CopyOnWriteArrayList<ChatMessage> getMessages(String jid)
		{
			return collMsgTreadSafe;
		}
		
		public void setMessageDateTime(Date utcDateTime)
		{
			this.lastMessage = utcDateTime;
		}
		
		public Date  getMessageDateTime()
		{
			return this.lastMessage;
		}
		
		public int getImageId() {
		    return imageId;
		}
		public void setImageId(int imageId) {
		    this.imageId = imageId;
		}
		public String getJID() {
		    return jid;
		}
		public void setJID(String jid) {
		    this.jid = jid;
		}
		public PresenceState getPresence() {
		    return presence;
		}
		public void setPresence(PresenceState presence) {
		    this.presence = presence;
		}
		public String getName() {
		    return name;
		}
		public void setName(String name) {
		    this.name = name;
		}
		public String getStatus() {
		    return status;
		}
		public void setStatus(String status) {
		    this.status = status;
		}
		@Override
		public String toString() {
		    return name + " : " + status;
		}
	}
	
	///////////////////////////////////
	
	public class ChatMessage  {
		
		private String body;
		private short ReadFlag = 0; //default is unread - 0
		private Date timestamp;
		private String id; //or int?
		
		//THIS SHOULD BE DEPERATED EVENTUALLY
        //FOR THE CONSTRUCTOR THAT REQUIRES AN ID
		public ChatMessage(String msg, Date timestamp)
		{
			this.body = msg;
			this.timestamp = timestamp;
			
		}
		
		public ChatMessage(String msg, Date timestamp, String id)
		{
			this.body = msg;
			this.id = id;
			this.timestamp = timestamp; 
			
		}
		
		public String getMessage()
		{
			return this.body;
		}
		
	}
	
}
