package com.fezzee.activity;


import java.util.List;

import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.fezzee.data.ChatCollection;
import com.fezzee.data.ChatCollection.ChatObject;
import com.fezzee.data.ChatCollection.PresenceState;
import com.fezzee.service.connection.R;
//import com.theopentutorials.android.R;
//import com.theopentutorials.android.beans.RowItem;
 
public class FavoritesListAdapter extends ArrayAdapter<ChatObject> {
	
	private static final String TAG = "FavoritesListAdapter";
 
    Context context;
 
    public FavoritesListAdapter(Context context, int resourceId,
            List<ChatObject> items) {
        super(context, resourceId, items);
        this.context = context;
    }
 
    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView txtName;
        TextView txtStatus;
        ImageView presView;
        Button btnActivate;
        Button btnBlock;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final ChatObject rowItem = getItem(position);
        
        //Log.v(TAG+"::getView","Started");
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem_favs2, null);
            holder = new ViewHolder();
            
            holder.txtName = (TextView) convertView.findViewById(R.id.title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
            holder.presView = (ImageView) convertView.findViewById(R.id.pres);
            holder.txtStatus = (TextView) convertView.findViewById(R.id.desc);
            holder.btnActivate = (Button) convertView.findViewById(R.id.btnActivate);
            holder.btnBlock = (Button) convertView.findViewById(R.id.btnBlock);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
 
        if (rowItem.getPresence() != PresenceState.NOTIFIED){
        	 holder.btnActivate.setVisibility(View.GONE);
        	 holder.btnBlock.setVisibility(View.GONE);
        	 holder.txtStatus.setVisibility(View.VISIBLE);
        }
        holder.txtStatus.setText(rowItem.getStatus());
        holder.txtName.setText(rowItem.getName());
        holder.imageView.setImageResource(rowItem.getImageId());
        holder.presView.setImageResource(getPresenceImageRef(rowItem.getPresence()));
        holder.btnActivate.setOnClickListener(new OnClickListener() 
        { 
            @Override
            public void onClick(View v) 
            {
                // Your code that you want to execute on this button click
                //Intent myIntent = new Intent(CurrentActivity.this, NextActivity.class);
                //CurrentActivity.this.startActivity(myIntent);
            	//Presence presence = rowItem.getPresence();
            	Log.d("onClick","BtnActivate");
            	Presence newp = new Presence(Presence.Type.subscribed);
                newp.setMode(Presence.Mode.available);
                newp.setPriority(24);
                newp.setFrom("XXX");
                newp.setTo(rowItem.getJID());
                
                //TODO: FIX
                //LauncherActivity.connection.sendPacket(newp);
                //Presence subscription = new Presence(
                //        Presence.Type.subscribe);
                //subscription.setTo(rowItem.getJID());
                //FavoritesActivity.connection.sendPacket(subscription);
            }

        }); 
        holder.btnBlock.setOnClickListener(new OnClickListener() 
        { 
            @Override
            public void onClick(View v) 
            {
            	Log.d("onClick","BtnBlock");
            	Presence newp = new Presence(Presence.Type.unsubscribed);                        
            	newp.setMode(Presence.Mode.available);                        
            	newp.setPriority(24);                        
            	newp.setTo(rowItem.getJID()); 
            	
            	//TODO: FIX
            	//LauncherActivity.connection.sendPacket(newp);
            }

        }); 
        return convertView;
    }
    
    
    public int getPresenceImageRef(PresenceState pres)
    {
    	int rtn = -1;
    	switch(pres)
    	{
    		case AVAILABLE:
    			rtn = R.drawable.subscribed;
    			break;
    		case UNAVILABLE:
    			rtn = R.drawable.unsubscribed;
    			break;
    		case NOTIFIED:
    			rtn = R.drawable.notified;
    			break;
    	}
    	return rtn;
    }
}