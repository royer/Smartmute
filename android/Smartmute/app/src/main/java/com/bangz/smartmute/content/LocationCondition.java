
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

import com.google.android.gms.location.Geofence;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.bangz.smartmute.util.LogUtils;

/**
 * Created by royerwang on 2014-09-23.
 */
public class LocationCondition extends Condition {

    public static final String TAG = LocationCondition.class.getSimpleName();


    public static final float DEF_RADIUS = 100.0f;

    public static final int DEF_LOITERINGDELAY_MS = 1 * 60 * 1000;

    // we always monitor EXIT transition, user just care about either enter or dwell
    public static final int  DEF_TRANSITIONTYPE = Geofence.GEOFENCE_TRANSITION_ENTER ;


    // The best effort notification responsiveness of the geo fence
    public static final int  DEF_NOTIFYDELAY_MS = 0 ;



    public static class TriggerCondition {

        private int transitionType ;
        private int loiteringDelay ;
        private int notificationDelay ;

        public TriggerCondition(int transtype, int loitering, int notifydelay) {
            transitionType = transtype ;
            loiteringDelay = loitering ;
            notificationDelay  = notifydelay ;
        }

        public TriggerCondition(int transtype) {
            this(transtype, DEF_LOITERINGDELAY_MS, DEF_NOTIFYDELAY_MS);
        }

        public TriggerCondition() {
            this(DEF_TRANSITIONTYPE, DEF_LOITERINGDELAY_MS, DEF_NOTIFYDELAY_MS);
        }

        public int getTransitionType() {
            return transitionType ;
        }
        public int getLoiteringDelay() {
            return loiteringDelay ;
        }
        public int getNotificationDelay() {
            return notificationDelay ;
        }
        public void setTransitionType(final int transtype) {
            transitionType = transtype;
        }
        public void setLoiteringDelay(final int l) {
            loiteringDelay = l;
        }
        public void setNotificationDelay(final int notifydelay) {
            notificationDelay = notifydelay ;
        }
    }

    //public static final String strMatch =
    //        "(?i)\\s*location\\s*:\\s*[-+]?\\d+(\\.?\\d+)\\s*,\\s*[-+]?\\d+(\\.?\\d+)\\s*(,\\s*\\d+)?";

    // The regular string to match, after location: is a JSON string. the whole string example:
    // location:{"Lat":45.3344, "Lng":-123.56,"radius":100.0,"transitiontype":1,"loitering=60000,
    // "notifydelay":60000}

    private static final String strMatch = "(?i)\\s*location\\s*:\\s*\\{.*\\}";
    private static final String KEY_LATITUDE = "Lat";
    private static final String KEY_LONGITUDE = "Lng";
    private static final String KEY_RADIUS = "radius";
    private static final String KEY_LOITERINGDELAY = "loitering";
    private static final String KEY_TRANSITIONTYPE = "transitiontype";
    private static final String KEY_NOTIFYDELAY = "notifydelay";

    private double longitude ;   //经度
    private double latitude ;    //纬度
    private float  radius;       //metter

    private TriggerCondition    triggerCondition ;

    public LocationCondition() {
        setType(Condition.RT_LOCATION);
        triggerCondition = new TriggerCondition();
    }

    /**
     *
     * @param condition is a string follow this format:
     *        location:JSON string
     */
    public LocationCondition(String condition) {

        //super(condition);

        setType(Condition.RT_LOCATION);
        String strparam = getParamString(condition);

        try {
            JSONObject allJson = (JSONObject) new JSONTokener(strparam).nextValue();

            latitude = allJson.getDouble(KEY_LATITUDE);
            longitude = allJson.getDouble(KEY_LONGITUDE);
            radius = (float)allJson.getDouble(KEY_RADIUS);

            int tt = allJson.getInt(KEY_TRANSITIONTYPE);
            int loitering = allJson.getInt(KEY_LOITERINGDELAY);
            int notifydelay = allJson.getInt(KEY_NOTIFYDELAY);
            triggerCondition = new TriggerCondition(tt,loitering,notifydelay);


            setConditionString(BuildConditionString());

        } catch (JSONException e) {
              LogUtils.LOGE(TAG,e.getStackTrace().toString());
        }
    }

    public LocationCondition(final double lng, final double lat, final float r,
                             final int transitiontype, final int loitering, final int notifydelay) {
        setType(Condition.RT_LOCATION);
        longitude = lng;
        latitude = lat;
        radius = r;
        triggerCondition = new TriggerCondition(transitiontype,loitering,notifydelay);

        setConditionString(BuildConditionString());
    }

    public LocationCondition(final double longitude, final double latitude, final float radius) {
        this(longitude, latitude, radius,
                DEF_TRANSITIONTYPE, DEF_LOITERINGDELAY_MS, DEF_NOTIFYDELAY_MS);
    }

    public LocationCondition(final double lng, final double lat) {
        this(lng, lat, DEF_RADIUS);
    }

    public LocationCondition(final double lng, final double lat, final float r,
                             final int transtype) {
        this(lng, lat, r, transtype, DEF_LOITERINGDELAY_MS, DEF_NOTIFYDELAY_MS);
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

        JSONObject json = new JSONObject();
        try {
            json
                    .put(KEY_LATITUDE, latitude)
                    .put(KEY_LONGITUDE, longitude)
                    .put(KEY_RADIUS,radius)
                    .put(KEY_TRANSITIONTYPE,triggerCondition.getTransitionType())
                    .put(KEY_LOITERINGDELAY,triggerCondition.getLoiteringDelay())
                    .put(KEY_NOTIFYDELAY,triggerCondition.getNotificationDelay());


        } catch(JSONException e) {
            LogUtils.LOGE(TAG,e.toString());
        }
        sb.append(json.toString());

        return sb.toString();
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public float getRadius() {
        return radius ;
    }

    public TriggerCondition getTriggerCondition() {
        return triggerCondition ;
    }
}
