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

package bangz.smartmute.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;

import java.util.Calendar;

import bangz.smartmute.Config;
import bangz.smartmute.Constants;
import bangz.smartmute.content.ConditionFactory;
import bangz.smartmute.content.RulesColumns;
import bangz.smartmute.content.TimeCondition;
import bangz.smartmute.util.ApiAdapterFactory;
import bangz.smartmute.util.LogUtils;
import bangz.smartmute.util.PrefUtils;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class TimeRuleAlarmService extends IntentService {

    private static final String TAG = TimeRuleAlarmService.class.getSimpleName();


    /**  set ring volume to silence */
    private static final String ACTION_SILENCE =
            Constants.PACKAGE_NAME + ".services.action.silence";

    /** restore silence to normal */
    private static final String ACTION_RESTORE_VOLUME =
            Constants.PACKAGE_NAME + ".services.action.restorevol";

    /** set alarm for all activated time rule . */
    public static final String ACTION_SET_ALARM_FOR_ALL_TIMERULE =
            Constants.PACKAGE_NAME + ".services.action.set_alarm_for_all_timerule";

    /**  set alarm for one record base time */
    public static final String ACTION_SCHEDULE_ALARM =
            Constants.PACKAGE_NAME + ".services.action.schedule.alarm";
    public static final String ACTION_CANCEL_ALARM =
            Constants.PACKAGE_NAME + ".services.acition.calcel.alarm";




    /**
     * Starts this service to perform arrange alarm for one time rule . If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startScheduleAlarm(Context context, Uri uri) {

        Intent intent = new Intent(ACTION_SCHEDULE_ALARM, uri, context, TimeRuleAlarmService.class);
        LogUtils.LOGD(TAG, "Start AlarmService with ACTION_SCHEDULE_ALARM for Id: " + ContentUris.parseId(uri));
        context.startService(intent);

    }

    public static void cancelScheduledAlarm(Context context, Uri uri) {

        Intent intent = new Intent(ACTION_CANCEL_ALARM, uri, context, TimeRuleAlarmService.class);
        LogUtils.LOGD(TAG, "Start to cancel a alarm for Id: " + ContentUris.parseId(uri));
        context.startService(intent);
    }

    public static void startSetAlarmForAll(Context context) {
        Intent intent = new Intent(ACTION_SET_ALARM_FOR_ALL_TIMERULE,null, context, TimeRuleAlarmService.class);
        LogUtils.LOGD(TAG, "start to set all alarm that activited");
        context.startService(intent);
    }

    public TimeRuleAlarmService() {
        super("TimeRuleAlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SILENCE.equals(action)) {
                handleActionSilence(intent);
            } else if (ACTION_RESTORE_VOLUME.equals(action)) {
                handleActionRestoreVol(intent);
            } else if (ACTION_SET_ALARM_FOR_ALL_TIMERULE.equals(action)) {
                handleActionSetAlarmForAll(intent);
            } else if (ACTION_SCHEDULE_ALARM.equals(action)) {
                long recordid = ContentUris.parseId(intent.getData());
                handleActionScheduleAlarm(recordid);
            } else if (ACTION_CANCEL_ALARM.equals(action)) {
                long recordid = ContentUris.parseId(intent.getData());
                handleActionCancelAlarm(recordid);
            }
        }
    }

    private void handleActionSilence(Intent intent) {

        LogUtils.LOGD(TAG, "Handle Action Silence");

        Uri uri = intent.getData();

        long _id = ContentUris.parseId(uri);
        if (_id == -1) {
            LogUtils.LOGD(TAG, "Action Silence got recordid = -1. do noting.");
            return ;
        }

        final ContentResolver cr = getContentResolver();
        final Cursor cursor = cr.query(uri,RulesColumns.COLUMNS,null,null,null);
        if (cursor == null) {
            LogUtils.LOGD(TAG, "Handle Action Silence: id: " + _id + " reocrd doesn't exist! do nothing.");
            return ;
        }

        if (cursor.moveToFirst() == false) {
            LogUtils.LOGD(TAG, "Handle Acton Silence: Cursor.moveToFirst() return false.");
            return;
        }

        final int activated = cursor.getInt(cursor.getColumnIndex(RulesColumns.ACTIVATED));
        final String condition = cursor.getString(cursor.getColumnIndex(RulesColumns.CONDITION));
        if (activated == 0) {
            LogUtils.LOGD(TAG, condition + " was deactivated, do nothing.");
            return ;
        }

        final int ringmode = cursor.getInt(cursor.getColumnIndex(RulesColumns.RINGMODE));


        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        int currringmode = am.getRingerMode() ;
        LogUtils.LOGD(TAG, "current ringer mode is: " + currringmode);
        if (currringmode == AudioManager.RINGER_MODE_NORMAL) {
            if (ringmode == RulesColumns.RM_SILENT) {
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                LogUtils.LOGD(TAG, "id: " + _id + " set ringer mode to silent");
            } else if (ringmode == RulesColumns.RM_VIBRATE) {
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                LogUtils.LOGD(TAG, "id: " + _id + " set ringer mode to vibrate");
            }
            PrefUtils.rememberWhoMuted(this, _id);
            //TODO notify user by notification
            if (Config.TEST_BUILD == true) {
                String msg = "set ringer mode to ";
                if (ringmode == RulesColumns.RM_SILENT) {
                    msg += "silent";
                } else if (ringmode == RulesColumns.RM_VIBRATE) {
                    msg += "vibrate";
                }
                //Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
            }
        }

        // get restore time then set alarm
        TimeCondition timeCondition = (TimeCondition) ConditionFactory.CreateCondition(condition);
        Calendar calendarRestore = Calendar.getInstance();
        Calendar calendarNow = Calendar.getInstance();
        Calendar calendartemp = (Calendar) calendarRestore.clone();
        calendartemp.setTime(timeCondition.getEnd());
        calendarRestore.set(Calendar.HOUR_OF_DAY,calendartemp.get(Calendar.HOUR_OF_DAY));
        calendarRestore.set(Calendar.MINUTE, calendartemp.get(Calendar.MINUTE));
        calendarRestore.set(Calendar.SECOND, 0);
        if (calendarRestore.before(calendarNow)) {
            calendarRestore.add(Calendar.DATE, 1);
        }

        if (Config.TEST_BUILD) {
            calendarRestore = calendarNow ;
            calendarRestore.add(Calendar.SECOND, 10) ;
        }

        Intent restoreVolIntent = new Intent(ACTION_RESTORE_VOLUME,uri,this, TimeRuleAlarmService.class);


        PendingIntent pi = PendingIntent.getService(this,0, restoreVolIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        LogUtils.LOGD(TAG, "Scheduling RTC_WAKEUP alarm at " + calendarRestore.getTime().toString() +
                " for record id: " + _id + " to restore volume.");
        ApiAdapterFactory.getApiAdapter().setExactAlarm(alarmManager,
                AlarmManager.RTC_WAKEUP,calendarRestore.getTimeInMillis(),pi);

        PrefUtils.rememberLastAlarmAction(this,_id, ACTION_RESTORE_VOLUME);

    }

    private void handleActionSetAlarmForAll(Intent intent) {

        final ContentResolver cr = getContentResolver();
        final String selection = RulesColumns.RULETYPE + " = " + RulesColumns.RT_TIME +
                " AND " + RulesColumns.ACTIVATED + " = 1" ;
        final Cursor c = cr.query(RulesColumns.CONTENT_URI, RulesColumns.COLUMNS,selection,null,null);

        if (c == null) {
            LogUtils.LOGD(TAG,"handle set alarm for all, Cursor return null");
            return ;
        }

        if (c.getCount() == 0) {
            LogUtils.LOGD(TAG,"Handle set alarm for all, but no rule to be set.");
            return ;
        }

        while(c.moveToNext()) {
            final String condition = c.getString(c.getColumnIndex(RulesColumns.CONDITION));
            long id = c.getLong(c.getColumnIndex(RulesColumns._ID));

            setAlarmForMute(id, condition);
        }
    }

    private void handleActionRestoreVol(Intent intent) {
        long lastsetMuteId = PrefUtils.getLastSetMuteId(this);

        Uri uri = intent.getData();

        long thisId = ContentUris.parseId(uri);
        if (thisId != lastsetMuteId) {
            String str = String.format("Handle Restore Volumn, but id (%d) != last set Mute id(%d)!",
                    thisId, lastsetMuteId);
            LogUtils.LOGD(TAG, str);
        } else {

            // restore volume
            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

            //clearn sharepref
            PrefUtils.cleanLastMuteId(this);
        }

        PrefUtils.cleanLastAlarmAction(this, thisId);

        // set alarm for next mute time
        handleActionScheduleAlarm(thisId);

    }

    private void handleActionScheduleAlarm(long recordid) {

        Uri uri = ContentUris.appendId(RulesColumns.CONTENT_URI.buildUpon(), recordid).build();
        ContentResolver cc = getContentResolver();
        final Cursor cursor = cc.query(uri, RulesColumns.COLUMNS,null,null,null);
        if (cursor == null || (cursor != null && cursor.getCount() == 0)) {
            LogUtils.LOGD(TAG, "query id: " + recordid + " but does not exist when handle acion restore vol");
            return ;
        }

        cursor.moveToFirst();
        String strcondition = cursor.getString(cursor.getColumnIndex(RulesColumns.CONDITION));

        setAlarmForMute(recordid, strcondition);

    }


    private void setAlarmForMute(final long id, final String strcondition ) {
        TimeCondition timecondition = new TimeCondition(strcondition);

        Uri uri = ContentUris.withAppendedId(RulesColumns.CONTENT_URI,id);

        Calendar currenttime = Calendar.getInstance();

        Calendar nextmutetime = timecondition.getNextMuteTime(currenttime);

        if (nextmutetime != null) {

            if (Config.TEST_BUILD) {

                nextmutetime = currenttime ;
                nextmutetime.add(Calendar.SECOND, 10);
            }

            Intent silentIntent = new Intent(
                    ACTION_SILENCE,
                    uri,
                    this,
                    TimeRuleAlarmService.class
            );
            long scheduletime = nextmutetime.getTimeInMillis();
            PendingIntent pi = PendingIntent.getService(this,
                    0,
                    silentIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            final AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            LogUtils.LOGD(TAG, "Scheduling RTC_WAKEUP Alarm at  " + nextmutetime.getTime().toString()
                    + " for record id: "+ id + " to silent or vibrate.");
            ApiAdapterFactory.getApiAdapter().setExactAlarm(am, AlarmManager.RTC_WAKEUP,scheduletime, pi);
            PrefUtils.rememberLastAlarmAction(this,id,ACTION_SILENCE);
        }

    }

    private void handleActionCancelAlarm(final long recordid) {
        LogUtils.LOGD(TAG, "Handle cancel alarm for recordid: " + recordid);

        Uri uri = ContentUris.withAppendedId(RulesColumns.CONTENT_URI, recordid);

        String lastaction = PrefUtils.getLastAlarmAction(this, recordid,"NO");
        if (lastaction.equals("NO")) {
            LogUtils.LOGD(TAG,"Handle Cancel Alarm,Can't found last alarm action of recordid: " + recordid);
            return ;
        }

        Intent intent = new Intent(lastaction,uri,this,TimeRuleAlarmService.class);

        PendingIntent pi = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        final AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        PrefUtils.cleanLastAlarmAction(this, recordid);

        if (ACTION_RESTORE_VOLUME.equals(lastaction) &&
                recordid == PrefUtils.getLastSetMuteId(this)) {

            final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        }

        if (recordid == PrefUtils.getLastSetMuteId(this)) {
            PrefUtils.cleanLastMuteId(this);
        }
    }


//    /**
//     * Handle action Foo in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleActionFoo(String param1, String param2) {
//        // TODO: Handle action Foo
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    /**
//     * Handle action Baz in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleActionBaz(String param1, String param2) {
//        // TODO: Handle action Baz
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
}
