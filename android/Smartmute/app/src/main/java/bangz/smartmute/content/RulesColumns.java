
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

import android.media.AudioManager;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Constants for the rules table
 *
 * @author royerwang.
 */
public interface RulesColumns extends BaseColumns {

    public static final String TABLE_NAME = "rules";

    /**
     * Rules provider uri
     */
    public static final Uri CONTENT_URI = Uri.parse(
            "content://com.bangz.smartmute.provider/rules"
    );
    public static final Uri CONTENT_ID_URI_BASE = Uri.parse(
            "content://com.bangz.smartmute.provider/rules"
    );

    public static final String DEFAULT_SORT_ORDER = "_ID";

    // Rule type values
    public static final int RT_LOCATION = 1 ;
    public static final int RT_WIFI = 2;
    public static final int RT_TIME = 3;

    // Ring mode values
    public static final int RM_NORMAL = AudioManager.RINGER_MODE_NORMAL ;
    public static final int RM_SILENT = AudioManager.RINGER_MODE_SILENT;
    public static final int RM_VIBRATE = AudioManager.RINGER_MODE_VIBRATE;


    // Columns
    public static final String NAME = "name";               //rule name
    public static final String ACTIVATED = "activated"; //whether activated
    public static final String RULETYPE = "ruletype";  // main rule type
    public static final String CONDITION = "condtion" ; // the main rule
    public static final String SECONDCONDITION = "secondcondition"; // addition rules
    public static final String RINGMODE = "ringmode" ;  // which ring mode to be set
    public static final String DESCRIPTION = "description"; // rule description (only main rule
                                                            // type is location type, the address
                                                            // info fill this column

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "( "
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "   //ID
            + NAME + " STRING, " // name
            + ACTIVATED + " INTEGER, "
            + RULETYPE + " INTEGER, "
            + CONDITION +" STRING, "
            + SECONDCONDITION + " STRING, "
            + RINGMODE + " INTEGER, "
            + DESCRIPTION + " STRING "
            + ");" ;

    public static final String[] COLUMNS = { _ID,
        NAME,
        ACTIVATED,
        RULETYPE,
        CONDITION,
        SECONDCONDITION,
        RINGMODE,
        DESCRIPTION};
}
