package com.fezzee.service;



import org.jivesoftware.smack.ConnectionListener;

import com.fezzee.data.XMPPListenerTypes;


import android.util.Log;



public class XMPPConnectionListener implements ConnectionListener {
	
	
    
    private XMPPService service;
    
	private final String TAG = "XMPPConnectionListener[Service]";
	
	public XMPPConnectionListener(XMPPService parent)
	{
		this.service = parent;
	}
			
	@Override
    public void reconnectionSuccessful() {
		service.setState("reconnectionSuccessful",XMPPListenerTypes.CONNECTION);
		Log.i(TAG,"reconnectionSuccessful");
		service.connect();
    }

    @Override
    public void reconnectionFailed(Exception arg0) {
    	service.setState("reconnectionFailed: " + arg0.getMessage(),XMPPListenerTypes.CONNECTION);
    	Log.e(TAG,"reconnectionFailed: " + arg0.getMessage());
    }

    @Override
    public void reconnectingIn(int arg0) {
    	service.setState("reconnectingIn: " + arg0,XMPPListenerTypes.CONNECTION);
    	Log.i(TAG,"reconnectingIn: " + arg0);
    }

    @Override
    public void connectionClosedOnError(Exception arg0) {
    	service.setState("reconnectionFailed: " + arg0.getMessage(),XMPPListenerTypes.CONNECTION);
    	Log.e(TAG,"reconnectionFailed: " + arg0.getMessage());
    }

    /*
     * Every 6 mins the library disconnects
     */
    @Override
    public void connectionClosed() {
    	service.setState("connectionClosed",XMPPListenerTypes.CONNECTION);
    	Log.i(TAG,"connectionClosed");
    	service.connect();
    	
    }
    

}
