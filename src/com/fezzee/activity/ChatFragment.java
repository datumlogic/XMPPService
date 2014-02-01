package com.fezzee.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fezzee.patterns.Observable;
import com.fezzee.patterns.Observer;
import com.fezzee.service.XMPPService;
import com.fezzee.service.connection.R;



/**
 * A fragment representing a single chat.
 */
public class ChatFragment extends Fragment implements Observer {

    public static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "ChatFragment";
    
    private XMPPService myService;    
	//private boolean isBound = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        final Bundle args = getArguments();
        Log.d(TAG,"Chat Number: " + args.getInt(ARG_SECTION_NUMBER));
        Log.d(TAG,"Chat JID: " + args.getString("JID"));
        ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                getString(R.string.dummy_text, args.getString("JID"))); //args.getInt(ARG_SECTION_NUMBER)));
        
        LocalBoundActivity.myService.createChat(args.getString("JID") + "@ec2-54-201-47-27.us-west-2.compute.amazonaws.com");
        
        Button send = (Button) rootView.findViewById(R.id.sendBtn);
	    send.setOnClickListener(new View.OnClickListener() {
	      public void onClick(View view) {
	    	Log.d(TAG,"Send Button Pressed: " + args.getString("JID"));
	        /*
	        String text = textMessage.getText().toString();          
	        Log.i("XMPPChatDemoActivity ", "Sending text " + text + " to " + recipient);
	        Message msg = new Message(recipient, Message.Type.chat);  
	        msg.setBody(text);
	        if (LauncherActivity.connection != null) {
	          //add checks here- should have something  to handle an empty recipient, but will do for now
	        	Log.i("XMPPChatDemoActivity ", "TO len: " + recipient.trim().length() + " HOST Len: " + LauncherActivity.HOST.length());
	          if (text.trim().length() > 0 && recipient.trim().length() > LauncherActivity.HOST.length() + 1 ) //+1 for @
	          {
	        	  LauncherActivity.connection.sendPacket(msg);
	        	  //GM New
		          String name = LauncherActivity.connection.getAccountManager().getAccountAttribute("name");
		          //messages.add(connection.getUser() + ":");
		          //messages.add(text);
		          messages.add(name + ": " + text);
		          setListAdapter();
		          //GM New
		          textMessage.setText("");
		          InputMethodManager imm = (InputMethodManager)getSystemService(
		        	      Context.INPUT_METHOD_SERVICE);
		        	imm.hideSoftInputFromWindow(textMessage.getWindowToken(), 0);
	          }
	        }
	        */
	      }
	    });
        
        
        return rootView;
    }
    
    /*
	 * adds clarity to the process?
	 * Replaced 
	 * 		myService = binder.getService();
	 * with
	 * 		setObservable(binder.getService());
	 * in 
	 * 		ServiceConnection::onServiceConnected
	 */
	@Override
	public void setObservable(Observable obj)
	{
		this.myService = (XMPPService)obj;
	}
	
    
	//can be called from any thread
	@Override
	public void update() {
		getActivity().runOnUiThread(new Runnable() {
		    public void run() {
		    	//String serviceVal = (String) myService.getUpdate(LocalBoundActivity.this,XMPPTypes.CONNECTION);
				//final TextView myTextView = (TextView)findViewById(R.id.myTextView);  
				//myTextView.append(serviceVal+"\n");//
				//if (myTextView.getLineCount()>0)
				//{
				//	int scroll_amount = (int) (myTextView.getLineCount() * myTextView.getLineHeight()) - (myTextView.getBottom() - myTextView.getTop());
				//	myTextView.scrollTo(0, scroll_amount);
				//} 
		    }
		});
		
	}
}
