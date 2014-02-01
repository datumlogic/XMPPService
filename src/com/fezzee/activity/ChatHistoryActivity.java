package com.fezzee.activity;


import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.fezzee.service.connection.R;

/*
 * A fragment activity/view pagers representing all chats, both active and historical.
 */
public class ChatHistoryActivity extends FragmentActivity implements ActionBar.TabListener {
	
	private static final String TAG = "ChatActivity";
	
	private static final String[] collJIDS = {"gene2","gene3","gene5","gene6"};//for prototyping only

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * chats in the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    ViewPager mViewPager;
    
    private static Bundle passedArgs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        //use a Bundle to pass args(JID from LocalBoundActivity) through this activity to fragment
        passedArgs = getIntent().getExtras();
        

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
        
        
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     * 
     * Maybe use FragmentStatePagerAdapter at a later stage to conserve memory?
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
    	

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
        	Fragment fragment;
            switch (i) {
                case 0:
                	// The first Fragment (leftmost) passes in the JID (from passedArgs Bundle) of the selected item
                	fragment = new ChatFragment();
                	//Bundle args = new Bundle();
                	passedArgs.putInt(ChatFragment.ARG_SECTION_NUMBER, i + 1);
                	fragment.setArguments(passedArgs);
                	return fragment;

                default:
                    // The other sections PASS IN JID's from the service(?)
                    fragment = new ChatFragment();
                    Bundle args = new Bundle();
                    args.putInt(ChatFragment.ARG_SECTION_NUMBER, i + 1);
                    args.putString("JID", collJIDS[i-1]);//prototype hack
                    fragment.setArguments(args);
                    return fragment;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	String rtn;// = "Chat " + (position + 1);
        	Log.d(TAG,"POSITION>>>>" + position);
        	if (position==0) 
        		rtn = "gene";
        	else
        		rtn = collJIDS[position-1];
        	
            return rtn;
        }
    }

}
