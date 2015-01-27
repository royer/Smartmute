
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

/**
 * Created by royerwang on 2014-09-23.
 */
public class WifiCondition extends Condition {

    public static final String TAG = WifiCondition.class.getSimpleName();

    public static final String strMatch = "(?i)\\s*wifi\\s*:\\s*\\S+\\s*";

    private String SSID ;

    public WifiCondition() {
        setType(Condition.RT_WIFI);
    }

    public WifiCondition(String strcondition) {

        setType(Condition.RT_WIFI);

        SSID = getParamString(strcondition);

        setConditionString(BuildConditionString());

    }

    public static String buildWifiConditionString(final String ssid) {
        return "wifi: " + ssid;
    }

    @Override
    public boolean isValidConditionString(String strcondition) {
        return checkStringFormat(strcondition);
    }

    public static boolean checkStringFormat(String strcondition) {
        return strcondition.matches(strMatch);
    }
    @Override
    public String getParamString(String strcondition) {
        return strcondition.replaceAll("(?i)\\s*wifi\\s*:\\s*","").trim();
    }

    @Override
    public String BuildConditionString() {
        return buildWifiConditionString(SSID);
    }

    public String getSSID() {
        return SSID ;
    }

    public void setSSID(String ssid) {
        SSID = ssid ;
    }
}
