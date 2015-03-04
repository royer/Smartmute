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

package com.bangz.smartmute.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by wangruoyu on 15-02-23.
 */
public class ReceUtils {

    private static final String TAG = "ReceUtils" ;

    public static void enableReceiver(final Context context, ComponentName receiver
            , boolean bEnable) {

        PackageManager pm = context.getPackageManager() ;

        int state = bEnable?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        LogUtils.LOGD(TAG,(bEnable?"Enable":"Disable") + " Broadcast for location provider changed.");

        pm.setComponentEnabledSetting(receiver, state, PackageManager.DONT_KILL_APP);

    }

    public static void enableReceiver( Context context, String cls
            , boolean bEnabled) {

        ComponentName receiver = new ComponentName(context, cls) ;
        enableReceiver(context, receiver, bEnabled);
    }

    public static void enableReceiver( Context context, Class<?> cls, boolean bEnabled) {
        ComponentName receiver = new ComponentName(context, cls);
        enableReceiver(context, receiver, bEnabled);

    }



}
