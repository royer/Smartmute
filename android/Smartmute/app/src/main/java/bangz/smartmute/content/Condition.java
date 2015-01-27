
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
public abstract class Condition {
    public static final String TAG = Condition.class.getSimpleName();

    public static final int RT_UNKNOWN = 0;
    public static final int RT_LOCATION = RulesColumns.RT_LOCATION;
    public static final int RT_WIFI = RulesColumns.RT_WIFI;
    public static final int RT_TIME = RulesColumns.RT_TIME;

    public static final String KEYLOCATION = "location";
    public static final String KEYWIFI = "wifi";
    public static final String KEYTIME = "time";

    private String strCondition ;

    private int type ;

    protected Condition() {
        type = RT_UNKNOWN ;
        strCondition = "";
    }

    protected Condition(String strcondition) {
        setConditionString(strcondition);
    }

    protected void setConditionString(String strcondition) {
        strCondition = strcondition ;
    }


    @Override
    public String toString() {
        return strCondition ;
    }

    public boolean isEmpty() {
        return strCondition == null || strCondition.isEmpty() ;
    }

    public int getType() {
        return type;
    }
    
    protected void setType(int t) {
        type = t ;
    }

    public abstract boolean isValidConditionString(String strcondition) ;
    public abstract String getParamString(String strcondition) ;
    public abstract String BuildConditionString();
}
