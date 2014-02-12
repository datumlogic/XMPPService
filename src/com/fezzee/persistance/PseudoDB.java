package com.fezzee.persistance;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

import com.fezzee.patterns.Observable;
import com.fezzee.patterns.Observer;
import com.fezzee.types.XMPPTypes;

public class PseudoDB implements Observer {
	
	//stores messages
    //the pseudo DB here is a hashmap (objects stored by key) of a Deque (a queue  that can be read from either end)
	private HashMap<String,ArrayList<String>> msgDatabase;
	
	public PseudoDB()
	{
		
		    msgDatabase = new HashMap<String,ArrayList<String>>();
		    
		    msgDatabase.put("gene", new ArrayList<String>());
		    //msgDatabase.put("gene2", new ArrayList<String>());
		    //msgDatabase.put("gene3", new ArrayList<String>());
		    //msgDatabase.put("gene5", new ArrayList<String>());
		    //msgDatabase.put("gene6", new ArrayList<String>());
			
		
	}
	
	//creates the key is it doesn't exist
	public void setMessage(String jidHost, String msg)
	{
		if (! msgDatabase.containsKey(jidHost))
			msgDatabase.put(jidHost, new ArrayList<String>());
		
		msgDatabase.get(jidHost).add(msg);
	}
	
	public ArrayList<String> getMessages(String jidHost)
	{  
		return msgDatabase.get(jidHost);
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
