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

import android.app.AlarmManager;
import android.app.PendingIntent;

/**
 * Created by royerwang on 14-12-24.
 */
public interface ApiAdapter {

    /**
     * Wrap {@link AlarmManager#set} method, Because after API19, must use
     * {@link AlarmManager#setExact} method to set exact alarm, do not use {@link AlarmManager#set}
     * method.
     * @param am {@link android.app.AlarmManager}
     * @param type
     * @param triggerAtMillis
     * @param operation
     */
    public void setExactAlarm(final AlarmManager am, int type, long triggerAtMillis, PendingIntent operation);
}
