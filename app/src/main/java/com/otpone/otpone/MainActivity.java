package com.otpone.otpone;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * The Main Activity provides a Tabbed view of:
 * <ul>
 *     <li>The List of contacts to send messages to.({@link ContactsListFragment})</li>
 *     <li>The Sent messages history.({@link MessagesRecordFragment})</li>
 * </ul>
 */
public class MainActivity extends AppCompatActivity {

    private ViewPager viewpager;
    private ContactsAndMessagesRecordAdapter contactsAndMessagesRecordAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // We want to insert the Tabs of the ViewPager in the Action Bar.
        final ActionBar actionBar = getSupportActionBar();

        // Retrieve the ViewPager from Layout
        viewpager = (ViewPager) findViewById(R.id.message_and_contacts_details);

        // Set the Adapter on this ViewPager.
        contactsAndMessagesRecordAdapter = new ContactsAndMessagesRecordAdapter(getSupportFragmentManager());
        viewpager.setAdapter(contactsAndMessagesRecordAdapter);

        // Page change listener for the ViewPager
        viewpager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // Specify that tabs should be displayed in the action bar.

        // NOTE(regarding DEPRECATION): Google has now introduced a feature rich customizable,
        // ToolBar augmenting(and deprecating some ActionBar API) the ActionBar.
        // Cool thing is that, it is available in the support library.
        // I have decided not to use it because learning and getting familiar with
        // the API shall require some time.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Time to wire the Tabs in the Action Bar with a listeners.
        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // When the tab is selected, switch to the
                // corresponding page in the ViewPager.
                viewpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        // Attach the listeners to both the Tabs.
        //Tab 1
        actionBar.addTab(
                actionBar.newTab()
                        .setText("Contacts")
                        .setTabListener(tabListener));
        // Tab 2
        actionBar.addTab(
                actionBar.newTab()
                        .setText("Message History")
                        .setTabListener(tabListener));
    }


    // PAGER ADAPTER FOR CONTACTS AND MESSAGES RECORD--------------------------------------------------------------------

    private static class ContactsAndMessagesRecordAdapter extends FragmentPagerAdapter {

        public ContactsAndMessagesRecordAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch(position){
                case 0:
                    return new ContactsListFragment();
                    //break;
                case 1:
                    return new MessagesRecordFragment();
                    //break;
            }
            // Default: Contacts list
            return new ContactsListFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
