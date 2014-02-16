package com.fezzee.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fezzee.patterns.Observable;
import com.fezzee.patterns.Observer;
import com.fezzee.utils.DateTime;


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
	
    //DEPRECATED
	//@Deprecated
	//public HashMap<ChatPerson,ArrayList<String>> msgDatabase; //FIXME: SHOULDN NOT BE PUBLIC

	//NEW Treadsafe Collection
	private CopyOnWriteArrayList<ChatObject> collChatThreadSafe;
	
	public ChatCollection()
	{
		    //DEPRECATED- REPLACING
		    //msgDatabase = new HashMap<ChatPerson,ArrayList<String>>();
		    
		    //NEW Treadsafe Collection
		    collChatThreadSafe = new CopyOnWriteArrayList<ChatObject>();
		    
		    //for testing- lets pretend this has two values
		   //msgDatabase.put(new FavoriteItem("gene@ec2-54-201-47-27.us-west-2.compute.amazonaws.com"), new ArrayList<String>());
		   //this.setMessage("gene@ec2-54-201-47-27.us-west-2.compute.amazonaws.com", "hello world");
		   //this.setMessage("gene", "hello world");
		   //this.setMessage("gene2", "hello world 2"); 
		   //this.setMessage("gene@ec2-54-201-47-27.us-west-2.compute.amazonaws.com", "hello world");
		   //this.setMessage("gene2@ec2-54-201-47-27.us-west-2.compute.amazonaws.com", "hello world 2");
		   this.setMsg("gene@ec2-54-201-47-27.us-west-2.compute.amazonaws.com", "hello world");
		     
	}
	
	/*
	 * DEPRECATED
	 * Creates the key is it doesn't exist
	 * Also sets the UTC time
	 
	@Deprecated
	public void setMessage(String jidHost, String msg)
	{
		//if the DB doesn't contain the JID, create a new entry, else
		//returns the msgs collection and add the latest message to it
		//calls the version of this method that sets the timestamp
		ArrayList<String> msgs = containsJID(jidHost, true);
		if (msgs == null)
		{
			ArrayList<String> newMsgs = new ArrayList<String>();
			newMsgs.add(msg);
			msgDatabase.put(new ChatPerson(jidHost), newMsgs);
		}
		else
			msgs.add(msg);
		
	}
	*/
	
	//NEW
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
	
	/*
	//DEPRECATED
	//if the DB contains this JID, return the msgs, else null
    //this overridden version not only returns it if found, 
    //but sets the timestamp- can only be used internally
	@Deprecated
	private ArrayList<String> containsJID(String jidHost, boolean setTimeStanp)
	{

		Iterator<ChatPerson> fi = msgDatabase.keySet().iterator();
		Iterator<ArrayList<String>> vals = msgDatabase.values().iterator();
		while (fi.hasNext()){
	   	      ChatPerson f = fi.next();
	   	      ArrayList<String> msgs = vals.next();
	   	      if (f.getJID().equals(jidHost)) 
	   	      {
	   	    	  f.setMessageDateTime(DateTime.GetUTCdatetimeAsDate());
	   	    	  return msgs;
	   	      }
	    }
		
		return null;
	}
	
	
	
	//DEPRECATED
	@Deprecated
	public String[] getNames(){
		
		ArrayList<String> ar = new ArrayList<String>();
		Iterator<ChatPerson> keys = msgDatabase.keySet().iterator();
		while (keys.hasNext()){
	   	      ChatPerson f = keys.next();
	   	      ar.add(f.getName());
		}
		return ar.toArray(new String[ar.size()]);
	}
	*/
	
    //NEW
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
	
	/*
	@Deprecated
	public String[] getJIDs(){
		
		ArrayList<String> ar = new ArrayList<String>();
		Iterator<ChatPerson> keys = msgDatabase.keySet().iterator();
		while (keys.hasNext()){
	   	      ChatPerson f = keys.next();
	   	      ar.add(f.getJID());
		}
		return ar.toArray(new String[ar.size()]);
	}
	*/
	
	//NEW
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
	
	/*
	@Deprecated
    public ChatCollection reorder(final String JID) {
		
		synchronized(MUTEX)
		{
		 HashMap<ChatPerson,ArrayList<String>> copyDB = (HashMap<ChatPerson,ArrayList<String>>)this.msgDatabase.clone();
		 HashMap<ChatPerson,ArrayList<String>> newDB = null;
		 
		 Iterator<ChatPerson> fi = copyDB.keySet().iterator();
			Iterator<ArrayList<String>> vals = copyDB.values().iterator();
			while (fi.hasNext()){
		   	      ChatPerson f = fi.next();
		   	      ArrayList<String> msgs = vals.next();
		   	      if (f.getJID().split("@")[0].equals(JID.split("@")[0])) 
		   	      {
		   	    	 //IF THE USER ALEADY IN DB
		   	    	 //if found remove it from the copy
		   	    	 copyDB.remove(f);
		   	    	 //make a new DB
		   	    	 newDB = new HashMap<ChatPerson,ArrayList<String>>(1);
		   	    	 //add the found object first
		   	    	 newDB.put(f, msgs);
		   	    	 //then put the rest after
		   	    	// newDB.putAll(copyDB);
		   	    	
					fi = copyDB.keySet().iterator();
					vals = copyDB.values().iterator();
					while (fi.hasNext()){
						ChatPerson f2 = fi.next();
				   	    ArrayList<String> msgs2 = vals.next();
						newDB.put(f2, msgs2);
					}
		   	    	 break; //important to break out because we deleted an entry
		   	      }
		    }
			
		//If the User wasn't found
		if (newDB==null) 
		{
			newDB = new HashMap<ChatPerson,ArrayList<String>>(1);
			newDB.put(new ChatPerson(JID),new ArrayList<String>());
			copyDB = (HashMap<ChatPerson,ArrayList<String>>)this.msgDatabase.clone();
			fi = copyDB.keySet().iterator();
			vals = copyDB.values().iterator();
			while (fi.hasNext()){
				ChatPerson f = fi.next();
		   	    ArrayList<String> msgs = vals.next();
				newDB.put(f, msgs);
			}
		}
		
		msgDatabase = (HashMap<ChatPerson,ArrayList<String>>)newDB.clone();
		}
		
		return this;
		
	}
	
	
	//if the DB contains this JID, return the msgs, else null
	@Deprecated
	public ArrayList<String> ifContainsJidReturnMsgs(String jidHost)
	{

		Iterator<ChatPerson> fi = msgDatabase.keySet().iterator();
		Iterator<ArrayList<String>> vals = msgDatabase.values().iterator();
		while (fi.hasNext()){
	   	      ChatPerson f = fi.next();
	   	      ArrayList<String> msgs = vals.next();
	   	      if (f.getJID().equals(jidHost)) return msgs;
	    }
		
		return null;
	}
	*/
	
	//NEW
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
	
	    
	
	/*
	@Deprecated
	public int size()
	{
		return msgDatabase.size();
	}
	*/
	
	public int size()
	{
		return collChatThreadSafe.size();
	}
	
	
   	@Override
	public void setObservable(Observable obj)
	{
	    obj.register(this, XMPPListenerTypes.CHAT);
	}
   	
   	/*
   	public boolean containsKey(String jid)
   	{
   		Iterator<ChatPerson> favs = msgDatabase.keySet().iterator();
   		while (favs.hasNext()){
   	      ChatPerson fi = favs.next();
   	      if (fi.getJID().equals(jid)) return true;
   	    }
   		return false;
   	}
	*/
	
	 /*
     * THIS IS THE MOST IMPORTANT METHOD IN THIS CLASS
     * this HACK came from- see my note inline in comments as well
     * http://stackoverflow.com/questions/12705342/refreshing-a-view-inside-a-fragment
     
	@Override
	public void update(final Object msg) {
		
		String jid = ((org.jivesoftware.smack.packet.Message)msg).getFrom();
		String host = jid.substring(0, jid.indexOf('@'));
		String body = ((org.jivesoftware.smack.packet.Message)msg).getBody();

		this.setMessage(jid, body);
        
	}
	*/
	
	public void update(final Object msg) {
		
		String jid = ((org.jivesoftware.smack.packet.Message)msg).getFrom();
		String host = jid.substring(0, jid.indexOf('@'));
		String body = ((org.jivesoftware.smack.packet.Message)msg).getBody();

		this.setMsg(jid,body);
        
	}
	
	////////////////////////////////////////////////////////////////////////
	
	
	
	public static enum PresenceState {
		UNAVILABLE, AVAILABLE, NOTIFIED 
	}
	
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
		
	}
	
}
