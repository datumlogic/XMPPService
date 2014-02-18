package com.fezzee.activity;

import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fezzee.data.ChatCollection;
import com.fezzee.data.ChatCollection.ChatMessage;
import com.fezzee.service.connection.R;

public class ChatListAdapter  extends ArrayAdapter<ChatMessage> { 

	private static final String TAG = "ChatListAdapter";
	 
    Context context;
 
    public ChatListAdapter(Context context, int resourceId,
            CopyOnWriteArrayList<ChatCollection.ChatMessage> items) {
        super(context, resourceId, items);
        this.context = context;
    }
    
    
    /*private view holder class*/
    private class ViewHolder {
        TextView txtMsg;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	
        ViewHolder holder = null;
        
        final ChatCollection.ChatMessage rowItem = getItem(position);
        
        //Log.v(TAG+"::getView","Started");
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem_chat, null);
            
            holder = new ViewHolder();
            
            holder.txtMsg = (TextView) convertView.findViewById(R.id.msg);
            
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        //Log.e(TAG+"::getView","holder: "+ holder + " : rowItem" + rowItem);
        holder.txtMsg.setText(rowItem.getMessage());
           
        
        

        return convertView;
    }
    
}
