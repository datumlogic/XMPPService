package com.fezzee.activity;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.fezzee.patterns.Observable;
import com.fezzee.patterns.Observer;
import com.fezzee.service.XMPPService;
import com.fezzee.service.connection.R;




/**
 * A fragment representing a single chat.
 */
public class ChatFragment extends ListFragment implements Observer {

    public static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "ChatFragment";
    
    private final String[] arr = {"test"};
    
    private XMPPService myService;    
	//private boolean isBound = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_chat, container, false);
        final Bundle args = getArguments();
        Log.d(TAG,"Chat Number: " + args.getInt(ARG_SECTION_NUMBER));
        Log.d(TAG,"Chat JID: " + args.getString("JID"));
        //((ListView)rootView.findViewById(android.R.id.list));
        
        //((TextView) rootView.findViewById(android.R.id.text1)).setText(
        //        getString(R.string.dummy_text, args.getString("JID"))); //args.getInt(ARG_SECTION_NUMBER)));
        
        //LocalBoundActivity.myService.createChat(args.getString("JID") + "@ec2-54-201-47-27.us-west-2.compute.amazonaws.com");
        
        
        
        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        	setListAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, arr));
        
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
