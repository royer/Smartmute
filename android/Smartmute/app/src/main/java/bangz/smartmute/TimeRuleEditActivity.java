
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

package bangz.smartmute;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.support.v4.app.LoaderManager;
import android.content.ContentValues;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.bangz.lib.ui.donebar.DoneBarActivity;

import java.sql.Time;

import bangz.smartmute.content.RulesColumns;
import bangz.smartmute.content.TimeCondition;
import bangz.smartmute.services.TimeRuleAlarmService;
import bangz.smartmute.util.LogUtils;


public class TimeRuleEditActivity extends DoneBarActivity
        implements LoaderManager.LoaderCallbacks {

    private static final String TAG=TimeRuleEditActivity.class.getSimpleName();

    // Button for weekdays



    private int mode;   //for NEW record or Update exist record
    private Uri mUri;
    private Cursor mCursor;
    private boolean bModified = false;

    private static final String[] PROJECTS = new String[] {
            RulesColumns._ID,
            RulesColumns.NAME,
            RulesColumns.RULETYPE,
            RulesColumns.CONDITION,
            RulesColumns.SECONDCONDITION,
            RulesColumns.ACTIVATED,
            RulesColumns.RINGMODE,
            RulesColumns.DESCRIPTION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timerule_edit);

        Intent intent = getIntent();
        mode = intent.getIntExtra(Constants.INTENT_EDITORNEW,Constants.INTENT_NEW);

        if (mode == Constants.INTENT_EDIT) {
            mUri = intent.getData();
            LoaderManager lm = getSupportLoaderManager();
            lm.initLoader(1, null, this);
        }

        //TODO need remove this savebutton when finished Donebar
        Button saveButton = (Button)findViewById(R.id.btnSaveTimeRule);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToDatabase();
            }
        });

        showDoneCancelBar(true);

    }

    @Override
    public void onDoneButtonClicked() {
        saveToDatabase();
        finish();
    }

    @Override
    public void onCancelButtonClicked() {
        finish();
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

    private void updateView() {

        if (mCursor == null)
            return ;

        mCursor.moveToFirst();
        int idxName = mCursor.getColumnIndex(RulesColumns.NAME);
        int idxCondition = mCursor.getColumnIndex(RulesColumns.CONDITION);
        int idxDescription = mCursor.getColumnIndex(RulesColumns.DESCRIPTION);
        int idxRingMode = mCursor.getColumnIndex(RulesColumns.RINGMODE);
        int idxActivited = mCursor.getColumnIndex(RulesColumns.ACTIVATED);

        String strname = mCursor.getString(idxName);
        final EditText editName = (EditText)findViewById(R.id.TimeRuleName);
        editName.setText(strname);
        String strdescript = mCursor.getString(idxDescription);
        final EditText editDescript = (EditText)findViewById(R.id.TimeRuleDescription);
        editDescript.setText(strdescript);

        RadioGroup radiogroup = (RadioGroup)findViewById(R.id.ringmode);
        int ringmode = mCursor.getInt(idxRingMode);
        if (ringmode == RulesColumns.RM_SILENT) {
            radiogroup.check(R.id.silence);
        } else {
            radiogroup.check(R.id.vibrate);
        }

        String strCondition = mCursor.getString(idxCondition);
        TimeCondition timeCondition = new TimeCondition(strCondition);
        Time starttime = timeCondition.getBegin();
        Time endtime = timeCondition.getEnd();
        String strStartTime = starttime.toString().substring(0,starttime.toString().lastIndexOf(':'));
        String strEndTime = endtime.toString().substring(0,endtime.toString().lastIndexOf(':'));
        EditText editStartTime = (EditText)findViewById(R.id.editStartTime);
        editStartTime.setText(strStartTime);
        EditText editEndTime = (EditText)findViewById(R.id.editEndTime);
        editEndTime.setText(strEndTime);

        Switch btnSwitch = (Switch)findViewById(R.id.Activited);
        btnSwitch.setChecked(mCursor.getInt(idxActivited) != 0);

        if(timeCondition.isAllDaySet()) {
            CheckBox chkAllDays = (CheckBox)findViewById(R.id.chkAllDays);
            chkAllDays.setChecked(timeCondition.isAllDaySet());
            //TODO hidden Weekdays selector.
        }

        ToggleButton button = (ToggleButton)findViewById(R.id.Sunday);
        button.setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_SUNDAY));
        button = (ToggleButton)findViewById(R.id.Monday);
        button.setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_MONDAY));
        button = (ToggleButton)findViewById(R.id.Tuesday);
        button.setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_TUESDAY));
        button = (ToggleButton)findViewById(R.id.Wednesday);
        button.setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_WEDNESDAY));
        button = (ToggleButton)findViewById(R.id.Thursday);
        button.setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_THURSDAY));
        button = (ToggleButton)findViewById(R.id.Friday);
        button.setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_FRIDAY));
        button = (ToggleButton)findViewById(R.id.Saturday);
        button.setChecked(timeCondition.isEnableOnThisDay(TimeCondition.IDX_SATURDAY));
    }


    private void saveToDatabase() {

        final EditText rulename = (EditText)findViewById(R.id.TimeRuleName);
        String strName = rulename.getText().toString();

        final EditText editDescript = (EditText)findViewById(R.id.TimeRuleDescription);
        String strDescript = editDescript.getText().toString();

        final EditText editStartTime = (EditText)findViewById(R.id.editStartTime);
        String strStartTime = editStartTime.getText().toString();

        final EditText editEndTime = (EditText)findViewById(R.id.editEndTime);
        String strEndTime = editEndTime.getText().toString();

        final Switch btnActivited = (Switch)findViewById(R.id.Activited);
        int activited = btnActivited.isChecked() ? 1:0;

        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.ringmode);
        int idRingMode = radioGroup.getCheckedRadioButtonId();
        int ringmode ;
        if (idRingMode == R.id.silence) {
            ringmode = RulesColumns.RM_SILENT;
        } else  {
            ringmode = RulesColumns.RM_VIBRATE ;
        }

        String strWhichDays ;
        CheckBox cbAllDays = (CheckBox)findViewById(R.id.chkAllDays);
        if (cbAllDays.isChecked()) {
            strWhichDays = "1111111";
        } else {
            ToggleButton buttonDay = (ToggleButton)findViewById(R.id.Sunday);
            StringBuilder stringbuilder = new StringBuilder();
            stringbuilder.append(buttonDay.isChecked()?"1":"0");
            buttonDay = (ToggleButton)findViewById(R.id.Monday);
            stringbuilder.append(buttonDay.isChecked()?"1":"0");
            buttonDay = (ToggleButton)findViewById(R.id.Tuesday);
            stringbuilder.append(buttonDay.isChecked()?"1":"0");
            buttonDay = (ToggleButton)findViewById(R.id.Wednesday);
            stringbuilder.append(buttonDay.isChecked()?"1":"0");
            buttonDay = (ToggleButton)findViewById(R.id.Thursday);
            stringbuilder.append(buttonDay.isChecked()?"1":"0");
            buttonDay = (ToggleButton)findViewById(R.id.Friday);
            stringbuilder.append(buttonDay.isChecked()?"1":"0");
            buttonDay = (ToggleButton)findViewById(R.id.Saturday);
            stringbuilder.append(buttonDay.isChecked()?"1":"0");

            strWhichDays = stringbuilder.toString();
        }

        String strCondition ;
        strCondition = String.format("time: %s, %s, %s", strStartTime, strEndTime, strWhichDays);
        long recordid = 0;

        if (mode == Constants.INTENT_NEW) {
            ContentValues values = new ContentValues();
            values.put(RulesColumns.NAME, strName);
            values.put(RulesColumns.ACTIVATED, activited);
            values.put(RulesColumns.RULETYPE, RulesColumns.RT_TIME);
            values.put(RulesColumns.RINGMODE, ringmode);
            values.put(RulesColumns.CONDITION, strCondition);
            values.put(RulesColumns.SECONDCONDITION, "");  //TODO combine condition until Version 2.0 to implement.
            values.put(RulesColumns.DESCRIPTION,strDescript);

            mUri = getContentResolver().insert(RulesColumns.CONTENT_URI, values);
            mode = Constants.INTENT_EDIT;
        } else {
            ContentValues values = new ContentValues();
            values.put(RulesColumns.NAME, strName);
            values.put(RulesColumns.ACTIVATED, activited);
            values.put(RulesColumns.CONDITION, strCondition);
            values.put(RulesColumns.RINGMODE,ringmode);
            values.put(RulesColumns.DESCRIPTION, strDescript);
            getContentResolver().update(mUri, values, null, null);


        }

        recordid = ContentUris.parseId(mUri);

        if (activited != 0) {
            TimeRuleAlarmService.startScheduleAlarm(this, mUri);
        } else {
            TimeRuleAlarmService.cancelScheduledAlarm(this, mUri);
        }

        bModified = false;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mUri, PROJECTS,"",null,null);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mCursor = (Cursor)data;
        updateView();

    }


    @Override
    public void onLoaderReset(Loader loader) {

        mCursor = null ;
    }
}
