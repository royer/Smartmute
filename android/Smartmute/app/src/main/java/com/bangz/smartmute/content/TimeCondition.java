
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

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bangz.smartmute.util.MyTimeUtils;

/**
 * Created by royerwang on 2014-09-23.
 */
public class TimeCondition extends Condition {

    private static final String TAG = TimeCondition.class.getSimpleName();

    public static final String strMatch =
            "(?i)\\s*time\\s*:\\s*((?:0?[0-9]|1[0-9]|2[0-3]):[0-5][0-9])\\s*,\\s*" +
                    "((?:0?[0-9]|1[0-9]|2[0-3]):[0-5][0-9])\\s*,\\s*([01]{7})";
    // which days string combined by 7 0/1 character. first character means whether sunday is enable,
    // left character means Monday to Saturday.


    private Time begin;
    private Time end ;
    private int whichdays;

    public static final int IDX_SUNDAY = 0;
    public static final int IDX_MONDAY = 1;
    public static final int IDX_TUESDAY = 2;
    public static final int IDX_WEDNESDAY = 3;
    public static final int IDX_THURSDAY = 4;
    public static final int IDX_FRIDAY = 5;
    public static final int IDX_SATURDAY = 6;

    public static final int ALLDAYSSET = 0x7F;


    private static final int[] MAP_CALENDARDAYOFWEEK = {
        Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
    };

    public static int getCalendarDayofWeekfromIdxDay(int idxday) {
        if (idxday < IDX_SUNDAY || idxday > IDX_SATURDAY)
            throw new ArrayIndexOutOfBoundsException();
        return MAP_CALENDARDAYOFWEEK[idxday];
    }

    public static int getIdxDayfromCalendarDayOfWeek(int calendarday) {

        int i ;
        for ( i = IDX_SUNDAY;i<=IDX_SATURDAY; i++)
            if (MAP_CALENDARDAYOFWEEK[i] == calendarday)
                return i;

        return -1;
    }

    public TimeCondition() {
        setType(Condition.RT_TIME);
    }

    public TimeCondition(String strcondition) {
        setType(Condition.RT_TIME);

        Pattern p = Pattern.compile(strMatch);
        Matcher m = p.matcher(strcondition);

        if (m.find() == true) {

            try {
                String strbegin = m.group(1);
                String strend = m.group(2);
                String sw = m.group(3);
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                begin = new Time(formatter.parse(strbegin).getTime());
                end = new Time(formatter.parse(strend).getTime());
                whichdays = parseWhichdayString(sw);

                setConditionString(BuildConditionString());

            } catch (ParseException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("incorrect time format in time condition string.");
            }

        } else {
            throw new IllegalArgumentException("incorrect time condition string.");
        }
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
        return strcondition.replaceAll("(?i)\\s*time:\\s*","");
    }

    @Override
    public String BuildConditionString() {

        String strbegin = begin.toString().substring(0,begin.toString().lastIndexOf(':'));
        String strend = end.toString().substring(0,end.toString().lastIndexOf(':'));

        return String.format("time: %s, %s, %s",strbegin, strend,whichdaysToString(whichdays));
    }

    public static int parseWhichdayString(String strwhichdays) {

        int i ;
        int wd = 0;
        for (i = 0; i < strwhichdays.length(); i++) {

            if (strwhichdays.charAt(i) != '0') {
                wd |= (1 << i) ;
            }
        }

        return wd ;
    }

    public static String whichdaysToString(int w) {
        StringBuilder s = new StringBuilder();

        int i;
        for (i = 0; i < 7; i++) {
            s.append((w & (1 << i)) != 0 ? '1' : '0') ;
        }

        return s.toString() ;
    }

    public boolean isEnableOnThisDay(int whichday) {

        return (whichdays & (1 << whichday)) != 0;
    }

    public boolean isEnableToday(final Calendar today) {
        int thedayofweek = today.get(Calendar.DAY_OF_WEEK);

        int whichday = getIdxDayfromCalendarDayOfWeek(thedayofweek);

        return isEnableOnThisDay(whichday);
    }


    public Calendar getNextMuteTime(final Calendar currenttime) {

        Calendar startmutetime = MyTimeUtils.convertSqlTimeToCalendar(begin);

        int hour = startmutetime.get(Calendar.HOUR_OF_DAY);
        int minute = startmutetime.get(Calendar.MINUTE);

        Calendar creturn = (Calendar)currenttime.clone();
        creturn.set(Calendar.HOUR_OF_DAY, hour);
        creturn.set(Calendar.MINUTE, minute);
        creturn.set(Calendar.SECOND, 0);

        if (isEnableToday(currenttime) && currenttime.before(creturn)) {

            return creturn ;
        }

        creturn.add(Calendar.DATE, 1);
        int idx_search = getIdxDayfromCalendarDayOfWeek(creturn.get(Calendar.DAY_OF_WEEK)) ;
        int searcheddays = 0;
        while(searcheddays < 7) {
            if (isEnableOnThisDay(idx_search)) {
                return creturn ;
            }
            creturn.add(Calendar.DATE, 1);
            idx_search = getIdxDayfromCalendarDayOfWeek(creturn.get(Calendar.DAY_OF_WEEK));
            searcheddays++;
        }

        return null ;



    }

    public Time getBegin() {
        return begin ;
    }
    public Time getEnd() {
        return end;
    }
    public int getWhichdays() {
        return whichdays;
    }

    public boolean isAllDaySet() {
        return whichdays == ALLDAYSSET ;
    }
}
