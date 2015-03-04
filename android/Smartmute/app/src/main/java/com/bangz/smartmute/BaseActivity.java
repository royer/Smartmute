/*
 * Copyright (c) 2015 Royer Wang. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bangz.smartmute;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;

import com.bangz.smartmute.util.LogUtils;
import com.bangz.smartmute.util.PlayServicesUtils;

/**
 * Created by wangruoyu on 15-02-28.
 */
public abstract class BaseActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String TAG = BaseActivity.class.getSimpleName() ;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;


    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    // symbols for navigationDrawer items position, which same as StringArray "navigation_items"
    // in resource
    protected static final int NAVDRAWER_ALL        = 0;
    protected static final int NAVDRAWER_LOCATIONS  = 1;
    protected static final int NAVDRAWER_WIFI       = 2;
    protected static final int NAVDRAWER_TIME       = 3;
    protected static final int NAVDRAWER_TURNOFF    = 4;
    protected static final int NAVDRAWER_BACKUP     = 5;
    protected static final int NAVDRAWER_RESTORE    = 6;
    protected static final int NAVDRAWER_ABOUT      = 7;

    protected static final int NAVDRAWER_INVALID    = -1;
    protected static final int NAVDRAWER_SEPARATOR  = -2;
    protected static final int NAVDRAWER_SEPARATOR_SPECIAL = -3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getTitle();

    }

    @Override
    public void onCatalogItemSelected(int position) {

        if (position == getSelfNavDrawerItem())
            return ;

        switch(position) {
            case NAVDRAWER_ALL:
                Intent intent = new Intent(this, RulelistActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_LOCATIONS:
                intent = new Intent(this, LocationsActivity.class);
                startActivity(intent);
                finish();
                break;
        }

    }

    @Override
    public void onDrawerViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses of
     * BaseActivity override this to indicate what navigation drawer item corresponds to them
     * return NAVDRAWER_INVALID to mean that this Activity should not have a Nav Drawer.
     *
     */
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_INVALID ;
    }

    /**
     * Setup the navigation drawer as appropriate.
     */
    protected void setupNavDrawer() {

        if (getSelfNavDrawerItem() == NAVDRAWER_INVALID) {
            return ;
        }
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
//        mNavigationDrawerFragment = NavigationDrawerFragment.CreateInstance(getSelfNavDrawerItem());
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.navigation_drawer,mNavigationDrawerFragment)
//                .commit();
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout)findViewById(R.id.drawer_layout), getSelfNavDrawerItem());



    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //setupNavDrawer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        PlayServicesUtils.checkGooglePlayServices(this);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTitle = title ;
    }

    public void onSectionAttached(int number) {
        //
        String[] titles = getResources().getStringArray(R.array.navigation_items);

        setTitle(titles[number - 1]);
    }


    protected NavigationDrawerFragment getNavDrawerFragment() {
        return mNavigationDrawerFragment ;
    }

    public boolean isDrawerOpened() {
        return mNavigationDrawerFragment != null && mNavigationDrawerFragment.isDrawerOpen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mNavigationDrawerFragment != null &&
                !mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(getOptionMenuResId(), menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    protected abstract int getOptionMenuResId();
}
