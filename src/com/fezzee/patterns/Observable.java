package com.fezzee.patterns;

import com.fezzee.types.XMPPTypes;


public interface Observable {
  
    //methods to register and unregister observers
    public void register(Observer obj, XMPPTypes type);
    public void unregister(Observer obj, XMPPTypes type);
  
    //method to notify observers of change
    public void notifyObservers(XMPPTypes type);
  
    //method to get updates from subject
    public Object getUpdate(Observer obj,XMPPTypes type);
  
}
