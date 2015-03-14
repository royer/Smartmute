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

package com.bangz.smartmute.util;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import static android.os.Build.*;

/**
 * Created by royerwang on 14-12-24.
 */
public class ApiAdapter15 implements ApiAdapter {

    @Override
    public void setExactAlarm(AlarmManager am, int type, long triggerAtMillis, PendingIntent operation) {
        am.set(type, triggerAtMillis, operation);
    }

    @TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH_MR1) //15
    @Override
    public int getLocationMode(Context ctx) {

        String providers = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (TextUtils.isEmpty(providers))
            return LOCATION_MODE_OFF;
        else {

            LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null) {
                boolean bgps, bnetwork ;
                bgps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                bnetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (bgps && bnetwork) {
                    return LOCATION_MODE_HIGH_ACCURACY ;
                } else if (bgps) {
                    return LOCATION_MODE_SENSORS_ONLY ;
                } else if (bnetwork) {
                    return LOCATION_MODE_BATTERY_SAVING ;
                }
            }

            return LOCATION_MODE_OFF ;

        }
    }
}
