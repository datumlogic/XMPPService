package com.fezzee.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fezzee.service.connection.R;





/**
 * A fragment representing a single chat.
 * "In short, fragments use listeners to communicate with activities and activities just use public methods in fragments."
 * http://stackoverflow.com/questions/12176546/update-a-view-in-a-fragment
 */
public class ChatFragment extends ListFragment {

    //public static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "ChatFragment";
    
    //private XMPPService myService;    
	//private boolean isBound = false;
    
    ArrayList<String> arr;

    static ChatFragment init(String jidHost, ArrayList<String> msg) {
    	
        ChatFragment chatList = new ChatFragment();
        
        chatList.arr = (ArrayList<String>)msg.clone();
        
        Bundle args = new Bundle();
        args.putString("jid", jidHost);
        chatList.setArguments(args);
 
        return chatList;
    }
    
    
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_chat, container, false);
        final Bundle args = getArguments();
        
        //Log.d(TAG,"Chat Index: " + args.getInt(ARG_SECTION_NUMBER));
        Log.d(TAG,"Chat JID: " + args.getString("jid"));
       
        
        ((TextView) rootView.findViewById(R.id.text)).setText(
                getString(R.string.dummy_text, args.getString("jid"))); //args.getInt(ARG_SECTION_NUMBER)));
        
        MainConnectionActivity.myService.createChat(args.getString("jid") + "@ec2-54-201-47-27.us-west-2.compute.amazonaws.com");
        
        
        
        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        	setListAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, arr));
        
    }
    
    
  
}
