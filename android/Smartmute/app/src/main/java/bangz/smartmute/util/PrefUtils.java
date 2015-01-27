/*
 * Copyright (c) 2014 Royer Wang. All rights reserved.
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

package bangz.smartmute.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

/**
 * Created by royerwang on 14-12-12.
 */
public class PrefUtils {

    private static final String TAG = PrefUtils.class.getSimpleName();



    /**
     * long preference. When 0 indicates mute status is cleaned or nobody set mute .
     * -1 indicates current mute is user set. positive value is Rules._ID, indicates which rule
     * mute volume.
     */
    public static final String PREF_LAST_SETMUTE_ID = "pref_last_setmute_id";
    public static final long PREF_LAST_SETMUTE_ID_USER = -1;
    public static final long PREF_LAST_SETMUTE_ID_CLEAN = 0;

    public static final String PREF_LAST_WIFI_SSID = "pref_last_wifi_ssid";
    public static final String PREF_ENABLE_SMART_MUTE = "pref_enable_smart_mute";

    public static void enableSmartMute(final Context context, boolean benable) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        sp.edit().putBoolean(PREF_ENABLE_SMART_MUTE, benable).commit();
    }
    public static boolean isEnableSmartMute(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ENABLE_SMART_MUTE,true);
    }


    public static void rememberWhoMuted(final Context context, long recordid) {
       SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context) ;
       sp.edit().putLong(PREF_LAST_SETMUTE_ID, recordid).commit();
    }

    public static void rememberLastAlarmAction(final Context context, final long recordid,
                                           final String action) {

        String key = String.valueOf(recordid);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(key, action).commit();

    }

    public static void cleanLastAlarmAction(final Context context, final long recordid) {
        String key = String.valueOf(recordid);

        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(key).commit();
    }

    public static String getLastAlarmAction(final Context context, final long recordid, String defValue) {
        String key = String.valueOf(recordid) ;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(key, defValue);
    }


    public static long getLastSetMuteId(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SETMUTE_ID,PREF_LAST_SETMUTE_ID_CLEAN);
    }

    public static void cleanLastMuteId(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_LAST_SETMUTE_ID, PREF_LAST_SETMUTE_ID_CLEAN).commit();
    }

    public static void rememberWifi(final Context context, final String ssid) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_LAST_WIFI_SSID, ssid).commit();
    }

    public static String getLastWifiSsid(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_LAST_WIFI_SSID,"");
    }
}
