
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

package com.bangz.smartmute;

/**
 * Created by royerwang on 2014-11-19.
 */
public class Constants {

    public static final String PACKAGE_NAME = "com.bangz.smartmute";

    public static final String INTENT_EDITORNEW = PACKAGE_NAME + ".edit_or_new";

    public static final int INTENT_EDIT = 0;
    public static final int INTENT_NEW = 1;

    public static final String INTENT_LATLNG = PACKAGE_NAME + ".LATLNG";

    public static final int ONE_MINUTE_IN_MS = (60 * 1000);
    public static final int ONE_SECOND_IN_MS = 1000;

    public static final float MIN_RADIUS = 30;
    public static final float MAX_RADIUS = 300;

    private Constants() {}
}
