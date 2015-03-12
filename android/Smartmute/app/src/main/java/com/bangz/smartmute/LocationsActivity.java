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

import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.bangz.common.view.SlidingTabLayout;
import com.bangz.smartmute.util.LogUtils;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.OnMapReadyCallback;


public class LocationsActivity extends BaseActivity
implements RulelistFragment.OnRuleItemClickListerner
         {


    private static final String TAG = LocationsActivity.class.getSimpleName();

    private SlidingTabLayout mSlidingTabLayout ;
    private ViewPager       mViewPager ;

    private String mCurrentFragmentTag ;


    private LocationsMapFragment mMapFragment ;
    private RulelistFragment    mRuleListFragment ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        setupNavDrawer();


        mViewPager = (ViewPager)findViewById(R.id.viewpager);
        mViewPager.setAdapter(new LocationsPagerAdapter(getSupportFragmentManager()));

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the viewpager has had
        // it's PagerAdapter set/
        mSlidingTabLayout = (SlidingTabLayout)findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        //MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        //MapFragment mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

//        LocationsMapFragment mapFragment = LocationsMapFragment.newInstance() ;
//        getFragmentManager().beginTransaction()
//                .replace(R.id.container, mapFragment,"map")
//                .commit();
//
//        mapFragment.getMapAsync(this);

//        RulelistFragment fragment = RulelistFragment.newInstance(2);
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.container, fragment, TAG_LIST)
//                .commit();
    }

    @Override
    protected int getOptionMenuResId() {
        return R.menu.menu_locations;
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return BaseActivity.NAVDRAWER_LOCATIONS ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

//        if (isDrawerOpened() == false) {
//            MenuItem listview = menu.findItem(R.id.action_listview);
//            MenuItem mapview = menu.findItem(R.id.action_mapview);
//            listview.setVisible(mCurrentFragmentTag == TAG_MAP);
//            mapview.setVisible(mCurrentFragmentTag == TAG_LIST);
//        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

//        int id = item.getItemId();
//        if (id == R.id.action_mapview) {
//            LocationsMapFragment mapFragment = (LocationsMapFragment)
//                    getSupportFragmentManager().findFragmentByTag(TAG_MAP);
//            if (mapFragment == null) {
//                mapFragment = LocationsMapFragment.newInstance() ;
//            }
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container,mapFragment,TAG_MAP)
//                    .commit();
//            mCurrentFragmentTag = TAG_MAP ;
//
//            invalidateOptionsMenu();
//
//        } else if (id == R.id.action_listview) {
//            RulelistFragment fragment = (RulelistFragment)
//                    getSupportFragmentManager().findFragmentByTag(TAG_LIST);
//            if (fragment == null)
//                fragment = RulelistFragment.newInstance(2);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, fragment, TAG_LIST)
//                    .commit();
//            mCurrentFragmentTag = TAG_LIST ;
//
//            invalidateOptionsMenu();
//        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onRuleItemSelected(long id, int ruletype) {

    }

    public static class LocationsPagerAdapter extends FragmentPagerAdapter {

        public LocationsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch(position) {
                case 0:
                    fragment = LocationsMapFragment.newInstance();
                    LogUtils.LOGD(TAG,"LocationsMapFragment called.");
                    break;
                case 1:
                    fragment = RulelistFragment.newInstance(BaseActivity.NAVDRAWER_LOCATIONS+1);
                    break;
            }

            return fragment ;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String strTitle ;

            if (position == 0) {
                strTitle = "Map View";
            } else {
                strTitle =  "List View";
            }
            return strTitle;
        }
    }
}
