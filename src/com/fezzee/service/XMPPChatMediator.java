package com.fezzee.service;

import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import com.fezzee.types.XMPPTypes;

import android.util.Log;
//import java.util.List;


public class XMPPChatMediator {
	
	private ChatManager manager;
	private final String TAG = "ChatMediator";
	private ArrayList<ChatManager> activeChats;
	private XMPPService service;

	public XMPPChatMediator(ChatManager chatManager, XMPPService service){
		this.activeChats = new ArrayList<ChatManager>();
		this.manager = chatManager;
		this.service = service;
	}
	
	public void registerChat(String jid) {
		
		manager.createChat(jid, new ChatMessageListener());
		
	}
	
	
	
	private class ChatMessageListener implements MessageListener {

		@Override
		public void processMessage(final Chat chat, final Message message) {
			Log.e(TAG, "Xmpp message received: '" + message.getBody() + "' on thread: " + getThreadSignature());
			service.setState("Xmpp message received: '" + message.getBody() + "' on thread: " + getThreadSignature(),XMPPTypes.CHAT);
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
