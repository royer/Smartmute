
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

package com.bangz.smartmute.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.bangz.smartmute.R;
import com.bangz.smartmute.content.LocationCondition;
import com.bangz.smartmute.receiver.LocationProviderChangedReceiver;
import com.bangz.smartmute.util.ApiAdapter;
import com.bangz.smartmute.util.ApiAdapterFactory;
import com.bangz.smartmute.util.ReceUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.bangz.smartmute.Constants;
import com.bangz.smartmute.content.RulesColumns;
import com.bangz.smartmute.util.LogUtils;
import com.bangz.smartmute.util.PrefUtils;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LocationMuteService extends IntentService
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LocationMuteService.class.getSimpleName() ;

    private static String[] PROJECTS = new String[] {
        RulesColumns._ID,
        RulesColumns.RULETYPE,
        RulesColumns.ACTIVATED,
        RulesColumns.LATITUDE,
        RulesColumns.LONGITUDE,
        RulesColumns.RADIUS,
        RulesColumns.CONDITION,
        RulesColumns.RINGMODE

    };
    private static String SELECT_STRING = RulesColumns.RULETYPE + " = ? AND "
            + RulesColumns.ACTIVATED + " = ?" ;
    private static String[] DEF_SELECT_ARGUMENTS = new String[] {
        String.valueOf(RulesColumns.RT_LOCATION),
        "1"
    };


    public static final int GEOFENCE_NOT_AVLIABLE_NOTIFICATION_ID = 100;

    private GoogleApiClient mGoogleApiClient ;

    // TODO: Rename actions, choose action names that describe tasks that this
    private static final String ACTION_START_GEOFENCES =
            Constants.PACKAGE_NAME + "services.action.start_geofences" ;
    private static final String ACTION_SHUTDOWN_GEOFENCES =
            Constants.PACKAGE_NAME + "services.action.shutdown_geofences";
    private static final String ACTION_ADD_GEOFENCE =
            Constants.PACKAGE_NAME + "services.action.add_geofences";
    private static final String ACTION_REMOVE_GEOFENCE =
            Constants.PACKAGE_NAME + "services.action.remove_geofences";
    private static final String ACTION_DELETE_GEOFENCES =
            Constants.PACKAGE_NAME + "services.action.delete_geofences";
    private static final String ACTION_GEODEFENCE_TRIGGER =
            Constants.PACKAGE_NAME + "services.geodefence.trigger";

    private static final String PARAM_KEY_IDS = "IDS" ;





    public static void addGeofence(Context context, Uri uri) {

        Intent intent = new Intent(ACTION_ADD_GEOFENCE,uri, context, LocationMuteService.class);
        LogUtils.LOGD(TAG, " Pending add one geofence. id = " + ContentUris.parseId(uri));
        context.startService(intent);
    }

    public static void startAll(Context context) {
        Intent intent = new Intent(ACTION_START_GEOFENCES, null, context, LocationMuteService.class);
        LogUtils.LOGD(TAG, "Pending start geofences service.");
        context.startService(intent);
    }

    public static void removeGeofence(Context context, Uri uri) {
        Intent intent = new Intent(ACTION_REMOVE_GEOFENCE, uri, context, LocationMuteService.class);
        LogUtils.LOGD(TAG, "Pending remove one geofence. id = " + ContentUris.parseId(uri));
        context.startService(intent);
    }

    public static void deleteGeofence(Context context, long[] ids) {
        Intent intent = new Intent(ACTION_DELETE_GEOFENCES, null, context,
                LocationMuteService.class);

        intent.putExtra(PARAM_KEY_IDS,ids);
        LogUtils.LOGD(TAG,"Pending delete geofences. id = " + ids.toString());
        context.startService(intent);
    }

    public static void shutdownGeofences(Context context) {
        Intent intent = new Intent(ACTION_SHUTDOWN_GEOFENCES,null, context, LocationMuteService.class);
        LogUtils.LOGD(TAG, "Pending shutdown geofences.");
        context.startService(intent);
    }



    public LocationMuteService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {

        LogUtils.LOGD(TAG, " GooglePlayService OnConnected.");

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ADD_GEOFENCE.equals(action)) {
                handleAddGeofence(intent.getData());
            } else if (ACTION_START_GEOFENCES.equals(action)) {
                handleStartGeofences();
            } else if (ACTION_GEODEFENCE_TRIGGER.equals(action)) {
                handleGeofenceTrigger(intent);
            } else if (ACTION_REMOVE_GEOFENCE.equals(action)) {
                handleRemoveGeofence(intent.getData());
            } else if (ACTION_DELETE_GEOFENCES.equals(action)) {
                handleDeleteGeofences(intent.getLongArrayExtra(PARAM_KEY_IDS));
            }
        }
    }



    /**
     * Add all activated location position to Google geofencing services
     * TODO: adjust algorithm to fit geofencing service no more 100 geofences in one app.
     */
    private void handleStartGeofences() {

        LogUtils.LOGD(TAG,"Handling start geofences...");

        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            //TODO: notfiy user that we need Google Play Services.
            return ;
        }

        if (PrefUtils.isEnableSmartMute(this) == false) {
            LogUtils.LOGD(TAG,"Smart Mute is disabled by user. quit start geofence");
            return ;
        }

        ContentResolver cr = getContentResolver() ;
        Cursor cursor = cr.query(RulesColumns.CONTENT_URI,
                PROJECTS,
                SELECT_STRING,
                DEF_SELECT_ARGUMENTS,
                RulesColumns._ID);

        if (cursor.getCount() == 0) {
            LogUtils.LOGD(TAG,"\tNo record..., Leave handling start geofences. ");
            cursor.close();
            return ;
        }


        List<Geofence> geofences = fillGeofences(cursor);

        // no long need database
        cursor.close();


        boolean b = addGeofences(geofences);

        LogUtils.LOGD(TAG, (b ? "Successful":"Failed") + " started geofence.");


    }

    private void handleAddGeofence(Uri uri) {

        LogUtils.LOGD(TAG, "Handling add one geofence id = "+ContentUris.parseId(uri)) ;
        if (PrefUtils.isEnableSmartMute(this) == false) {
            LogUtils.LOGD(TAG,"Smart Mute is disabled by user. quit add one geofdence");
            return ;
        }

        ContentResolver cr = getContentResolver() ;

        Cursor cursor = cr.query(uri, PROJECTS, SELECT_STRING, DEF_SELECT_ARGUMENTS, RulesColumns._ID);
        if (cursor.getCount() == 0) {
            cursor.close();
            LogUtils.LOGD(TAG,"handleAddGeofence: no record selected,but id = " + ContentUris.parseId(uri));
            return ;
        }

        List<Geofence> geofences = fillGeofences(cursor) ;
        cursor.close();

        boolean b = addGeofences(geofences);
        LogUtils.LOGD(TAG, (b? "Successful":"Failed")
                + " add one geofence id = " + ContentUris.parseId(uri));

    }

    private void handleRemoveGeofence(Uri uri) {

        long id = ContentUris.parseId(uri) ;
        LogUtils.LOGD(TAG, "Handling Remove one Geofence id = " + id);

        List<String> removeids = new ArrayList<String>() ;
        removeids.add(String.valueOf(id));

        ConnectionResult cr = mGoogleApiClient.blockingConnect();
        if (cr.isSuccess() == false) {
            LogUtils.LOGD(TAG, "GoogleApiClient.blockconnect failed! message: " + cr.toString());
            return ;
        }
        Status result =
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, removeids).await() ;
        mGoogleApiClient.disconnect();


        LogUtils.LOGD(TAG,(result.isSuccess()?"Successful":"Failed")
                +" remove on geofence id = " + id);
    }

    private void handleDeleteGeofences(long[] ids) {


        LogUtils.LOGD(TAG, "Handling delete geofences...");
        List<String> removeids = new ArrayList<String>();
        for(long id : ids) {
            removeids.add(String.valueOf(id));
        }

        ConnectionResult cr = mGoogleApiClient.blockingConnect();
        if (cr.isSuccess() == false) {
            LogUtils.LOGD(TAG, "GoogleApiClient.blockconnect failed! message: " + cr.toString());
            return ;
        }
        Status result =
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, removeids).await();
        if (result.isSuccess()) {
            LogUtils.LOGD(TAG,"Delete geofences successful.");
        } else {
            LogUtils.LOGD(TAG,"Delete geofences fail. message:" + result.getStatusMessage());
        }
        mGoogleApiClient.disconnect();


        // now delete these records from database ;

        ContentResolver contentResolver = getContentResolver();
        String args = TextUtils.join(", ",removeids);
        String where = String.format("%s IN (%s)", RulesColumns._ID, args);
        contentResolver.delete(RulesColumns.CONTENT_URI, where, null);

    }


    private boolean addGeofences(List<Geofence> geofences) {

        ConnectionResult connresult = mGoogleApiClient.blockingConnect() ;
        if (connresult.isSuccess() == false) {
            LogUtils.LOGE(TAG, "GoogleApiClient.blockingConnect failed! message: " + connresult.toString());
            return false;
        }

        GeofencingRequest gR = new GeofencingRequest.Builder()
                .addGeofences(geofences)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER |
                GeofencingRequest.INITIAL_TRIGGER_DWELL |
                GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .build();
        PendingIntent pi =  getGeofencesPendingIntent(this);
        Status rs = LocationServices.GeofencingApi.addGeofences(mGoogleApiClient,
                                                                                gR, pi).await() ;
        boolean bret = false;
        if (rs.isSuccess()) {

            LogUtils.LOGD(TAG,"Add Geofence successful.");
            PrefUtils.Geofencing(this, true);
            ReceUtils.enableReceiver(this, LocationProviderChangedReceiver.class, false);
            bret = true ;

        } else {
            int errcode = rs.getStatusCode();
            bret = false ;
            LogUtils.LOGD(TAG,"Add Geofence failed! errorcode = "
                    + errcode + "Message:" +rs.getStatusMessage());

            if (errcode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {

                LocationManager lm =
                        (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                    LogUtils.LOGD(
                            TAG,"GPS or Network Provider is enabled. " +
                                    "Notification User to try add again late?");

                } else {

                    ReceUtils.enableReceiver(this, LocationProviderChangedReceiver.class, true);
                }

                NotificationUserFailed();

            }
        }


        mGoogleApiClient.disconnect();

        return bret;
    }

    private List<Geofence> fillGeofences( Cursor cursor) {

        List<Geofence> geofences = new ArrayList<Geofence>();
        long   id;
        double latitude, longitude ;
        float  radius;

        int  idxLatitude = cursor.getColumnIndex(RulesColumns.LATITUDE);
        int  idxLongitude = cursor.getColumnIndex(RulesColumns.LONGITUDE);
        int  idxRadius = cursor.getColumnIndex(RulesColumns.RADIUS);
        int  idxId = cursor.getColumnIndex(RulesColumns._ID);


        while (cursor.moveToNext()) {

            id = cursor.getLong(idxId);
            latitude = cursor.getDouble(idxLatitude);
            longitude = cursor.getDouble(idxLongitude);
            radius = cursor.getFloat(idxRadius);
            String strcondition = cursor.getString(cursor.getColumnIndex(RulesColumns.CONDITION));
            LocationCondition condition = new LocationCondition(strcondition);

            Geofence.Builder gb = new Geofence.Builder()
                    .setRequestId(String.valueOf(id))
                    .setTransitionTypes(
                            condition.getTriggerCondition().getTransitionType()
                                    |Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setCircularRegion(latitude, longitude, radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE);
            if (condition.getTriggerCondition().getTransitionType()
                    == Geofence.GEOFENCE_TRANSITION_DWELL)
                    gb.setLoiteringDelay(condition.getTriggerCondition().getLoiteringDelay());

            gb.setNotificationResponsiveness(
                    condition.getTriggerCondition().getNotificationDelay());

            Geofence geofence = gb.build();

            geofences.add(geofence);
        }

        return geofences ;
    }

    private void handleGeofenceTrigger(Intent intent) {

        LogUtils.LOGD(TAG,"Handling Geofence trigger ...");
        HashMap<Integer, String> mapRingerMode = new HashMap<Integer, String>();
        mapRingerMode.put(AudioManager.RINGER_MODE_NORMAL,"Normal");
        mapRingerMode.put(AudioManager.RINGER_MODE_SILENT,"Silent");
        mapRingerMode.put(AudioManager.RINGER_MODE_VIBRATE,"Vibrate");

        HashMap<Integer, String> mapTransition = new HashMap<Integer, String>();
        mapTransition.put(Geofence.GEOFENCE_TRANSITION_DWELL,"DWELL");
        mapTransition.put(Geofence.GEOFENCE_TRANSITION_ENTER,"ENTER");
        mapTransition.put(Geofence.GEOFENCE_TRANSITION_EXIT,"EXIT");

        GeofencingEvent geoEvent = GeofencingEvent.fromIntent(intent) ;

        if (geoEvent.hasError() == false) {
            LogUtils.LOGD(TAG,"\tgeoEvent has no error.");
            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            if (audioManager == null) {
                LogUtils.LOGD(TAG,"\t !!!!!  AudioManager == null !!!!!!!");
                return ;
            }
            int currringermode = audioManager.getRingerMode() ;

            List<Geofence> geofences = geoEvent.getTriggeringGeofences() ;

            int transition = geoEvent.getGeofenceTransition();
            ContentResolver cr = getContentResolver() ;

            //int enterTransition = Config.TEST_BUILD ? Geofence.GEOFENCE_TRANSITION_ENTER : Geofence.GEOFENCE_TRANSITION_DWELL;
            LogUtils.LOGD(TAG,"\tTransition: " + mapTransition.get(transition));
            if (transition == Geofence.GEOFENCE_TRANSITION_DWELL ||
                    transition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                boolean setted = false ;
                for(Geofence geofence : geofences) {
                    long id = Long.parseLong(geofence.getRequestId());
                    Uri uri = ContentUris.withAppendedId(RulesColumns.CONTENT_ID_URI_BASE, id);
                    Cursor cursor = cr.query(uri,PROJECTS,RulesColumns.ACTIVATED + " = 1",null,null);

                    if (cursor.getCount() != 0) {
                        cursor.moveToFirst();
                        int setmode = cursor.getInt(cursor.getColumnIndex(RulesColumns.RINGMODE));

                        if (currringermode == setmode) {
                            LogUtils.LOGD(TAG, "\tringer mode already is in silent or vibrate. we do nothing");
                        } else {

                            LogUtils.LOGD(TAG, "\tset ringer mode to " + setmode);
                            audioManager.setRingerMode(setmode);
                            PrefUtils.rememberWhoMuted(this, id);
                            //TODO Notify to user ?
                        }
                        setted = true ;

                    } else {
                        LogUtils.LOGD(TAG,"\tid = " + id + " trigger, but does not find in database. maybe disabled.");
                    }

                    cursor.close();
                    cursor = null ;

                    if (setted == true) {
                        break ;
                    }
                }
            } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                for (Geofence geofence : geofences) {
                    long id = Long.parseLong(geofence.getRequestId());
                    if (id == PrefUtils.getLastSetMuteId(this)) {
                        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                        if (am != null) {
                            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        }
                        PrefUtils.cleanLastMuteId(this);
                        break;
                    }
                }
            } else {
                LogUtils.LOGD(TAG,"transition is " + transition +
                        " ; != entertransition && !! EXIT");
            }

        } else {
            PrefUtils.Geofencing(this, false);
            if (geoEvent.getErrorCode() == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {

                NotificationUserFailed();

                ReceUtils.enableReceiver(this,LocationProviderChangedReceiver.class, true);
            } else {
                LogUtils.LOGD(TAG,"\tHandle Geofence trigger error. errcode = "
                        + geoEvent.getErrorCode());
            }
        }

        LogUtils.LOGD(TAG,"Successful Leave handling Geofence trigger.");
    }


    private void handleShutdown() {

        LogUtils.LOGD(TAG, "Handle shutdown geofences...");

        PendingIntent pi = getGeofencesPendingIntent(this);

        ConnectionResult rs = mGoogleApiClient.blockingConnect();
        if (rs.isSuccess() == false) {
            LogUtils.LOGD(TAG,"\tconnect Google Play Service fail. result = " + rs.toString());
        }

        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, pi);
        mGoogleApiClient.disconnect();
        PrefUtils.Geofencing(this, false);

        LogUtils.LOGD(TAG,"Successful shutdown geofence.");
    }

    private PendingIntent getGeofencesPendingIntent(Context context) {

        Intent intent = new Intent(ACTION_GEODEFENCE_TRIGGER,null, context, LocationMuteService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    private void NotificationUserFailed() {

        NotificationCompat.Builder nb = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setColor(getResources().getColor(R.color.theme_accent_2))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);


//        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
//        boolean bProviderEnabled =
//                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) |
//                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        int locationmode = ApiAdapterFactory.getApiAdapter().getLocationMode(this);
        if (locationmode <= ApiAdapter.LOCATION_MODE_SENSORS_ONLY) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            PendingIntent pi = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            nb.setContentTitle(getResources().getString(R.string.location_provider_disabled_title));
            nb.setContentText(getResources().getString(R.string.location_provider_disabled_text));
            nb.setContentIntent(pi);
        } else {
            // GPS and Network location provider is OK. let use try again
            Intent intent = new Intent(ACTION_START_GEOFENCES,null,this, LocationMuteService.class);
            PendingIntent pi = PendingIntent.getService(this,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            nb.setContentTitle(getResources().getString(R.string.geo_not_available_retry_title));
            nb.setContentText(getResources().getString(R.string.geo_not_available_retry_text));
            nb.setContentIntent(pi);
        }
        NotificationManager nm = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(GEOFENCE_NOT_AVLIABLE_NOTIFICATION_ID,nb.build());

    }

}

