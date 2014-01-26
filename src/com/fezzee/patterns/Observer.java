package com.fezzee.patterns;

public interface Observer {

     //method to update the observer, used by Observable
     public void update();
   
     //attach with message to observe
     public void setObservable(Observable observable);
}
