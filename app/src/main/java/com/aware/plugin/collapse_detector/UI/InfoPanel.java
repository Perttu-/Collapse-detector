package com.aware.plugin.collapse_detector.UI;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;

import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;


import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.aware.plugin.collapse_detector.CollapseInfo;
import com.aware.plugin.collapse_detector.DatabaseHandler;
import com.aware.plugin.collapse_detector.R;

import java.util.List;


public class InfoPanel extends FragmentActivity {
    private String[] array;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    Toolbar toolbar;

    public static Intent intent2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        array = getResources().getStringArray(R.array.items);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList= (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_listview_item, array));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setTitle(mTitle);


        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        //setSupportActionBar(toolbar);

        mTitle = mDrawerTitle = getTitle();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,toolbar,
                R.string.drawer_open, R.string.drawer_close){

            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);

                //invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);

                //invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        toolbar.setTitle(array[0]);
        Fragment fragment1 = new StartFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment1).commit();



    }

    private void clearDatabase(){
        DatabaseHandler db = new DatabaseHandler(this);
        List<CollapseInfo> coll_list=db.getAllCollapses();
        for(CollapseInfo ci : coll_list){
            db.deleteCollapse(ci);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                selectItem(position);
                displayView(position);
            }
        }

    private void selectItem(int position) {

        mDrawerList.setItemChecked(position, true);
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
                fragment = new StartFragment();
                break;

            case 1:
               // fragment = new GMapFragment();
                //Needs to be fixed. Now this opens a new activity although it should open a fragment
                intent2 = new Intent(getApplicationContext(), MapScreen.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                break;

            case 2:
                fragment = new FirstAidFragment();
                break;

            case 3:
                fragment = new SafetyFragment();
                break;
            case 4:
                fragment = new DebugFragment();
                break;


            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            toolbar.setTitle(array[position]);
            mDrawerLayout.closeDrawer(mDrawerList);


        }

        if (fragment == null) {
            //Toast.makeText(getApplicationContext(), "Fragment failed", Toast.LENGTH_SHORT).show();
        }
    }


}
