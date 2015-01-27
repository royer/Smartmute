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

package bangz.smartmute.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;

import bangz.smartmute.Constants;
import bangz.smartmute.content.RulesColumns;
import bangz.smartmute.content.WifiCondition;
import bangz.smartmute.util.LogUtils;
import bangz.smartmute.util.PrefUtils;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class WifiMuteService extends IntentService {

    private static final String TAG = WifiMuteService.class.getSimpleName();

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "bangz.smartmute.services.action.FOO";
    private static final String ACTION_BAZ = "bangz.smartmute.services.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "bangz.smartmute.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "bangz.smartmute.services.extra.PARAM2";

    private static final String ACTION_CONNECTED = Constants.PACKAGE_NAME +
            ".services.wifi.connected";
    private static final String ACTION_DISCONNECTED = Constants.PACKAGE_NAME +
            ".services.wifi.disconnected";

    private static final String EXTRA_SSID = Constants.PACKAGE_NAME +
            ".services.wifi.extra.ssid";


    /**
     * Starts service to process when wifi connected.
     * @param context
     * @param ssid
     */
    public static void wifiConnected(Context context, String ssid) {
        Intent intent = new Intent(context, WifiMuteService.class);
        intent.setAction(ACTION_CONNECTED);
        intent.putExtra(EXTRA_SSID, ssid);
        context.startService(intent);
    }

    /**
     * Starts service to process when wifi disconnected.
     * @param context
     */
    public static void wifiDisconnected(Context context) {
        Intent intent = new Intent(context, WifiMuteService.class);
        intent.setAction(ACTION_DISCONNECTED);
        context.startService(intent);
    }


    public WifiMuteService() {
        super("WifiMuteService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CONNECTED.equals(action)) {
                handleWifiConnected(intent.getStringExtra(EXTRA_SSID));
            } else if (ACTION_DISCONNECTED.equals(action)) {
                handleWifiDisconnected();
            }
        }
    }

    private void handleWifiConnected(String ssid) {
        String lastwifi = PrefUtils.getLastWifiSsid(this);

        // remove quotation
        ssid = ssid.replaceAll("[\\^\\\"\\\"\\$]","");

        if (ssid.equals(lastwifi))
            return ;

        if (PrefUtils.isEnableSmartMute(this) == false) {
            LogUtils.LOGD(TAG, "Smart Mute is disabled, do nothing.");
            return ;
        }

        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            LogUtils.LOGD(TAG, "!!!! AudioManager = null !!!!");
            return ;
        }
        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            // Ringer mode is already in silent or vibrate, do not change anything.
            LogUtils.LOGI(TAG,"Ringer mode is already in silent or vibrate. do not change anything. ");
            return ;
        }

        String condition = WifiCondition.buildWifiConditionString(ssid);

        String selection = RulesColumns.RULETYPE + " = " + String.valueOf(RulesColumns.RT_WIFI) +
                " AND " + RulesColumns.ACTIVATED + " = 1 AND " +
                RulesColumns.CONDITION + " = ? "  ;

        String[] argument = new String[] {
                condition
        };
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(RulesColumns.CONTENT_URI,RulesColumns.COLUMNS,
                selection,argument,null);
        if (cursor == null) {
            LogUtils.LOGD(TAG, "query return cursor = null for ssid:" + ssid);
            return ;
        }

        if (cursor.getCount() == 0) {
            LogUtils.LOGD(TAG,"Not find rule for this wifi: " + ssid);
            return ;
        }

        while(cursor.moveToNext()) {
            int ringermode = cursor.getInt(cursor.getColumnIndex(RulesColumns.RINGMODE));
            audioManager.setRingerMode(ringermode);
            long id = cursor.getLong(cursor.getColumnIndex(RulesColumns._ID));
            PrefUtils.rememberWhoMuted(this, id);
            PrefUtils.rememberWifi(this, ssid);
        }

    }

    private void handleWifiDisconnected() {

        String lastwifi = PrefUtils.getLastWifiSsid(this);
        if (lastwifi != null && lastwifi.isEmpty() == false) {
            AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            PrefUtils.cleanLastMuteId(this);

        }

        PrefUtils.rememberWifi(this, "");

    }

}
