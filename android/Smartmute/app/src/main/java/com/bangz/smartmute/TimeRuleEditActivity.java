
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

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.sql.Time;

import com.bangz.smartmute.R;

import com.bangz.smartmute.content.RulesColumns;
import com.bangz.smartmute.content.TimeCondition;
import com.bangz.smartmute.services.TimeRuleAlarmService;


public class TimeRuleEditActivity extends RuleEditActivity {

    private static final String TAG=TimeRuleEditActivity.class.getSimpleName();

    // Button for weekdays

    private String strName ;
    private String strDescription;

    private String strStartTime;
    private String strEndTime;
    private int    iWhichDays;

    private boolean bActivited ;
    private int     mRingMode ;



    private EditText mEditName ;
    private EditText mEditDescription;
    private EditText mEditStartTime ;
    private EditText mEditEndTime;
    private Switch   mSwitchActivited;
    private RadioGroup mViewRingMode ;
    private ToggleButton[] mButtonDays = new ToggleButton[7];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timerule_edit);

        mEditName = (EditText)findViewById(R.id.TimeRuleName);
        mEditDescription = (EditText)findViewById(R.id.TimeRuleDescription);
        mEditStartTime = (EditText)findViewById(R.id.editStartTime);
        mEditEndTime = (EditText)findViewById(R.id.editEndTime);
        mSwitchActivited = (Switch)findViewById(R.id.Activited);
        mViewRingMode = (RadioGroup)findViewById(R.id.ringmode);

        mButtonDays[0] = (ToggleButton)findViewById(R.id.Sunday);
        mButtonDays[1] = (ToggleButton)findViewById(R.id.Monday);
        mButtonDays[2] = (ToggleButton)findViewById(R.id.Tuesday);
        mButtonDays[3] = (ToggleButton)findViewById(R.id.Wednesday);
        mButtonDays[4] = (ToggleButton)findViewById(R.id.Thursday);
        mButtonDays[5] = (ToggleButton)findViewById(R.id.Friday);
        mButtonDays[6] = (ToggleButton)findViewById(R.id.Saturday);


        if (savedInstanceState == null) {
            strName = "";
            strDescription = "";
            strStartTime = "";
            strEndTime = "";
            iWhichDays = 0;

            bActivited = false;
            mRingMode = RulesColumns.RM_NORMAL ;

            mSwitchActivited.setChecked(true);
            mViewRingMode.check(R.id.vibrate);
        }

    }


    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.time_rule, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private int getRingMode() {
        int idringmode = mViewRingMode.getCheckedRadioButtonId() ;
        if (idringmode == R.id.silence)
            return RulesColumns.RM_SILENT ;
        else if (idringmode == R.id.vibrate)
            return RulesColumns.RM_VIBRATE ;
        else
            return RulesColumns.RM_NORMAL ;
    }

    private void setRingMode(int ringmode) {
        if (ringmode == RulesColumns.RM_SILENT)
            mViewRingMode.check(R.id.silence);
        else if (ringmode == RulesColumns.RM_VIBRATE)
            mViewRingMode.check(R.id.vibrate);
    }

    private int getWhichDays() {
        int whichdays = 0;

        CheckBox checkbox = (CheckBox)findViewById(R.id.chkAllDays);
        if (checkbox.isChecked()) {
            whichdays = TimeCondition.ALLDAYSSET ;
        } else {

            for (int i = 0; i < 7; i ++) {
                if (mButtonDays[i].isChecked())
                    whichdays |= (1 << i) ;
            }
        }

        return whichdays ;
    }

    @Override
    public void updateView(Cursor cursor) {

        if (cursor == null)
            return ;

        cursor.moveToFirst();
        int idxName = cursor.getColumnIndex(RulesColumns.NAME);
        int idxCondition = cursor.getColumnIndex(RulesColumns.CONDITION);
        int idxDescription = cursor.getColumnIndex(RulesColumns.DESCRIPTION);
        int idxRingMode = cursor.getColumnIndex(RulesColumns.RINGMODE);
        int idxActivited = cursor.getColumnIndex(RulesColumns.ACTIVATED);

        strName = cursor.getString(idxName);
        mEditName.setText(strName);

        strDescription = cursor.getString(idxDescription);
        mEditDescription.setText(strDescription);

        mRingMode = cursor.getInt(idxRingMode);
        setRingMode(mRingMode);

        String strCondition = cursor.getString(idxCondition);
        TimeCondition timeCondition = new TimeCondition(strCondition);
        Time starttime = timeCondition.getBegin();
        Time endtime = timeCondition.getEnd();
        strStartTime = starttime.toString().substring(0,starttime.toString().lastIndexOf(':'));
        strEndTime = endtime.toString().substring(0,endtime.toString().lastIndexOf(':'));
        mEditStartTime.setText(strStartTime);
        mEditEndTime.setText(strEndTime);

        bActivited = (cursor.getInt(idxActivited) != 0);
        mSwitchActivited.setChecked(bActivited);

        if(timeCondition.isAllDaySet()) {
            CheckBox chkAllDays = (CheckBox)findViewById(R.id.chkAllDays);
            chkAllDays.setChecked(timeCondition.isAllDaySet());
            //TODO hidden Weekdays selector.
        }

        iWhichDays = timeCondition.getWhichdays() ;
        mButtonDays[0].setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_SUNDAY));
        mButtonDays[1].setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_MONDAY));
        mButtonDays[2].setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_TUESDAY));
        mButtonDays[3].setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_WEDNESDAY));
        mButtonDays[4].setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_THURSDAY));
        mButtonDays[5].setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_FRIDAY));
        mButtonDays[6].setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_SATURDAY));
    }

    @Override
    public boolean isModified() {
        if (mEditName.getText().toString().equals(strName) == false)
            return true ;

        if (mEditDescription.getText().toString().equals(strDescription) == false)
            return true ;

        if (mEditStartTime.getText().toString().equals(strStartTime) == false)
            return true;

        if (mEditEndTime.getText().toString().equals(strEndTime) == false)
            return true ;

        if (mSwitchActivited.isChecked() != bActivited)
            return true ;

        if (getRingMode() != mRingMode)
            return true ;

        if (getWhichDays() != iWhichDays)
            return true ;

        return false;
    }

    @Override
    public ContentValues getContentValues() {

        ContentValues values ;

        String strname = mEditName.getText().toString().trim();
        if (strname.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_need_rule_name), Toast.LENGTH_SHORT).show();
            return null;
        }
        strName = strname ;

        strDescription = mEditDescription.getText().toString().trim();
        String strtemp = mEditStartTime.getText().toString().trim() ;
        if (strtemp.isEmpty()) {
            Toast.makeText(this, "Invalid time format.", Toast.LENGTH_SHORT).show();
            return null;
        }
        strStartTime = strtemp ;

        strtemp = mEditEndTime.getText().toString().trim();
        if (strtemp.isEmpty()) {
            Toast.makeText(this, "Invalid time format.", Toast.LENGTH_SHORT).show();
            return null;
        }
        strEndTime = strtemp ;
        bActivited = mSwitchActivited.isChecked() ;
        iWhichDays = getWhichDays() ;
        mRingMode = getRingMode() ;

        String  strcondition = String.format("time: %s, %s, %s",
                strStartTime, strEndTime, TimeCondition.whichdaysToString(iWhichDays)) ;

        values = new ContentValues() ;
        if (getMode() == Constants.INTENT_NEW) {
            values.put(RulesColumns.RULETYPE, RulesColumns.RT_TIME);
            values.put(RulesColumns.SECONDCONDITION,""); //TODO combine condition until version 2.0 to iimplement
        }

        values.put(RulesColumns.NAME, strName);
        values.put(RulesColumns.ACTIVATED, bActivited?1:0);
        values.put(RulesColumns.RINGMODE, mRingMode);
        values.put(RulesColumns.CONDITION, strcondition);
        values.put(RulesColumns.DESCRIPTION, strDescription);

        return values;
    }

    @Override
    public void onSuccessUpdateDatabase(Uri uri) {

        if (bActivited)
            TimeRuleAlarmService.startScheduleAlarm(this, uri);
        else
            TimeRuleAlarmService.cancelScheduledAlarm(this, uri);
    }


}
