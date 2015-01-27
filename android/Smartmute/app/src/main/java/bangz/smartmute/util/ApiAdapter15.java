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

import android.app.AlarmManager;
import android.app.PendingIntent;

/**
 * Created by royerwang on 14-12-24.
 */
public class ApiAdapter15 implements ApiAdapter {

    @Override
    public void setExactAlarm(AlarmManager am, int type, long triggerAtMillis, PendingIntent operation) {
        am.set(type, triggerAtMillis, operation);
    }
}
