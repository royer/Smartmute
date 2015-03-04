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
import android.os.Build;

/**
 * Created by royerwang on 14-12-24.
 */
public class ApiAdapter19 extends ApiAdapter15 {

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void setExactAlarm(AlarmManager am, int type, long triggerAtMillis, PendingIntent operation) {
        am.setExact(type, triggerAtMillis, operation);
    }
}
