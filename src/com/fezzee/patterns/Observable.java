package com.fezzee.patterns;

import com.fezzee.data.XMPPListenerTypes;


public interface Observable {
  
    //methods to register and unregister observers
    public void register(Observer obj, XMPPListenerTypes type);
    public void unregister(Observer obj, XMPPListenerTypes type);
  
    //method to notify observers of change
    public void notifyObservers(XMPPListenerTypes type, Object msg);
  
    //method to get updates from subject
    //public Object getState(Observer obj,XMPPTypes type);
  
    public void setState(Object msg, XMPPListenerTypes type);
}
