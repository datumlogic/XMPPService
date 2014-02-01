package com.fezzee.service;


import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import android.util.Log;


public class XMPPChatListener implements ChatManagerListener {
	
	
	private final String TAG = "ChatObserver";
	
	@Override
	public void chatCreated(final Chat chat, final boolean createdLocally) {
		if (!createdLocally) {
			chat.addMessageListener(new MyMessageListener());
		}
	}
	
	private class MyMessageListener implements MessageListener {

		@Override
		public void processMessage(final Chat chat, final Message message) {
			Log.e(TAG, "Xmpp message received: '" + message.getBody() + "' on thread: " + getThreadSignature());
			Log.d(TAG,"Participant: " + chat.getParticipant() + " : " + message.toXML());
			;
			// --> this is another thread ('Smack Listener Processor') not the
			// main thread!
			// you can parse the content of the message here
			// if you need to download something from the Internet, spawn a new
			// thread here and then sync with the main thread (via a
			// Handler)
		}
	}
	
	public static String getThreadSignature() {
		final Thread t = Thread.currentThread();
		return new StringBuilder(t.getName()).append("[id=").append(t.getId()).append(", priority=")
				.append(t.getPriority()).append("]").toString();
	}

	
}
