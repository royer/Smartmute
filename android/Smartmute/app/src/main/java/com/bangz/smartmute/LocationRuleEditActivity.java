/*
 * Copyright (c) 2015 Royer Wang. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bangz.smartmute;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
//import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bangz.smartmute.util.ApiAdapter;
import com.bangz.smartmute.util.ApiAdapterFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import com.bangz.smartmute.content.LocationCondition;
import com.bangz.smartmute.content.RulesColumns;
import com.bangz.smartmute.services.LocationMuteService;
import com.bangz.smartmute.util.LogUtils;
import com.bangz.smartmute.util.PrefUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;


public class LocationRuleEditActivity extends RuleEditActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        SeekBar.OnSeekBarChangeListener,
        GoogleMap.OnMarkerDragListener,
        LocationListener {

    private static final String TAG = LocationRuleEditActivity.class.getSimpleName();


    GoogleMap       mMap ;
    LatLng          mLatLng ;

    Marker          mMarker ;
    MarkerOptions   mMarkerOpts ;
    private static final String KEY_MARKER = "Marker";
    private Circle  mCircle ;



    private GoogleApiClient mGoogleApiClient ;

    SupportMapFragment mapFragment ;


    private SeekBar mViewRadius ;
    private TextView mTextViewRadius ;



    private EditText mEditName ;

    private Switch   mSwitchActivited;
    private RadioGroup mViewRingMode ;
    private Spinner  mSpinnerTransType;
    private EditText mEditLoitering;
    private EditText mEditNotifyDelay;


    private Location        mLastLocation ;

    private String[]        mTransTypes ;

    // To remember map had be initialized, if false, need zoom map to mLatLng ;
    private boolean mbMapInited = false ;
    private static final String KEY_MAPINITED = "MapInited";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_rule_edit);

        Intent intent = getIntent();
        mLatLng = intent.getParcelableExtra(Constants.INTENT_LATLNG);


        mapFragment = (SupportMapFragment)getSupportFragmentManager().
                findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);

        mEditName = (EditText)findViewById(R.id.RuleName);
        mSwitchActivited = (Switch)findViewById(R.id.Activited);
        mSwitchActivited.setChecked(true);

        mViewRadius = (SeekBar)findViewById(R.id.seekRadius);
        mViewRadius.setMax((int)Constants.MAX_RADIUS - (int) Constants.MIN_RADIUS);
        mViewRadius.setOnSeekBarChangeListener(this);
        mTextViewRadius = (TextView)findViewById(R.id.txtRadius);

        mViewRingMode = (RadioGroup)findViewById(R.id.ringmode);

        mSpinnerTransType = (Spinner)findViewById(R.id.spinTrans);
        mSpinnerTransType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mEditLoitering.setVisibility(View.INVISIBLE);
                } else if (position == 1) {
                    mEditLoitering.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mEditLoitering = (EditText)findViewById(R.id.txtLoitering);
        mEditNotifyDelay = (EditText)findViewById(R.id.txtNotifyDelay);

        mTransTypes = getResources().getStringArray(R.array.transition_type);



        if (savedInstanceState == null) {


            if (getMode() == Constants.INTENT_NEW) {
                int radius = (int)PrefUtils.getDefaultRadius(this);
                mTextViewRadius.setText(String.format("%d", radius ));
                mViewRadius.setProgress(radius - (int)Constants.MIN_RADIUS);
            }

        } else {
            mbMapInited = savedInstanceState.getBoolean(KEY_MAPINITED) ;
            mMarkerOpts = savedInstanceState.getParcelable(KEY_MARKER);


        }

        if (getMode() == Constants.INTENT_NEW && mLatLng == null &&
                mbMapInited == false) {
            // Need query current location
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mGoogleApiClient.connect();
        }

        if (ApiAdapterFactory.getApiAdapter().getLocationMode(this) <=
                ApiAdapter.LOCATION_MODE_SENSORS_ONLY) {
            //TODO mention use adjust location mode
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(getString(R.string.btn_text_location_setting),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setTitle(getString(R.string.Warning));
            builder.setMessage(getString(R.string.need_location_service_message));

            AlertDialog dialog = builder.create();
            dialog.show();

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap ;

        mMap.setOnMarkerDragListener(this);

        setupMapIfNeed(getCursor()) ;
    }

    private void setupMapIfNeed(Cursor cursor) {

        if (!mbMapInited && getMode() == Constants.INTENT_NEW && mLatLng != null) {
            // it is fresh start activity to add a new place that already have LatLng,
            // add this marker
            mMarkerOpts = new MarkerOptions().position(mLatLng)
                 .title("New Place")
                 .draggable(true);
            // TODO: get place name as marker title
            mMarker = mMap.addMarker(mMarkerOpts);
            updateCircle(mLatLng, PrefUtils.getDefaultRadius(this));

        } else if (!mbMapInited && getMode() == Constants.INTENT_NEW && mLatLng == null) {
            // It is fresh start activity to add a new place that with LatLng,
            // wait location update to fill the mLatLng.
        } else if (!mbMapInited && getMode() == Constants.INTENT_EDIT && cursor != null) {
            // It is fresh start activity to edit a exist place in database and database load is
            // finished

            makeMarkerFromDatabase(cursor);


        } else if (mbMapInited && mMarkerOpts != null) {
            // It is restore activitey and maker information is restored
            mMarker = mMap.addMarker(mMarkerOpts) ;
            updateCircle(mMarker.getPosition(), mViewRadius.getProgress()+Constants.MIN_RADIUS);

        }

        if (mbMapInited == false && mMarker != null) {

            LatLngBounds.Builder bb = new LatLngBounds.Builder();
            LatLng center = mMarker.getPosition();
            bb.include(center);
            float r = Constants.MAX_RADIUS ;
            bb.include(SphericalUtil.computeOffset(center,r,0));
            bb.include(SphericalUtil.computeOffset(center,r,90));
            bb.include(SphericalUtil.computeOffset(center,r,180));
            bb.include(SphericalUtil.computeOffset(center,r,-90));

            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(),18));
            //
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bb.build(),0));

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder().
                            tilt(45).target(center).
                            zoom(mMap.getCameraPosition().zoom).
                            build()
            ));


            mbMapInited = true ;
        }

    }

    void makeMarkerFromDatabase(Cursor cursor) {

        cursor.moveToFirst() ;

        mMarkerOpts = new MarkerOptions();
        String strname = cursor.getString(cursor.getColumnIndex(RulesColumns.NAME));
        mMarkerOpts.title(strname);
        int activated = cursor.getInt(cursor.getColumnIndex(RulesColumns.ACTIVATED));
        //TODO: set different icon depend activated status
        double lat = cursor.getDouble(cursor.getColumnIndex(RulesColumns.LATITUDE));
        double lng = cursor.getDouble(cursor.getColumnIndex(RulesColumns.LONGITUDE));
        float radius = cursor.getFloat(cursor.getColumnIndex(RulesColumns.RADIUS));
        mMarkerOpts.position(new LatLng(lat, lng)).draggable(true);


        mMarker = mMap.addMarker(mMarkerOpts);
        updateCircle(mMarker.getPosition(), radius);
    }

    private void updateCircle(final LatLng center, float radius) {

        if (mCircle != null) {
            mCircle.setCenter(center);
            mCircle.setRadius(radius);

        } else {
            mCircle = mMap.addCircle(new CircleOptions().center(center).radius(radius));
        }
    }

    private void updateCircle(float radius) {
        if (mCircle != null)
            mCircle.setRadius(radius);
    }

    private void updateCircle(final LatLng center) {
        if (mCircle != null) {
            mCircle.setCenter(center) ;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_MAPINITED, mbMapInited);
        outState.putParcelable(KEY_MARKER, mMarkerOpts);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int radius = mViewRadius.getProgress() + (int)Constants.MIN_RADIUS ;
        LogUtils.LOGD(TAG,"remember radius is: " + radius);

        mTextViewRadius.setText(String.format("%d",radius));

        //updateCircle(mMarkerOpts.getPosition(), radius);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


    private int getRingMode() {
        int idringmode = mViewRingMode.getCheckedRadioButtonId() ;
        if (idringmode == R.id.silence)
            return RulesColumns.RM_SILENT ;
        else if (idringmode == R.id.vibrate)
            return RulesColumns.RM_VIBRATE ;
        else
            return RulesColumns.RM_NORMAL ;
    }

    private void setRingMode(int ringmode) {
        if (ringmode == RulesColumns.RM_SILENT)
            mViewRingMode.check(R.id.silence);
        else if (ringmode == RulesColumns.RM_VIBRATE)
            mViewRingMode.check(R.id.vibrate);
    }

    private int getTransitionTypeFromSpinner() {
        int pos = mSpinnerTransType.getSelectedItemPosition();
        if (pos == 0) {
            return Geofence.GEOFENCE_TRANSITION_ENTER ;
        } else
            return Geofence.GEOFENCE_TRANSITION_DWELL;
    }

    private void setTransitionTypeToSpinner(int type) {

        if (type == Geofence.GEOFENCE_TRANSITION_DWELL)
            mSpinnerTransType.setSelection(1);
        else
            mSpinnerTransType.setSelection(0);
    }

    @Override
    public void updateView(Cursor cursor) {
        if (cursor == null)
            return ;

        cursor.moveToFirst() ;

        String name = cursor.getString(cursor.getColumnIndex(RulesColumns.NAME));
        if (name == null || (name != null && name.isEmpty()))
            name = getString(R.string.noname);
        mEditName.setText(name);

        mSwitchActivited.setChecked(
                (cursor.getInt(cursor.getColumnIndex(RulesColumns.ACTIVATED))!=0));

        String strcondition = cursor.getString(cursor.getColumnIndex(RulesColumns.CONDITION));
        LocationCondition condition = new LocationCondition(strcondition);


        int mRadius = (int)cursor.getFloat(cursor.getColumnIndex(RulesColumns.RADIUS));
        mViewRadius.setProgress(mRadius-(int)Constants.MIN_RADIUS);
        mTextViewRadius.setText(String.format("%d",mRadius));

        setRingMode(cursor.getInt(cursor.getColumnIndex(RulesColumns.RINGMODE)));

        setTransitionTypeToSpinner(condition.getTriggerCondition().getTransitionType());

        int mLoitering = condition.getTriggerCondition().getLoiteringDelay();
        String strLoitering = String.valueOf(mLoitering/(60*1000));
        mEditLoitering.setText(strLoitering);


        int mNotifyDelay = condition.getTriggerCondition().getNotificationDelay();
        String strNotifyDelay = String.valueOf(mNotifyDelay/1000);
        mEditNotifyDelay.setText(strNotifyDelay);

        if (mMap != null)
            setupMapIfNeed(cursor) ;

    }


    @Override
    public ContentValues getContentValues() {

        String strname = mEditName.getText().toString().trim();

        double longtitude = 0,latitude = 0 ;
        float radius = 0;

        longtitude = mMarker.getPosition().longitude;
        latitude = mMarker.getPosition().latitude ;
        radius = (float)mViewRadius.getProgress() + Constants.MIN_RADIUS ;

        int trans = getTransitionTypeFromSpinner();
        String strloitering = mEditLoitering.getText().toString();
        int loitering ;
        if (strloitering != null && !strloitering.isEmpty())
            loitering = Integer.valueOf(strloitering)*60*1000;
        else
            loitering = 0 ;
        //int  notifydelay = Integer.valueOf(mEditNotifyDelay.getText().toString())*1000;
        int notifydelay = 0;

        LocationCondition condition = new LocationCondition(longtitude, latitude, radius,
                trans, loitering, notifydelay);
        String strcondition = condition.BuildConditionString() ;

        int activited = mSwitchActivited.isChecked()?1:0;
        int ringmode = getRingMode() ;


        ContentValues values = new ContentValues() ;
        if (getMode() == Constants.INTENT_NEW) {
            values.put(RulesColumns.RULETYPE, RulesColumns.RT_LOCATION);
            values.put(RulesColumns.SECONDCONDITION, "");
        }

        values.put(RulesColumns.NAME, strname);
        values.put(RulesColumns.CONDITION, strcondition);
        values.put(RulesColumns.LATITUDE, latitude);
        values.put(RulesColumns.LONGITUDE, longtitude);
        values.put(RulesColumns.RADIUS, radius);
        values.put(RulesColumns.ACTIVATED, activited);
        values.put(RulesColumns.RINGMODE, ringmode);
        return values;
    }

    @Override
    public void onSuccessUpdateDatabase(Uri uri) {


        if (mSwitchActivited.isChecked()) {
            LocationMuteService.addGeofence(this, uri);
        } else {
            LocationMuteService.removeGeofence(this, uri);
        }

        setResult(RESULT_OK);

    }

    @Override
    public void onConnected(Bundle bundle) {


        startLocationUpdate();

    }

    @Override
    public void onConnectionSuspended(int i) {

        LogUtils.LOGD(TAG,"GgoogleApiClient onConnectionSuspended i="+i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        LogUtils.LOGD(TAG,"GoogleApiClient onConnectionFailed: "+connectionResult.toString());
    }

    private void startLocationUpdate() {
        LocationRequest locationRequest = new LocationRequest() ;
        locationRequest.setInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(1000);

        LocationServices.FusedLocationApi.
                requestLocationUpdates(mGoogleApiClient,locationRequest,this);
    }

    @Override
    public void onLocationChanged(Location location) {

        mLatLng = new LatLng(location.getLatitude(),location.getLongitude());

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);

        if (mMap != null)
            setupMapIfNeed(null);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        mTextViewRadius.setText(String.format("%d", progress + (int)Constants.MIN_RADIUS));
        updateCircle(progress + Constants.MIN_RADIUS);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        updateCircle(marker.getPosition());
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mMarker = marker ;
        updateCircle(marker.getPosition());
        mMarkerOpts.position(marker.getPosition());
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_location_rule_edit, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
