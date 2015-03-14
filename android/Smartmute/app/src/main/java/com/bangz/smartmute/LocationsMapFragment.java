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

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bangz.common.map.util.MarkerManager;
import com.bangz.smartmute.content.LocationCondition;
import com.bangz.smartmute.content.RulesColumns;
import com.bangz.smartmute.model.MarkerExInfo;
import com.bangz.smartmute.services.LocationMuteService;
import com.bangz.smartmute.util.LogUtils;
import com.bangz.smartmute.util.PrefUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import com.google.maps.android.SphericalUtil ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Created by wangruoyu on 15-03-02.
 */
public class LocationsMapFragment extends SupportMapFragment
implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor>
        , GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener
        , GoogleMap.OnMapLongClickListener
        , GoogleMap.OnMapClickListener
        , GoogleMap.OnMarkerClickListener
        , GoogleMap.OnInfoWindowClickListener
        , LocationListener {

    private static final String TAG = LocationsMapFragment.class.getSimpleName();

    public static final int ADD_PLACE_REQUEST = 1;

    GoogleMap mMap;
    MarkerManager<MarkerExInfo> markerManager ;
    private static final String MC_ID_DATABASE = "database";
    private static final String MC_ID_CURRPLACE = "currplace";
    private static final String MC_ID_NEWPLACE = "newplace";
    private static final int    MC_DATABASE = 1;
    private static final int    MC_CURRPLACE= 2;
    private static final int    MC_NEWPLACE= 3;

    Cursor      mCursor ;

    GoogleApiClient mGoogleApiClient ;
    LatLng        mLastLocation ;

    LatLngBounds    mAllDataBounds = null ;


    int             mapInitState ;
    private static final int MAP_INIT_BY_LASTLOCATION = 1;
    private static final int MAP_INIT_BY_CURRENTLOCATION = 2;
    private static final int MAP_INIT_BY_DATABASE = 4 ;

    private static final float DEFAULT_ZOOM   = 13 ;
    private static final int   MIN_BOUNDS_DISTANCE = 3000; //meters

    private static final String KEY_STATE_MYARGUMENT = "MyArguments";
    private static final String KEY_STATE_MAPINIT = "DatabaseInitState";
    private static final String KEY_STATE_MARKERS = "Markers";
    private static final String KEY_STATE_LOCATION_UPDATED = "LocationUpdated";
    private static final String KEY_STATE_LOCATION_UPDATE_TYPE = "LocationUpdateType";
    private static final String KEY_STATE_SELECT_MARKER = "LastSelectedMarker";


    private Marker markerSelected = null ;
    private MarkerExInfo meiSelected = null ; // only used for instance restore, screen rotation
    private Circle mCircle = null ;           //a circle on selected marker which
                                                // is a database record

    private ArrayList<MarkerExInfo> mSavedMarkerExInfos = new ArrayList<MarkerExInfo>();


    /**
     * When fragment get the first location update, set map bound include all location that in
     * database and current location. then set this value to true to avoid set map bound again
     * in later location update event or instant restart(e.t. screen rotated)
     */
    boolean bLocationUpdated = false;

    /**
     * two type location update in this fragment lifecycle, normal is no power type, one is high
     * accuracy, this is trigger by "current place" meun item, and once it get the location, then
     * switch back to no power update.
     *
     */
    private int mLocationUpdateType = LOCATION_UPDATE_NONE;
    private static final int LOCATION_UPDATE_NONE = 0; // no location update request register.
    private static final int LOCATION_UPDATE_NOPOWER = 1;
    private static final int LOCATION_UPDATE_HIACCURACY = 2;


    public LocationsMapFragment() {

        // When subclass from MapFragment, do not use outState to save you state data, if you do,
        // when in onCreate, onCreateView, onActivityCreated ,saveInstance.getParcelable will get
        // Class not found when unmarshalling from java.lang.ClassNotFoundException
        // the cause may be google play service clean class info in bundle,

        // http://stackoverflow.com/a/15973603/1036923
        //setArguments(new Bundle());
    }

    public static LocationsMapFragment newInstance() {
        LocationsMapFragment f = new LocationsMapFragment();

        return f ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getMapAsync(this);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();

        mGoogleApiClient.connect();

        if (savedInstanceState == null) {
            mLastLocation = PrefUtils.getLastLatLng(getActivity());
            mapInitState = 0;
        }
        else {

            // When subclass from MapFragment, do not use outState to save you state data which is
            // parcelable object, if you do, when in onCreate, onCreateView, onActivityCreated ,
            // saveInstance.getParcelable will get Class not found when unmarshalling from
            // java.lang.ClassNotFoundException. The cause may be google play service clean class
            // info in his saveInstance. to workaround this ,you add your all date to a new Bundle
            // and save this Bundle to outState in onSaveInstanceState method.

            // http://stackoverflow.com/a/15973603/1036923
            // in above site, the best answer is suggest use setArguments() when CTO new Bundle,
            // then onSaveInstanceState use getArguments()


            Bundle mybundle = savedInstanceState.getBundle(KEY_STATE_MYARGUMENT);
            mSavedMarkerExInfos = mybundle.getParcelableArrayList(KEY_STATE_MARKERS);
            mapInitState = mybundle.getInt(KEY_STATE_MAPINIT,0);
            bLocationUpdated = mybundle.getBoolean(KEY_STATE_LOCATION_UPDATED,false);
            mLocationUpdateType = mybundle.getInt(KEY_STATE_LOCATION_UPDATE_TYPE);

            meiSelected = mybundle.getParcelable(KEY_STATE_SELECT_MARKER);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LogUtils.LOGD(TAG,String.format("onActivityResult requestCode = %d, resultCode = %d",requestCode,resultCode));
        if (requestCode == ADD_PLACE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (markerSelected != null) {
                    MarkerExInfo mei = markerManager.getExtraInfo(markerSelected);
                    if (mei.getCollectionID() != MC_DATABASE) {
                        markerManager.remove(markerSelected);
                        unSelectMarker();
                    }
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Activity parentActivity = getActivity() ;

        if (!(parentActivity instanceof BaseActivity &&
                ((BaseActivity) parentActivity).isDrawerOpened())) {
            menu.clear();

            inflater.inflate(R.menu.menu_locations_map, menu);

            super.onCreateOptionsMenu(menu, inflater);
        }

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        Activity parentActivity = getActivity() ;

        if (!(parentActivity instanceof BaseActivity &&
                ((BaseActivity) parentActivity).isDrawerOpened())) {

            MenuItem discard = menu.findItem(R.id.action_discard) ;
            if (discard != null) {

                discard.setVisible(markerSelected != null &&
                    markerManager.getExtraInfo(markerSelected).getCollectionID() == MC_DATABASE);
            }
        }
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mLocationUpdateType != LOCATION_UPDATE_NONE) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            LogUtils.LOGD(TAG,"remove location update onPause, type = " + mLocationUpdateType);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null &&  mGoogleApiClient.isConnected()
                && mLocationUpdateType != LOCATION_UPDATE_NONE) {
            startLocationUpdate(mLocationUpdateType);
            LogUtils.LOGD(TAG, "resume location update onResume. type = " + mLocationUpdateType);
        } else {
            LogUtils.LOGD(TAG,"not resume location update onResume");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        if (mSavedMarkerExInfos.isEmpty() == false) {
            mSavedMarkerExInfos.clear();
        }

        Collection<Marker> newplaces =  markerManager.getCollection(MC_ID_NEWPLACE).getMarkers();

        for (Marker marker : newplaces) {
            mSavedMarkerExInfos.add(markerManager.getExtraInfo(marker));
        }
        Collection<Marker> currplace = markerManager.getCollection(MC_ID_CURRPLACE).getMarkers() ;
        for(Marker marker : currplace) {
            mSavedMarkerExInfos.add(markerManager.getExtraInfo(marker));
        }

        Bundle mybundle = new Bundle();
        mybundle.putParcelableArrayList(KEY_STATE_MARKERS,mSavedMarkerExInfos);
        mybundle.putInt(KEY_STATE_MAPINIT,mapInitState);
        mybundle.putBoolean(KEY_STATE_LOCATION_UPDATED,bLocationUpdated);
        mybundle.putInt(KEY_STATE_LOCATION_UPDATE_TYPE,mLocationUpdateType);

        if (markerSelected != null) {
            MarkerExInfo meiselected = markerManager.getExtraInfo(markerSelected);
            mybundle.putParcelable(KEY_STATE_SELECT_MARKER, meiselected);
        }

        outState.putBundle(KEY_STATE_MYARGUMENT,mybundle);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap ;

        setupMap();


        if (mLastLocation != null && mapInitState < MAP_INIT_BY_DATABASE) {
            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(mLastLocation, DEFAULT_ZOOM));
            mapInitState = MAP_INIT_BY_LASTLOCATION ;
        }

    }

    private void setupMap() {

        mMap.getUiSettings().setMapToolbarEnabled(false);
        //mMap.setMyLocationEnabled(true);
        //mMap.getUiSettings().setMyLocationButtonEnabled(true);

        markerManager = new MarkerManager(mMap) ;
        MarkerManager.Collection colData = markerManager.newCollection(MC_ID_CURRPLACE);
        colData.setOnMarkerClickListener(this);

        colData = markerManager.newCollection(MC_ID_DATABASE);
        colData.setOnMarkerClickListener(this);
        colData.setOnInfoWindowAdapter(new MyInfoWindowAddapter());

        colData = markerManager.newCollection(MC_ID_NEWPLACE);
        colData.setOnMarkerClickListener(this);


        mMap.setInfoWindowAdapter(markerManager);
        mMap.setOnMarkerClickListener(markerManager);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        restoreSavedMarkers();


        LoaderManager lm = getLoaderManager() ;
        lm.initLoader(1,null,this);

    }

    private void restoreSavedMarkers() {



        for (MarkerExInfo mei : mSavedMarkerExInfos) {
            switch(mei.getCollectionID()) {
                case MC_NEWPLACE:
                case MC_CURRPLACE:
                    addTempMarker(mei);
                    break;
                case MC_DATABASE:
                    LogUtils.LOGD(TAG,"Marker from Database should not in saveInstance");
                    break;
            }
        }

        mSavedMarkerExInfos.clear();
    }

    private void addTempMarker(MarkerExInfo mei) {
        String collectionid = null;
        String title ="";
        String snippet="" ;
        BitmapDescriptor icon = null ;

        switch(mei.getCollectionID()) {
            case MC_NEWPLACE:
                collectionid = MC_ID_NEWPLACE;
                title = getResources().getString(R.string.map_info_title_newplace);
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                break;
            case MC_CURRPLACE:
                collectionid = MC_ID_CURRPLACE;
                title = getResources().getString(R.string.map_info_title_currplace);
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                break;
            default:
                break;
        }
        snippet = getResources().getString(R.string.map_info_snippet_add);
        Marker marker = markerManager.getCollection(collectionid).addMarker(
                new MarkerOptions().position(mei.getPoint())
                .title(title)
                .snippet(snippet)
                .icon(icon),
                mei);

        if (meiSelected != null && meiSelected.equals(mei)) {
            selectMarker(marker);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_currentlocation:
                startLocationUpdate(LOCATION_UPDATE_HIACCURACY);
                break;
            case R.id.action_discard:
                deleteSelected();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteSelected() {
        if (markerSelected == null)
            return ;

        MarkerExInfo mei = markerManager.getExtraInfo(markerSelected);
        if (mei.getCollectionID() == MC_DATABASE) {

            markerManager.remove(markerSelected);

            //TODO remove geofence of this id;
            //getActivity().getContentResolver().delete(muri,null,null);
            LocationMuteService.deleteGeofence(getActivity(),new long[] {mei.getDatabaseId()});




        }

        unSelectMarker();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        LogUtils.LOGD(TAG, "onCreateLoader");
        String  strSelect = RulesColumns.RULETYPE + " = " + RulesColumns.RT_LOCATION ;
        return new CursorLoader(getActivity(), RulesColumns.CONTENT_URI,
                RulesColumns.COLUMNS,strSelect,null,null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mCursor = data ;

        LogUtils.LOGD(TAG,"onLoadFinished");
        cleanAllDatabaseMarker();
        LatLngBounds.Builder lb = new LatLngBounds.Builder();

        if (mCursor.getCount() > 0) {
            mCursor.moveToFirst();
            do {
                float lat = mCursor.getFloat(mCursor.getColumnIndex(RulesColumns.LATITUDE));
                float lng = mCursor.getFloat(mCursor.getColumnIndex(RulesColumns.LONGITUDE));
                LatLng latLng = new LatLng(lat, lng);
                lb.include(latLng);
                String strName = mCursor.getString(mCursor.getColumnIndex(RulesColumns.NAME));
                if (strName == null || (strName != null && strName.isEmpty()))
                    strName = getString(R.string.noname);
                long id = mCursor.getLong(mCursor.getColumnIndex(RulesColumns._ID));
                String strCondition = mCursor.getString(mCursor.getColumnIndex(RulesColumns.CONDITION));
                //LocationCondition condition = new LocationCondition(strCondition);
                int activited = mCursor.getInt(mCursor.getColumnIndex(RulesColumns.ACTIVATED));
                float radius = mCursor.getFloat(mCursor.getColumnIndex(RulesColumns.RADIUS));

                StringBuilder snipbuilder = new StringBuilder();
                snipbuilder.append("Radius:")
                        .append(String.format("%d", (int) radius))
                        .append("\n");



                MarkerExInfo mei = new MarkerExInfo(latLng,MC_DATABASE, id, mCursor.getPosition());

                Marker marker = markerManager.getCollection(MC_ID_DATABASE).addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title(strName)
                                .snippet(snipbuilder.toString()),
                        mei);

                if (meiSelected != null && meiSelected.equals(mei)) {
                    markerSelected = marker;
                }



            }while(mCursor.moveToNext());

            if (markerSelected != null) {
                selectMarker(markerSelected);
            }
            mAllDataBounds = lb.build();
            if (mapInitState < MAP_INIT_BY_DATABASE)
                setBoundaries(mAllDataBounds);

        }
        mapInitState = MAP_INIT_BY_DATABASE ;

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        LogUtils.LOGD(TAG,"onLoaderReset");
        mCursor = null ;
        cleanAllDatabaseMarker();
    }

    private void cleanAllDatabaseMarker() {
        //Collection<Marker> markers = markerManager.getCollection(MC_ID_DATABASE).getMarkers();

        if (markerSelected != null) {
            MarkerExInfo mei = markerManager.getExtraInfo(markerSelected) ;
            if (mei.getCollectionID() == MC_DATABASE) {
                if (mCircle != null) {
                    mCircle.remove();
                    mCircle = null ;
                }
                markerSelected = null ;
                meiSelected = null;
                getActivity().invalidateOptionsMenu();
            }
        }

        markerManager.getCollection(MC_ID_DATABASE).clear();


    }

    @Override
    public void onConnected(Bundle bundle) {

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location != null) {
            mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
            PrefUtils.rememberLastLocation(getActivity(),
                    mLastLocation.latitude, mLastLocation.longitude);
        } else {
            mLastLocation = PrefUtils.getLastLatLng(getActivity());
        }

        if (location != null && mMap != null &&
                mapInitState < MAP_INIT_BY_LASTLOCATION) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation,DEFAULT_ZOOM));
            mapInitState = MAP_INIT_BY_LASTLOCATION ;
        }

        if (mapInitState < MAP_INIT_BY_CURRENTLOCATION) {
            // Request update current location
            LogUtils.LOGD(TAG,"start high accuracy location update on ApiClient onConnect");
            startLocationUpdate(LOCATION_UPDATE_HIACCURACY);

        } else {
            if (mLocationUpdateType != LOCATION_UPDATE_NONE) {
                startLocationUpdate(mLocationUpdateType);
                LogUtils.LOGD(TAG,"start old location update type = "+mLocationUpdateType);
            }
        }
    }

    private void startLocationUpdate(int whichtype) {

        if (mGoogleApiClient == null) {
            LogUtils.LOGE(TAG,
                    String.format("mGoogleApiClient is null in startLocationUpdate(%d)",whichtype));
            return ;
        }
        if (mapInitState < MAP_INIT_BY_CURRENTLOCATION) {
            mapInitState = MAP_INIT_BY_CURRENTLOCATION ;
        }
        if (mLocationUpdateType != LOCATION_UPDATE_NONE) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        }
        LocationRequest lr = LocationRequest.create();
        switch (whichtype) {
            case LOCATION_UPDATE_HIACCURACY:
                lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                lr.setInterval(5*1000);
                break ;
            case LOCATION_UPDATE_NOPOWER:
                lr.setPriority(LocationRequest.PRIORITY_NO_POWER);
                lr.setInterval(60*60*1000); //60 min update
                lr.setFastestInterval(60*1000); // 1 min
                break;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, lr, this);

        mLocationUpdateType = whichtype ;
    }


    @Override
    public void onLocationChanged(Location location) {

        markerManager.getCollection(MC_ID_CURRPLACE).clear();

        LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());

        MarkerExInfo mei = new MarkerExInfo(latlng, MC_CURRPLACE);
        addTempMarker(mei);

        if (bLocationUpdated == false) {
            if (mAllDataBounds != null) {
                setBoundaries(mAllDataBounds.including(latlng));
            }
            bLocationUpdated = true ;
        }

        if (mLocationUpdateType == LOCATION_UPDATE_HIACCURACY) {
            //switch back to no power location update
            startLocationUpdate(LOCATION_UPDATE_NOPOWER);
        }

        PrefUtils.rememberLastLocation(getActivity(),latlng.latitude,latlng.longitude);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        // add a new place marker on the map. only one new place exist in anytime.

        unSelectMarker();

        markerManager.getCollection(MC_ID_NEWPLACE).clear();


        MarkerExInfo mei = new MarkerExInfo(latLng,MC_NEWPLACE);
        addTempMarker(mei);

        getActivity().invalidateOptionsMenu();

    }



    @Override
    public void onMapClick(LatLng latLng) {

        unSelectMarker();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        selectMarker(marker);
        return false;
    }

    private void selectMarker(Marker marker) {

        markerSelected = marker ;
        getActivity().invalidateOptionsMenu();


        removeCircle();

        MarkerExInfo mei = markerManager.getExtraInfo(marker);
        if (mei.getCollectionID() == MC_DATABASE) {
            mCursor.moveToPosition(mei.getCursorPosition());
            float radius = mCursor.getFloat(mCursor.getColumnIndex(RulesColumns.RADIUS));
            addCircle(marker, radius);

        }

        marker.showInfoWindow();

    }

    private void unSelectMarker() {
        if (markerSelected != null) {
            markerSelected = null;
            getActivity().invalidateOptionsMenu();
            removeCircle();
        }
    }


    private void removeCircle() {

        if (mCircle != null) {
            mCircle.remove();
            mCircle = null ;
        }
    }

    private void addCircle(Marker marker, float radius) {

        if (mCircle != null) throw new AssertionError(" mCircle must be null when call addCircle.");

        mCircle = mMap.addCircle(new CircleOptions()
            .center(marker.getPosition()).radius(radius));

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        MarkerExInfo mei = markerManager.getExtraInfo(marker) ;
        LatLng latlng = marker.getPosition() ;

        Uri     uri = null ;
        Intent intent = new Intent(getActivity(), LocationRuleEditActivity.class);
        switch(mei.getCollectionID()) {
            case MC_DATABASE:
                uri = ContentUris.withAppendedId(RulesColumns.CONTENT_URI, mei.getDatabaseId());
                intent.setData(uri);
                intent.putExtra(Constants.INTENT_EDITORNEW,Constants.INTENT_EDIT);
                startActivity(intent);
                break;
            case MC_CURRPLACE:
            case MC_NEWPLACE:
                intent.putExtra(Constants.INTENT_EDITORNEW,Constants.INTENT_NEW);
                intent.putExtra(Constants.INTENT_LATLNG, latlng);
                startActivityForResult(intent, ADD_PLACE_REQUEST);
                break;
        }

    }

    private void setBoundaries(LatLngBounds bounds) {
        if (mMap == null)
            return ;

        if (SphericalUtil.computeDistanceBetween(bounds.southwest, bounds.northeast) <
                MIN_BOUNDS_DISTANCE) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(),DEFAULT_ZOOM));
        } else
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,150));
    }

    private String makeTriggerInfoString(Cursor cursor) {
        String strcondition = cursor.getString(cursor.getColumnIndex(RulesColumns.CONDITION));
        LocationCondition condition = new LocationCondition(strcondition);

        float radius = cursor.getFloat(cursor.getColumnIndex(RulesColumns.RADIUS));
        LocationCondition.TriggerCondition tc = condition.getTriggerCondition();
        Resources res = getResources() ;

        if (tc.getTransitionType() == Geofence.GEOFENCE_TRANSITION_ENTER) {
            return String.format(res.getString(R.string.info_trigger_by_enter),(int)radius);
        } else {
            return String.format(res.getString(R.string.info_trigger_by_dwell),
                    (int)radius, tc.getLoiteringDelay()/Constants.ONE_MINUTE_IN_MS);
        }

    }



    public class MyInfoWindowAddapter implements GoogleMap.InfoWindowAdapter {

        private final View mContentView ;

        public MyInfoWindowAddapter() {
            mContentView = getActivity()
                    .getLayoutInflater().inflate(R.layout.map_info_contents,null);
        }
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView tv = (TextView)mContentView.findViewById(R.id.Title);
            tv.setText(marker.getTitle());

            MarkerExInfo exinfo = markerManager.getExtraInfo(marker);
            tv = (TextView)mContentView.findViewById(R.id.ClickHint);
            switch (exinfo.getCollectionID()) {
                case MC_DATABASE:
                    tv.setText(Html.fromHtml(getActivity().getString(R.string.click_to_edit)));
                    break;
                case MC_CURRPLACE:
                case MC_NEWPLACE:
                    tv.setText(Html.fromHtml(getActivity().getString(R.string.click_to_add)));
                    break;
            }

            mCursor.moveToPosition(exinfo.getCursorPosition());

            int Activited = mCursor.getInt(mCursor.getColumnIndex(RulesColumns.ACTIVATED));
            Resources res = getActivity().getResources() ;
            ImageView iv = (ImageView)mContentView.findViewById(R.id.imgActivated);
            if(Activited == 1)
                iv.setImageResource(R.drawable.ic_on);
            else
                iv.setImageResource(R.drawable.ic_off);
            String strtemp ;

            strtemp = makeTriggerInfoString(mCursor);
            CharSequence cstr = Html.fromHtml(strtemp);
            tv = (TextView)mContentView.findViewById(R.id.TriggerInfo);
            tv.setText(cstr);



            return mContentView;
        }
    }


}
