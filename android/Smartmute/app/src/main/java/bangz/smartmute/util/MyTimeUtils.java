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

import java.sql.Time;
import java.util.Calendar;
import java.util.GregorianCalendar;

import bangz.smartmute.BuildConfig;

/**
 * Created by royerwang on 14-12-18.
 */
public class MyTimeUtils {

    private static final long sAppLoadTime = System.currentTimeMillis();

    public static long getCurrentTime(final Context context) {
        if (BuildConfig.DEBUG) {
            return context.getSharedPreferences("mock_data", Context.MODE_PRIVATE)
                    .getLong("mock_current_time",System.currentTimeMillis())
                    +System.currentTimeMillis() - sAppLoadTime ;
        } else {
            return System.currentTimeMillis();
        }
    }

    public static Calendar convertSqlTimeToCalendar(final Time t) {
        Calendar c = new GregorianCalendar();

        c.setTimeInMillis(t.getTime());

        return c ;
    }


}
