package com.fezzee.service;

import java.util.Collection;
import java.util.Iterator;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import android.util.Log;

import com.fezzee.data.ChatCollection.ChatObject;
import com.fezzee.data.ChatCollection.PresenceState;
import com.fezzee.data.XMPPListenerTypes;
import com.fezzee.service.connection.R;



public class XMPPPresenceListener implements RosterListener {

	private static final String TAG = "XMPPPresenceListener[Service]";
	private XMPPPresenceMediator mediator;
	private XMPPService service;
	private Roster roster;
	
	public XMPPPresenceListener(XMPPPresenceMediator mediator, XMPPService service)
	{
		this.mediator =  mediator;
		this.roster = mediator.roster;
		this.service = service;
	}
	
	public void entriesAdded(Collection<String> param) {
    	
    	Log.v(TAG + "::entriesAdded",  "Entered: " + param);
    	
    	    
            //if we find any existing jid's in the param collection, remove it from the param colection
    		for (int i = 0; i < XMPPPresenceMediator.contacts.size(); i++)
            {
            	ChatObject fav= XMPPPresenceMediator.contacts.get(i);
            	if ( param.contains(fav.getJID()))
            	{
            		Log.v(TAG + "::entriesAdded",  "User Found, so removing it");
            		param.remove(fav.getJID());
            	}
            }
    		//now add any remaining JIDs to contacts.
    		for (Iterator<String> Params = param.iterator(); Params.hasNext();) {
    			
    			ChatObject item = new ChatObject(Params.next(),R.drawable.ic_launcher,"","",PresenceState.UNAVILABLE);
    			Log.v(TAG + "::entriesAdded",  "ADDING: " + item.getJID());
    			XMPPPresenceMediator.contacts.add(item);
    		}
    	    service.setState(XMPPPresenceMediator.contacts, XMPPListenerTypes.PERSISTANCE);
            
    }

    public void entriesDeleted(Collection<String> addresses) {
    	
    	Log.v(TAG + "::entriesDeleted",  "Entered: " + addresses);
    	
    	
    	for (int i = 0; i < XMPPPresenceMediator.contacts.size(); i++)
        {
        	ChatObject fav= XMPPPresenceMediator.contacts.get(i);
        	Log.v(TAG + "::entriesDeleted",  "Fav item: " + i + " : " + fav);
        	if ( addresses.contains(fav.getJID()))
        	{
        		Log.v(TAG + "::entriesDeleted",  "User Found, Deleting: " + fav.getJID());
        		XMPPPresenceMediator.contacts.remove(fav);
        	}
        }
    	Log.v(TAG + "::entriesDeleted",  "Contact removed: ");
    	
    	//not sure why this is here
    	//try{
    	//	Thread.sleep(1000);
    	//} catch (Exception e) {
    	//	return;
    	//}
    	
    	service.setState(XMPPPresenceMediator.contacts, XMPPListenerTypes.PERSISTANCE);
        
    }

    public void entriesUpdated(Collection<String> addresses) {
    	Log.v(TAG + "::entriesUpdated",  "Entered: " + addresses);
    	
    	
        //not sure what this should do- read spec!
    	//for now lets just reset it to unavailable and clear the status
		for (int i = 0; i < XMPPPresenceMediator.contacts.size(); i++)
        {
        	ChatObject fav = XMPPPresenceMediator.contacts.get(i);
        	if (addresses.contains(fav.getJID()))
        	{
        		Log.v(TAG + "::entriesUpdated",  "Resetting...");
        		fav.setPresence(PresenceState.UNAVILABLE);
        		fav.setStatus("");
        	}
        }
		
		service.setState(XMPPPresenceMediator.contacts, XMPPListenerTypes.PERSISTANCE);
    	
       
        
    } // end of entriesUpdated

    
    //TODO: Note that the device identifier is NOT used here!
    //This method updates the Presence only
    public void presenceChanged(Presence presence) {

    	Log.v(TAG + "::presenceChanged",  "Entered for user(without device): " + presence.getFrom().split("/")[0]);
    
    	
        //find the existing Roster entry- with new presence 
    	RosterEntry entry = this.roster.getEntry(presence.getFrom().split("/")[0]);
    	//find the associated FavoriteItem and update the presence and status
    	for (int i = 0; i < XMPPPresenceMediator.contacts.size(); i++)
        {
        	ChatObject fav= XMPPPresenceMediator.contacts.get(i);
        	if (fav.getJID() == entry.getUser())
        	{
        		Log.v(TAG + "::presenceChanged",  "UserFound");
        		
                String jid = entry.getUser();
        		fav.setPresence((presence.getType() == Presence.Type.available)?PresenceState.AVAILABLE:PresenceState.UNAVILABLE);
        		fav.setStatus(presence.getStatus());
        		fav.setName(entry.getUser().split("@")[0]);
        		break;
        	}
        }
    	
    	service.setState(XMPPPresenceMediator.contacts, XMPPListenerTypes.PERSISTANCE);
    	
    } // end of presenceChanged
     

}
