package com.fezzee.persistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.fezzee.patterns.Observable;
import com.fezzee.patterns.Observer;
import com.fezzee.types.XMPPTypes;
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
public class PseudoDB implements Observer {
	
	//stores messages
    //the pseudo DB here is a hashmap (objects stored by key) of a Deque (a queue  that can be read from either end)
	public HashMap<FavoriteItem,ArrayList<String>> msgDatabase; //FIXME: SHOULDN NOT BE PUBLIC
	//private TreeMap<FavoriteItem,ArrayList<String>> msgDatabase;
	
	public PseudoDB()
	{
		    msgDatabase = new HashMap<FavoriteItem,ArrayList<String>>();
		    //msgDatabase = new TreeMap<FavoriteItem,ArrayList<String>>();
		    
		    
		    //for testing- lets pretend this has two values
		    msgDatabase.put(new FavoriteItem("gene"), new ArrayList<String>());
		    this.setMessage("gene", "hello world");
		    //msgDatabase.put(new FavoriteItem("gene2"), new ArrayList<String>());	
	}
	
	public void addUser(FavoriteItem fav)
	{
		//TODO:we should check that user doesn't already exists
		//if doesn't exist, add , if already exists,update the user info
		
		if (!containsKey(fav.getJID())){
			msgDatabase.put(fav, new ArrayList<String>());
		} else {
			Iterator<FavoriteItem> fi = msgDatabase.keySet().iterator();
			Iterator<ArrayList<String>> vals = msgDatabase.values().iterator();
			while (fi.hasNext()){
		   	      FavoriteItem f = fi.next();
		   	      ArrayList<String> msgs = vals.next();
		   	      if (f.getJID().equals(fav.getJID())) {
		   	    	msgDatabase.remove(f);
		   	    	msgDatabase.put(fav,msgs);
		   	      }
		    }
			
			
		}
		
	
	}
	
	public ArrayList<FavoriteItem> getFavorites(){
		
		ArrayList<FavoriteItem> al = new ArrayList<FavoriteItem>();
		al.addAll(this.msgDatabase.keySet());
	    return al;
	}
	
	public String[] getNames(){
		
		ArrayList<String> ar = new ArrayList<String>();
		Iterator<FavoriteItem> keys = msgDatabase.keySet().iterator();
		while (keys.hasNext()){
	   	      FavoriteItem f = keys.next();
	   	      ar.add(f.getName());
		}
		return ar.toArray(new String[ar.size()]);
	}
	
	/*
	 * Creates the key is it doesn't exist
	 * Also sets the UTC time
	 */
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
			msgDatabase.put(new FavoriteItem(jidHost), newMsgs);
		}
		else
			msgs.add(msg);
		
	}
	
	
	public HashMap<FavoriteItem,ArrayList<String>> reorder(final String JID) {
		
		 HashMap<FavoriteItem,ArrayList<String>> copyDB = (HashMap<FavoriteItem,ArrayList<String>>)this.msgDatabase.clone();
		 HashMap<FavoriteItem,ArrayList<String>> newDB = null;
		 
		 Iterator<FavoriteItem> fi = copyDB.keySet().iterator();
			Iterator<ArrayList<String>> vals = copyDB.values().iterator();
			while (fi.hasNext()){
		   	      FavoriteItem f = fi.next();
		   	      ArrayList<String> msgs = vals.next();
		   	      if (f.getJID().equals(JID)) 
		   	      {
		   	    	  //if found remove it from the copy
		   	    	  copyDB.remove(f);
		   	    	  //make a new DB
		   	    	 newDB = new HashMap<FavoriteItem,ArrayList<String>>();
		   	    	 //add the found object first
		   	    	 newDB.put(f, msgs);
		   	    	 //then put the rest after
		   	    	 newDB.putAll(copyDB);
		   	      }
		    }
			
		if (newDB==null) newDB = copyDB;
		return newDB;
		
	}
	
	
	//if the DB contains this JID, return the msgs, else null
	public ArrayList<String> ifContainsJidReturnMsgs(String jidHost)
	{

		Iterator<FavoriteItem> fi = msgDatabase.keySet().iterator();
		Iterator<ArrayList<String>> vals = msgDatabase.values().iterator();
		while (fi.hasNext()){
	   	      FavoriteItem f = fi.next();
	   	      ArrayList<String> msgs = vals.next();
	   	      if (f.getJID().equals(jidHost)) return msgs;
	    }
		
		return null;
	}
	
	//if the DB contains this JID, return the msgs, else null
	public FavoriteItem ifContainsJidReturnFav(String jidHost)
	{

		Iterator<FavoriteItem> fi = msgDatabase.keySet().iterator();
		Iterator<ArrayList<String>> vals = msgDatabase.values().iterator();
		while (fi.hasNext()){
	   	      FavoriteItem f = fi.next();
	   	      //ArrayList<String> msgs = vals.next();
	   	      if (f.getJID().equals(jidHost)) return f;
	    }
		
		return null;
	}
	
	    //if the DB contains this JID, return the msgs, else null
	    //this overridden version not only returns it if found, 
	    //but sets the timestamp- can only be used internally
		private ArrayList<String> containsJID(String jidHost, boolean setTimeStanp)
		{

			Iterator<FavoriteItem> fi = msgDatabase.keySet().iterator();
			Iterator<ArrayList<String>> vals = msgDatabase.values().iterator();
			while (fi.hasNext()){
		   	      FavoriteItem f = fi.next();
		   	      ArrayList<String> msgs = vals.next();
		   	      if (f.getJID().equals(jidHost)) 
		   	      {
		   	    	  f.setMessageDateTime(DateTime.GetUTCdatetimeAsDate());
		   	    	  return msgs;
		   	      }
		    }
			
			return null;
		}
	
	
	
	public int size()
	{
		return msgDatabase.size();
	}
	
   	@Override
	public void setObservable(Observable obj)
	{
	    obj.register(this, XMPPTypes.CHAT);
	}
   	
   	public boolean containsKey(String jid)
   	{
   		Iterator<FavoriteItem> favs = msgDatabase.keySet().iterator();
   		while (favs.hasNext()){
   	      FavoriteItem fi = favs.next();
   	      if (fi.getJID().equals(jid)) return true;
   	    }
   		return false;
   	}
	
	
	 /*
     * THIS IS THE MOST IMPORTANT METHOD IN THIS CLASS
     * this HACK came from- see my note inline in comments as well
     * http://stackoverflow.com/questions/12705342/refreshing-a-view-inside-a-fragment
     */
	@Override
	public void update(final Object msg) {
		
		String jid = ((org.jivesoftware.smack.packet.Message)msg).getFrom();
		String host = jid.substring(0, jid.indexOf('@'));
		String body = ((org.jivesoftware.smack.packet.Message)msg).getBody();

		this.setMessage(host, body);
        
	}
	
}
