
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

package bangz.smartmute.content;

import android.util.Log;

import java.util.IllegalFormatException;

/**
 * Created by royerwang on 2014-09-23.
 */
public class LocationCondition extends Condition {

    public static final String TAG = LocationCondition.class.getSimpleName();

    public static final String strMatch =
            "(?i)\\s*location\\s*:\\s*[-+]?\\d+(\\.?\\d+)\\s*,\\s*[-+]?\\d+(\\.?\\d+)\\s*(,\\s*\\d+)?";

    private float longitude ;   //经度
    private float latitude ;    //纬度
    private int   radar;        //metter

    public LocationCondition() {
        setType(Condition.RT_LOCATION);
    }

    /**
     *
     * @param condition is location condition. string format:
     *                  latitude, longitude[, radar(meter)]
     */
    public LocationCondition(String condition) {

        //super(condition);

        setType(Condition.RT_LOCATION);
        String strparam = getParamString(condition);

        String[] value = strparam.split(",\\s*");
        if (value.length > 3 || value.length < 2) {
            throw new IllegalArgumentException("not enough parameter");
        }

        if (value[0].isEmpty() == false) {
            latitude = Float.valueOf(value[0]);
        }
        if (value[1].isEmpty() == false) {
            longitude = Float.valueOf(value[1]);
        }
        if (value.length == 3) {
            radar = Integer.valueOf(value[2]);
        }

        setConditionString(BuildConditionString());

    }

    @Override
    public boolean isValidConditionString(String strCondition) {

        return checkStringFormat(strCondition);
    }

    public static boolean checkStringFormat(String strcondition) {
        return strcondition.matches(strMatch);
    }

    @Override
    public String getParamString(String strcondition) {
        return strcondition.replaceAll("(?i)\\s*location\\s*:\\s*","");
    }

    @Override
    public String BuildConditionString() {

        StringBuilder   sb = new StringBuilder("location: ");
        sb.append(latitude).append(", ").append(longitude);
        if(radar > 0) {
            sb.append(", ").append(radar);
        }

        return sb.toString();
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public int getRadar() {
        return radar ;
    }
}
