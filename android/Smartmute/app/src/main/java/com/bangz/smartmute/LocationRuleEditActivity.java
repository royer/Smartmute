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

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
//import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import com.bangz.smartmute.R;

import com.bangz.smartmute.content.LocationCondition;
import com.bangz.smartmute.content.RulesColumns;
import com.bangz.smartmute.services.LocationMuteService;
import com.bangz.smartmute.util.LogUtils;
import com.bangz.smartmute.util.PrefUtils;


public class LocationRuleEditActivity extends RuleEditActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = LocationRuleEditActivity.class.getSimpleName();

    private String strName;
    private String strDescription;

    //private String strLongitude;
    //private String strLatitude;
    private double  mLongitude;
    private double  mLatitude;
    private float   mRadius;
    private boolean bActivited ;
    private int     mRingMode ;
    private int     mTransType;
    private long    mLoitering;
    private int     mNotifyDelay;


    private EditText mEditName ;
    private EditText mEditDescription;
    private EditText mEditLongitude;
    private EditText mEditLatitude;
    private EditText mEditRadius;
    private Switch   mSwitchActivited;
    private RadioGroup mViewRingMode ;
    private Spinner  mSpinnerTransType;
    private EditText mEditLoitering;
    private EditText mEditNotifyDelay;

    private GoogleApiClient mGoogleApiClient ;
    private Location        mLastLocation ;
    private boolean         mRequestLocationUpdate = true ;

    private String[]        mTransTypes ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_rule_edit);

        mEditName = (EditText)findViewById(R.id.RuleName);
        mEditDescription = (EditText)findViewById(R.id.txtDescription);
        mEditLatitude = (EditText)findViewById(R.id.txtLatitude);
        mEditLongitude = (EditText)findViewById(R.id.txtLongitude);
        mEditRadius = (EditText)findViewById(R.id.txtRadar);
        mSwitchActivited = (Switch)findViewById(R.id.Activited);

        mViewRingMode = (RadioGroup)findViewById(R.id.ringmode);

        mSpinnerTransType = (Spinner)findViewById(R.id.spinTrans);
        mEditLoitering = (EditText)findViewById(R.id.txtLoitering);
        mEditNotifyDelay = (EditText)findViewById(R.id.txtNotifyDelay);

        mTransTypes = getResources().getStringArray(R.array.transition_type);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();


        if (savedInstanceState == null) {
            strName = "";
            strDescription = "";

            mLongitude = 0.0;
            mLatitude = 0.0;

            mRadius = 0;
            bActivited = true;
            mRingMode = RulesColumns.RM_NORMAL;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


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
        strName = cursor.getString(cursor.getColumnIndex(RulesColumns.NAME));
        mEditName.setText(strName);

        strDescription = cursor.getString(cursor.getColumnIndex(RulesColumns.DESCRIPTION));
        mEditDescription.setText(strDescription);

        bActivited = (cursor.getInt(cursor.getColumnIndex(RulesColumns.ACTIVATED))!=0);
        mSwitchActivited.setChecked(bActivited);

        String strcondition = cursor.getString(cursor.getColumnIndex(RulesColumns.CONDITION));
        LocationCondition condition = new LocationCondition(strcondition);
        int locformat = PrefUtils.getLocatonFormat(this);

        mLongitude = cursor.getDouble(cursor.getColumnIndex(RulesColumns.LONGITUDE));
        String strtemp = Location.convert(mLongitude, locformat);
        mEditLongitude.setText(strtemp);

        mLatitude = cursor.getDouble(cursor.getColumnIndex(RulesColumns.LATITUDE));
        strtemp = Location.convert(mLatitude, locformat);
        mEditLatitude.setText(strtemp);

        mRadius = cursor.getFloat(cursor.getColumnIndex(RulesColumns.RADIUS));
        mEditRadius.setText(String.valueOf(mRadius));

        mRingMode = cursor.getInt(cursor.getColumnIndex(RulesColumns.RINGMODE));
        setRingMode(mRingMode);

        mTransType = condition.getTriggerCondition().getTransitionType();
        setTransitionTypeToSpinner(mTransType);

        mLoitering = condition.getTriggerCondition().getLoiteringDelay();
        String strLoitering = String.valueOf(mLoitering/(60*1000));
        mEditLoitering.setText(strLoitering);


        mNotifyDelay = condition.getTriggerCondition().getNotificationDelay();
        String strNotifyDelay = String.valueOf(mNotifyDelay/1000);
        mEditNotifyDelay.setText(strNotifyDelay);

    }

    @Override
    public boolean isModified() {
        if (mEditName.getText().toString().trim().equals(strName) == false) {
            return true;
        }
        if (mEditDescription.getText().toString().trim().equals(strDescription) == false) {
            return true ;
        }
        String strtemp = mEditLongitude.getText().toString().trim();
        if (strtemp.isEmpty() == false) {
            try {
                double dtemp = Location.convert(strtemp);
                if (dtemp != mLongitude)
                    return true;
            } catch (NullPointerException e) {
                if (mLongitude != 0.0)
                    return true;
            } catch (IllegalArgumentException e) {
                // wrong format, it must  modified by user, so return true.
                return true;
            }
        } else {
            if (mLongitude != 0.0)
                return true ;
        }

        strtemp = mEditLatitude.getText().toString().trim();
        if (strtemp .isEmpty() == false) {
            try {
                double dtemp = Location.convert(strtemp);
                if (dtemp != mLatitude)
                    return true;
            } catch (NullPointerException e) {
                if (mLatitude != 0.0)
                    return true;
            } catch (IllegalArgumentException e) {
                return true;
            }
        } else {
            if (mLatitude != 0.0)
                return true ;
        }

        strtemp = mEditRadius.getText().toString().trim();
        if ((strtemp.isEmpty() && mRadius != 0.0f) ||
                (strtemp.isEmpty() == false && strtemp.equals(String.valueOf(mRadius)) == false))
            return true ;

        if (mSwitchActivited.isChecked() != bActivited)
            return true;

        if (getRingMode() != mRingMode)
            return true ;

        strtemp = mEditLoitering.getText().toString();
        if(mLoitering != Integer.valueOf(strtemp)*60*1000) {
            return true;
        }

        strtemp = mEditNotifyDelay.getText().toString();
        if (mNotifyDelay != Integer.valueOf(strtemp)*1000) {
            return true;
        }

        if (mTransType != getTransitionTypeFromSpinner())
            return true ;

        return false;
    }

    @Override
    public ContentValues getContentValues() {

        String strname = mEditName.getText().toString().trim();
        if (strname.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_need_rule_name), Toast.LENGTH_SHORT).show();
            return null ;
        }

        String strdescription = mEditDescription.getText().toString().trim() ;

        String strLongitude = mEditLongitude.getText().toString().trim();
        if (strLongitude.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_location_cannot_empty), Toast.LENGTH_SHORT).show();
            return null ;
        }
        String strLatitude = mEditLatitude.getText().toString().trim();
        if (strLatitude.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_location_cannot_empty),Toast.LENGTH_SHORT).show();
            return null ;
        }
        double longtitude, latitude ;
        try {
            longtitude = Location.convert(strLongitude);
            latitude = Location.convert(strLatitude);
        }catch (NullPointerException e) {
            Toast.makeText(this, getString(R.string.toast_location_cannot_empty), Toast.LENGTH_SHORT).show();
            return null ;

        }catch (IllegalArgumentException e) {
            Toast.makeText(this, getString(R.string.toast_location_format_error), Toast.LENGTH_SHORT).show();
            return null ;
        }
        String strradar = mEditRadius.getText().toString().trim();
        float radar ;
        try {
            radar = Float.parseFloat(strradar) ;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format in radar.", Toast.LENGTH_SHORT).show();
            return null;
        }
        int trans = getTransitionTypeFromSpinner();
        int loitering = Integer.valueOf(mEditLoitering.getText().toString())*60*1000;
        int  notifydelay = Integer.valueOf(mEditNotifyDelay.getText().toString())*1000;

        LocationCondition condition = new LocationCondition(longtitude, latitude, radar,
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
        values.put(RulesColumns.DESCRIPTION, strdescription);
        values.put(RulesColumns.CONDITION, strcondition);
        values.put(RulesColumns.LATITUDE, latitude);
        values.put(RulesColumns.LONGITUDE, longtitude);
        values.put(RulesColumns.RADIUS, radar);
        values.put(RulesColumns.ACTIVATED, activited);
        values.put(RulesColumns.RINGMODE, ringmode);
        return values;
    }

    @Override
    public void onSuccessUpdateDatabase(Uri uri) {

        strName = mEditName.getText().toString().trim();
        strDescription = mEditDescription.getText().toString().trim();
        bActivited = mSwitchActivited.isChecked() ;
        mRingMode = getRingMode() ;
        mLongitude = Location.convert(mEditLongitude.getText().toString().trim());
        mLatitude = Location.convert(mEditLatitude.getText().toString().trim());
        mRadius = Float.parseFloat(mEditRadius.getText().toString().trim());

        mTransType = getTransitionTypeFromSpinner();
        mLoitering = Long.valueOf(mEditLoitering.getText().toString())*60*1000l;
        mNotifyDelay = Integer.valueOf(mEditNotifyDelay.getText().toString())*1000;

        if (bActivited) {
            LocationMuteService.addGeofence(this, uri);
        } else {
            LocationMuteService.removeGeofence(this, uri);
        }

    }

    @Override
    public void onConnected(Bundle bundle) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) ;
        LogUtils.LOGD(TAG,"onConnected. lastlocation = " + mLastLocation);
        if (mLastLocation != null && getMode() == Constants.INTENT_NEW ) {
            int locformat = PrefUtils.getLocatonFormat(this);
            mEditLatitude.setText(Location.convert(mLastLocation.getLatitude(),locformat));
            mEditLongitude.setText(Location.convert(mLastLocation.getLongitude(),locformat));
        }

        startLocationUpdate();
        mRequestLocationUpdate = true ;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void startLocationUpdate() {
        LocationRequest locationRequest = new LocationRequest() ;
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setFastestInterval(5000);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,locationRequest,this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (getMode() == Constants.INTENT_NEW) {
            int lcformat = PrefUtils.getLocatonFormat(this);
            mEditLatitude.setText(Location.convert(location.getLatitude(), lcformat));
            mEditLongitude.setText(Location.convert(location.getLongitude(), lcformat));
        }
        mRequestLocationUpdate = false;

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);

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
