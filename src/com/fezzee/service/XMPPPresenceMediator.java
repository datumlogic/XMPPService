package com.fezzee.service;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import android.util.Log;

import com.fezzee.data.ChatCollection;
//import com.fezzee.data.ChatCollection.ChatObject;
//import com.fezzee.data.ChatCollection.PresenceState;
import com.fezzee.data.XMPPListenerTypes;
import com.fezzee.service.connection.R;

public class XMPPPresenceMediator {
	
	private static final String TAG = "XMPPPresenceMediator[Service]";
	
    protected static CopyOnWriteArrayList<ChatCollection.ChatObject> contacts = new CopyOnWriteArrayList<ChatCollection.ChatObject>();
    //protected Roster roster;
    
    private Collection<RosterEntry> entries;
    protected static XMPPService service;
    protected Roster roster;
    
    /*
     * launcheractivity.connection.getRoster();
     */
    public XMPPPresenceMediator(Roster roster, XMPPService service){
    	
    	this.roster = roster;
    	this.service = service;
        //fetch the roster from the server   
        //roster = LauncherActivity.connection.getRoster();
        //Auto subscribe is the default
        roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        
    }
    
    
    
    //LauncherActivity.connection.addPacketListener(
    protected static class PresenceSubscriptionListener implements PacketListener {
    	
    	private static final String TAG = "PresenceSubscriptionListener";
    	
    	@Override
        public void processPacket(Packet paramPacket) {
            
            if (paramPacket instanceof Presence) {
                Presence presence = (Presence) paramPacket;
                String email = presence.getFrom();
                Log.v(TAG + "::processPacket","chat invite status changed by user: : "
                        + email + " calling listner");
                Log.v(TAG + "::processPacket","presence: " + presence.getFrom()
                        + "; type: " + presence.getType() + "; to: "
                        + presence.getTo() + "; " + presence.toXML());
         
                if (presence.getType().equals(Presence.Type.subscribe)) {
                	Log.v(TAG + "::processPacket","SUBSCRIBE");
                	
                
                	ChatCollection.ChatObject item = ChatCollection.getInstance().new  ChatObject(presence.getFrom(),R.drawable.ic_launcher, presence.getFrom().split("@")[0], "SUBSCRIBE NOTIFICATION", ChatCollection.PresenceState.NOTIFIED);
		       	    //contacts.add(item); 
		       	    
		       	   service.setState(contacts, XMPPListenerTypes.PERSISTANCE);
		       	    
                } else if (presence.getType().equals(
                        Presence.Type.subscribed)) {
                	Log.v(TAG + "::processPacket","SUBSCRIBED");//DIFFERENT than SUBSCRIBE
                	/*
                    Presence newp = new Presence(Presence.Type.unsubscribed);
                    newp.setMode(Presence.Mode.available);
                    newp.setPriority(24);
                    newp.setTo(presence.getFrom());
                    connection.sendPacket(newp);
                    */
                } else if (presence.getType().equals(
                        Presence.Type.unsubscribe)) {
                	Log.v(TAG + "::processPacket","UNSUBSCRIBE");
                	/*
                    Presence newp = new Presence(Presence.Type.unsubscribed);
                    newp.setMode(Presence.Mode.available);
                    newp.setPriority(24);
                    newp.setTo(presence.getFrom());
                    connection.sendPacket(newp);
                    */
                } else if (presence.getType().equals(
                        Presence.Type.unsubscribed)) {
                	Log.v(TAG + "::processPacket","UNSUBSCRIBED"); //DIFFERENT than UNSUBSCRIBE
                	/*
                    Presence newp = new Presence(Presence.Type.unsubscribed);
                    newp.setMode(Presence.Mode.available);
                    newp.setPriority(24);
                    newp.setTo(presence.getFrom());
                    connection.sendPacket(newp);
                    */
                }
            }// end of paramPacket instanceof Presence

        }// end of ProcessPacket
    } // end of PresenceSubscriptionListener
    
    
    protected static class SubscriptionFilter implements PacketFilter {
        public boolean accept(Packet packet) {
            if (packet instanceof Presence) {
                Presence presence = (Presence) packet;
                if (presence.getType().equals(Presence.Type.subscribed)
                        || presence.getType().equals(
                                Presence.Type.subscribe)
                        || presence.getType().equals(
                                Presence.Type.unsubscribed)
                        || presence.getType().equals(
                                Presence.Type.unsubscribe)) {
                    return true;
                }
            }
            return false;
        }
    } //end of SubscriptionFilter
 
} //end of Presence Mediator
