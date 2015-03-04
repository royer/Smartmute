
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

package com.bangz.smartmute.content;

import android.util.Log;

/**
 * Created by royerwang on 2014-09-23.
 */
public class ConditionFactory {

    public static final String TAG=ConditionFactory.class.getSimpleName();

    public static Condition CreateCondition(String strrule) {

        Condition condition = null;

        strrule = strrule.replaceAll("[\\s\\t\\n]+"," ").trim();
        String strkey = "";

        try {
            strkey = strrule.substring(0, strrule.indexOf(':')).trim();

            if (strkey.compareToIgnoreCase(Condition.KEYLOCATION) == 0) {
                condition = new LocationCondition(strrule);
            } else if (strkey.compareToIgnoreCase(Condition.KEYWIFI) == 0) {
                condition = new WifiCondition(strrule);
            } else if (strkey.compareToIgnoreCase(Condition.KEYTIME) == 0) {
                condition = new TimeCondition(strrule);
            } else {
                return null ;
            }

        }catch (Exception e) {
            Log.e(TAG, e.toString());
            condition = null ;
        }


        return condition;
    }
}
