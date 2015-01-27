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

package bangz.smartmute.test;

import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

import java.sql.Time;
import java.util.Calendar;

import bangz.smartmute.util.MyTimeUtils;

/**
 * Created by royerwang on 14-12-18.
 */
public class MyTimeUtilsTest extends TestCase {

    @SmallTest
    public void testConvertSqlTimeToCalendar() {

        Time time = Time.valueOf("11:23:15");

        Calendar c = MyTimeUtils.convertSqlTimeToCalendar(time);
        assertEquals(11,c.get(Calendar.HOUR_OF_DAY));
        assertEquals(23, c.get(Calendar.MINUTE));

    }
}
