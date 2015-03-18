package com.aware.plugin.collapse_detector;

import android.app.Activity;


import android.provider.SyncStateContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;


import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;


public class MainActivity extends FragmentActivity {
    private String[] array;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        array = getResources().getStringArray(R.array.items);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList= (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_listview_item, array));

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mTitle);


        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        //setSupportActionBar(toolbar);

        mTitle = mDrawerTitle = getTitle();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,toolbar,
                R.string.drawer_open, R.string.drawer_close){

            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                //toolbar.setTitle(mTitle);

                //invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
                //setTitle(toolbar, mTitle);

                //invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void setTitle(Toolbar toolbar, CharSequence title) {
        mTitle = title;
        toolbar.setTitle(mTitle);
    }
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                selectItem(position);
                displayView(position);
            }
        }

    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position
        //Fragment fragment = new HomeFragment();

        mDrawerList.setItemChecked(position, true);
        setTitle(mTitle);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Toast.makeText(getApplicationContext(), "Something", Toast.LENGTH_SHORT).show();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new MapFragment();
                break;

            case 1:
                fragment = new FirstAidFragment();
                break;

            case 2:
                fragment = new SafetyFragment();
                break;

            case 3:
                fragment = new NewsFragment();
                break;

            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentManager.beginTransaction()
//                    .replace(R.id.frame_container, fragment).commit();
            fragmentTransaction.replace(R.id.content_frame, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            Toast.makeText(getApplicationContext(), "Fragment failed", Toast.LENGTH_SHORT).show();

        }
    }


}
