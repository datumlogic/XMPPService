package com.fezzee.activity;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fezzee.data.ChatCollection;
import com.fezzee.service.connection.R;





/**
 * A fragment representing a single chat.
 * "In short, fragments use listeners to communicate with activities and activities just use public methods in fragments."
 * http://stackoverflow.com/questions/12176546/update-a-view-in-a-fragment
 */
public class ChatFragment extends ListFragment {

    //public static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "ChatFragment";
    
    View rootView;
    
    ListAdapter listAdapter;
    
    protected ListView listView;
    
    
    private static HashMap<String,ChatFragment> mFragments = new HashMap<String,ChatFragment>();
    
    public CopyOnWriteArrayList<ChatCollection.ChatMessage> messages;

    static ChatFragment init(String jidHost, String name, CopyOnWriteArrayList<ChatCollection.ChatMessage> msgs) {
    	
    	ChatFragment chatList;
    	if (mFragments.containsKey(jidHost))
    	{
    		chatList = mFragments.get(jidHost);
    		Log.v(TAG,"init(): FOUND: " + chatList);
    	}
    	else
    	{
    		chatList = new ChatFragment();
    		chatList.messages = (CopyOnWriteArrayList<ChatCollection.ChatMessage>)msgs;
    		mFragments.put(jidHost, chatList);
    		Log.v(TAG,"init(): CREATED: " + chatList + " : " + chatList.messages );
    		
    	}
       
        
        Bundle args = new Bundle();
        args.putString("jid", jidHost);
        args.putString("name", name);
        //try{
        	chatList.setArguments(args);
        //} catch (IllegalStateException ese) {
        	
        //}
 
        return chatList;
    }
    

    
    //getListView().smoothScrollToPosition(listAdapter.getCount() -1);
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list_chat, container, false);
        final Bundle args = getArguments();
        
        //Log.d(TAG,"Chat Index: " + args.getInt(ARG_SECTION_NUMBER));
        Log.v(TAG,"onCreateView(): " + args.getString("jid"));
        
        listView = (ListView) rootView.findViewById(android.R.id.list);
       
       //This is the Bottom Bar in the Chat window- that has the name of the person you're chatting to. 
        ((TextView) rootView.findViewById(R.id.text)).setText(
                getString(R.string.dummy_text, args.getString("name"))); //args.getInt(ARG_SECTION_NUMBER)));
        
        //this stopped my crashing issue when I used the full JID rather than just the ID before the @ to init  the PseudoDB with dummy data
        if (this.messages==null)
        {
        	this.messages = new CopyOnWriteArrayList<ChatCollection.ChatMessage>();
        	Log.e(TAG+"::onCreateView", "CRASH AVOIDED: Messages are null: Created empty messages arraylist");
        }
        
        listAdapter = new ChatListAdapter(this.getActivity(),
        		android.R.layout.simple_list_item_1, this.messages);
        
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem_favorites, contacts);
        //Log.e(TAG+"::onCreateView","listView/listAdapter: "+listView+" : " + listAdapter);
        
		listView.setAdapter(listAdapter);
        /*
        ListView lv = ((ListView) rootView.findViewById(android.R.id.list));
        if (listAdapter != null)
        	lv.setSelection( listAdapter.getCount() - 1);
        else
        	Log.e(TAG,"listAdapter is NULL");
        */
        
        //MainConnectionActivity.myService.createChat(args.getString("jid") + "@ec2-54-201-47-27.us-west-2.compute.amazonaws.com");
        MainConnectionActivity.myService.createChat(args.getString("jid"));
        
        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        Log.v(TAG,"onActivityCreated() ");
        
         
        setListAdapter(listAdapter);
        	

        
    }
    
    
  
}
