package com.fezzee.activity;



import java.util.Arrays;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.fezzee.patterns.Observable;
import com.fezzee.patterns.Observer;
import com.fezzee.persistance.PseudoDB;
import com.fezzee.service.connection.R;
import com.fezzee.types.XMPPTypes;

/*
 * A fragment activity/view pagers representing all chats, both active and historical.
 */
public class ChatHistoryActivity extends FragmentActivity implements ActionBar.TabListener {
	
	private static final String TAG = "ChatHistoryActivity";
	
	// for prototyping only
	// for now the size will always equal
	//private static final String[] collJIDS = {"gene1","gene2","gene3","gene5","gene6"};
	
	private static PseudoDB msgDatabase;

    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    private ViewPager mViewPager;
    
    private static Bundle passedArgs;
    
    public ChatHistoryActivity()
	{
		super();
		
	}
    
    public static void setChatDatabase(PseudoDB db)
    {
    	 //for prototyping
        msgDatabase = db;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
     
        
        //use a Bundle to pass args(JID from LocalBoundActivity) through this activity to fragment
        //FIXME: Not doing anything with the JID passed to this Activity
        passedArgs = getIntent().getExtras();
        
        Log.v(TAG+"::onCreate","passedArgs: "+ passedArgs);
        
        String selectedJID = passedArgs.getString("JID");
        
        Log.v(TAG+"::onCreate","passed JID: "+ selectedJID);

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        
        Log.d(TAG,"Actionbar: " + actionBar);

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setIcon(R.drawable.chaticon)
                            .setTabListener(this));
        }
        
        Button send = (Button) this.findViewById(R.id.sendBtn);
	    send.setOnClickListener(new View.OnClickListener() {
	      public void onClick(View view) {
	    	  
	    	  
	    	  //updateTest("gene2","my new Test");
	    	 
	      }
	    });
    }
    

    
    
	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
		
	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
		 mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
		
	} 

    /*
     * This inner class i the Observer
     */
    public class AppSectionsPagerAdapter extends FragmentStatePagerAdapter implements Observer{
    	
        public AppSectionsPagerAdapter(FragmentManager fragmentManager) {
        	super(fragmentManager);
        	this.setObservable(MainConnectionActivity.myService);
        }
 
        @Override
        public int getCount() {
        	Log.e(TAG,"SIZE::::::::::::" + msgDatabase.size());
            return msgDatabase.size();
        }
 
        @Override
        public Fragment getItem(int position) {
        	   //containsJID returns the messages if the JID exists
        	    Log.d(TAG,"getItem() called: " + position + " : " + msgDatabase.getNames()[position] + " : " + msgDatabase.ifContainsJidReturnMsgs(msgDatabase.getNames()[position]));
            	return ChatFragment.init(msgDatabase.getNames()[position],msgDatabase.ifContainsJidReturnMsgs(msgDatabase.getNames()[position]));
                
        }
        
        @Override
        public CharSequence getPageTitle(int position) {
            //
            return msgDatabase.getNames()[position];
        }
        
        
        /*
    	 *
    	 */
    	@Override
    	public void setObservable(Observable obj)
    	{
    	    obj.register(this, XMPPTypes.CHAT);
    	}

    	//public int getId()
    	//{
    	//	return ChatHistoryActivity.this.getTaskId();
    	//}
        
    	
    	 /*
         * THIS IS THE MOST IMPORTANT METHOD IN THIS CLASS
         * this HACK came from- see my note inline in comments as well
         * http://stackoverflow.com/questions/12705342/refreshing-a-view-inside-a-fragment
         */
    	@Override
    	public void update(final Object msg) {
    		
    		String jid = ((org.jivesoftware.smack.packet.Message)msg).getFrom();
    		String host = jid.substring(0, jid.indexOf('@'));
    		//String body = ((org.jivesoftware.smack.packet.Message)msg).getBody();
    		Log.e(TAG,"update() JID: '" + host + "'");

    	    
    	    int pos = Arrays.asList(msgDatabase.getNames()).indexOf(host);
       	    if (pos == -1) 
       	    {
       		   Log.e(TAG,"update() JID not found, can not update: '" + host+ "'");
       		   return; 
       	    }

   		    ChatFragment frag = (ChatFragment)this.instantiateItem( ChatHistoryActivity.this.mViewPager, pos );
   		    //msgDatabase.setMessage(host, body);
  		    
   		    if (mViewPager.getCurrentItem() == pos || mViewPager.getCurrentItem() == pos-1 || mViewPager.getCurrentItem() == pos+1)
   		    {
   		         Log.e(TAG,"Current page visible ******");
   			    //this seems like a hack but it works to update the list if the list if the current page
   			    //just detaching and reattaching the fragment.
   			    //if you're not on the fragment thats being updated, this not required
   			    //but if its called when the page isn't the current  view (+/- 1 on either side) it causes a crash
   			    FragmentManager fragmentManager = getSupportFragmentManager();
   			    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
   			    fragmentTransaction.detach(frag);
   			    fragmentTransaction.attach(frag);
   			    fragmentTransaction.commit();
   		     } 	 
    	}
    }// end of inner class AppSectionsPagerAdapter
}// end of class ChatHistoryAdapter


    


