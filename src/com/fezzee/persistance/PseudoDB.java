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
		    msgDatabase.put("gene2", new ArrayList<String>());
		    msgDatabase.put("gene3", new ArrayList<String>());
		    msgDatabase.put("gene5", new ArrayList<String>());
		    msgDatabase.put("gene6", new ArrayList<String>());
			/*
			setMessage("gene", "Chat Window 1- Gene");
			setMessage("gene", "This is");
			setMessage("gene", "a Fezzee");
			setMessage("gene", "Demo");
			setMessage("gene", "app");
			setMessage("gene", "for");
			setMessage("gene", "showing");
			setMessage("gene", "FragmentStatePagerAdapter");
			setMessage("gene", "and ViewPager");
			setMessage("gene", "Implementation");
			
			setMessage("gene2", "Chat Window 2- Gene2");
			setMessage("gene2", "This is");
			setMessage("gene2", "a Fezzee");
			setMessage("gene2", "Demo");
			setMessage("gene2", "app");
			setMessage("gene2", "for");
			setMessage("gene2", "showing");
			setMessage("gene2", "FragmentStatePagerAdapter");
			setMessage("gene2", "and ViewPager");
			setMessage("gene2", "Implementation");
			
			setMessage("gene3", "Chat Window 3- Gene3");
			setMessage("gene3", "This is");
			setMessage("gene3", "a Fezzee");
			setMessage("gene3", "Demo");
			setMessage("gene3", "app");
			setMessage("gene3", "for");
			setMessage("gene3", "showing");
			setMessage("gene3", "FragmentStatePagerAdapter");
			setMessage("gene3", "and ViewPager");
			setMessage("gene3", "Implementation");
			
			setMessage("gene5", "Chat Window 4- Gene5");
			setMessage("gene5", "This is");
			setMessage("gene5", "a Fezzee");
			setMessage("gene5", "Demo");
			setMessage("gene5", "app");
			setMessage("gene5", "for");
			setMessage("gene5", "showing");
			setMessage("gene5", "FragmentStatePagerAdapter");
			setMessage("gene5", "and ViewPager");
			setMessage("gene5", "Implementation");
			
			setMessage("gene6", "Chat Window 5- Gene6");
			setMessage("gene6", "This is");
			setMessage("gene6", "a Fezzee");
			setMessage("gene6", "Demo");
			setMessage("gene6", "app");
			setMessage("gene6", "for");
			setMessage("gene6", "showing");
			setMessage("gene6", "FragmentStatePagerAdapter");
			setMessage("gene6", "and ViewPager");
			setMessage("gene6", "Implementation");
			*/
		
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
