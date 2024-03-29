package com.fezzee.service;

import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import com.fezzee.activity.MainConnectionActivity;
import com.fezzee.data.ChatCollection;
import com.fezzee.data.XMPPListenerTypes;

import android.util.Log;

/*
 * Persists chat in background to DB
 */
public class XMPPChatMediator {
	
	private ChatManager manager;
	private final String TAG = "XMPPChatMediator[Service]";
	public ChatCollection collChatObjects;// = new PseudoDB();
	private XMPPService service;

	public XMPPChatMediator(ChatManager chatManager, XMPPService service){
		//this.activeChats = new ArrayList<ChatManager>();
		this.manager = chatManager;
		this.service = service;
		collChatObjects = ChatCollection.getInstance();
		collChatObjects.setObservable(this.service);
	}
	
	public void registerChat(String jid) {
		
		manager.createChat(jid, new ChatMessageListener());
		
	}
	
	
	
	private class ChatMessageListener implements MessageListener {

		@Override
		public void processMessage(final Chat chat, final Message message) {
			
			String msg = message.getBody();
			Log.d(TAG, "Xmpp message received: '" + msg + "' on thread: " + getThreadSignature());
			if (msg != null)
				service.setState(message,XMPPListenerTypes.CHAT);
			// --> this is another thread ('Smack Listener Processor') not the
			// main thread!
			// you can parse the content of the message here
			// if you need to download something from the Internet, spawn a new
			// thread here and then sync with the main thread (via a
			// Handler)
		}
		
		public String getThreadSignature() {
			final Thread t = Thread.currentThread();
			return new StringBuilder(t.getName()).append("[id=").append(t.getId()).append(", priority=")
					.append(t.getPriority()).append("]").toString();
		}
		
	}
	
	
}
