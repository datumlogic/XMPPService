package com.fezzee.service.connection;



import org.jivesoftware.smack.ConnectionListener;


import android.util.Log;



public class ConnectionObserver implements ConnectionListener {
	
	
    
    private ConnectionService service;
    
	private final String TAG = "ConnectionObserver";
	
	public ConnectionObserver(ConnectionService parent)
	{
		this.service = parent;
	}
			
	@Override
    public void reconnectionSuccessful() {
		service.postMessage("reconnectionSuccessful");
		Log.i(TAG,"reconnectionSuccessful");
		service.connect();
    }

    @Override
    public void reconnectionFailed(Exception arg0) {
    	service.postMessage("reconnectionFailed: " + arg0.getMessage());
    	Log.e(TAG,"reconnectionFailed: " + arg0.getMessage());
    }

    @Override
    public void reconnectingIn(int arg0) {
    	service.postMessage("reconnectingIn: " + arg0);
    	Log.i(TAG,"reconnectingIn: " + arg0);
    }

    @Override
    public void connectionClosedOnError(Exception arg0) {
    	service.postMessage("reconnectionFailed: " + arg0.getMessage());
    	Log.e(TAG,"reconnectionFailed: " + arg0.getMessage());
    }

    /*
     * Every 6 mins the library disconnects
     */
    @Override
    public void connectionClosed() {
    	service.postMessage("connectionClosed");
    	Log.i(TAG,"connectionClosed");
    	service.connect();
    	
    }
    

}
