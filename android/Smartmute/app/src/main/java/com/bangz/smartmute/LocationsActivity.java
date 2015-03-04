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

import android.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;


public class LocationsActivity extends BaseActivity
implements RulelistFragment.OnRuleItemClickListerner
        , OnMapReadyCallback {


    private static final String TAG = LocationsActivity.class.getSimpleName();
    private static final String TAG_MAP = "map";
    private static final String TAG_LIST = "list";

    private String mCurrentFragmentTag ;

    private GoogleMap   mMap ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        setupNavDrawer();

        //MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        //MapFragment mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

//        LocationsMapFragment mapFragment = LocationsMapFragment.newInstance() ;
//        getFragmentManager().beginTransaction()
//                .replace(R.id.container, mapFragment,"map")
//                .commit();
//
//        mapFragment.getMapAsync(this);

        RulelistFragment fragment = RulelistFragment.newInstance(2);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, TAG_LIST)
                .commit();
        mCurrentFragmentTag = TAG_LIST;
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

        if (isDrawerOpened() == false) {
            MenuItem listview = menu.findItem(R.id.action_listview);
            MenuItem mapview = menu.findItem(R.id.action_mapview);
            listview.setVisible(mCurrentFragmentTag == TAG_MAP);
            mapview.setVisible(mCurrentFragmentTag == TAG_LIST);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.action_mapview) {
            LocationsMapFragment mapFragment = (LocationsMapFragment)
                    getSupportFragmentManager().findFragmentByTag(TAG_MAP);
            if (mapFragment == null) {
                mapFragment = LocationsMapFragment.newInstance() ;
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container,mapFragment,TAG_MAP)
                    .commit();
            mCurrentFragmentTag = TAG_MAP ;

            invalidateOptionsMenu();

        } else if (id == R.id.action_listview) {
            RulelistFragment fragment = (RulelistFragment)
                    getSupportFragmentManager().findFragmentByTag(TAG_LIST);
            if (fragment == null)
                fragment = RulelistFragment.newInstance(2);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment, TAG_LIST)
                    .commit();
            mCurrentFragmentTag = TAG_LIST ;

            invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;


    }

    @Override
    public void onRuleItemSelected(long id, int ruletype) {

    }
}
