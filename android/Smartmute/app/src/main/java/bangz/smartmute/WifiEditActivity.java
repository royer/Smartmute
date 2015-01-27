
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

import android.app.ActionBar;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.LoaderManager;
import android.content.ContentValues;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.LoaderManager;
//import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.bangz.lib.ui.donebar.DoneBarActivity;

import bangz.smartmute.content.RulesColumns;
import bangz.smartmute.content.WifiCondition;


public class WifiEditActivity extends DoneBarActivity
implements LoaderManager.LoaderCallbacks {

    private static final String TAG = WifiEditActivity.class.getSimpleName();

    private int mode ;

    private Uri mUri;
    private Cursor mCursor ;
    private boolean bModified = false ;


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
        setContentView(R.layout.activity_wifi_edit);

        //ActionBar actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeButtonEnabled(true);

        Intent intent = getIntent();
        mode = intent.getIntExtra(Constants.INTENT_EDITORNEW,Constants.INTENT_NEW);

        if(mode == Constants.INTENT_EDIT) {
            mUri = intent.getData();
            LoaderManager lm = getSupportLoaderManager();
            lm.initLoader(1,null,this);

        }

        //TODO need remove this savebutton when finished Donebar
        Button saveButton = (Button)findViewById(R.id.SaveWifiRule);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToDatabase();
            }
        });

        // set ringmode on RadioGroup
        RadioGroup radiogroup = (RadioGroup)findViewById(R.id.ringmode);

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
//        getMenuInflater().inflate(R.menu.wifi_edit, menu);
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

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mUri, PROJECTS,"",null,null);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mCursor = (Cursor)data;
        UpdateView();
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mCursor = null ;

    }


    private void UpdateView() {
        if (mCursor == null) {
            return;
        }

        mCursor.moveToFirst();
        int idxName = mCursor.getColumnIndex(RulesColumns.NAME);
        int idxCondition = mCursor.getColumnIndex(RulesColumns.CONDITION);
        int idxRingMode = mCursor.getColumnIndex(RulesColumns.RINGMODE);

        String strname = mCursor.getString(idxName) ;
        final EditText viewName = (EditText)findViewById(R.id.WifiRuleName);
        viewName.setText(strname);

        String strCondition = mCursor.getString(idxCondition);

        WifiCondition wifiCondition = new WifiCondition(strCondition);
        String strSSID = wifiCondition.getSSID();
        final EditText viewSSID = (EditText)findViewById(R.id.SSID);
        viewSSID.setText(strSSID);

        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.ringmode);
        int ringmode = mCursor.getInt(idxRingMode);
        if (ringmode == RulesColumns.RM_SILENT) {
            radioGroup.check(R.id.silence);
        } else {
            radioGroup.check(R.id.vibrate);
        }

    }


    private void saveToDatabase() {
        final EditText viewRuleName = (EditText)findViewById(R.id.WifiRuleName);
        String strName = viewRuleName.getText().toString();

        final EditText viewSSID = (EditText)findViewById(R.id.SSID);
        String strSSID = viewSSID.getText().toString();
        WifiCondition wifiCondition = new WifiCondition();
        wifiCondition.setSSID(strSSID);
        String strCondition = wifiCondition.BuildConditionString();

        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.ringmode);
        int idRingMode = radioGroup.getCheckedRadioButtonId();
        int ringmode ;
        if (idRingMode == R.id.silence) {
            ringmode = RulesColumns.RM_SILENT ;
        } else {
            ringmode = RulesColumns.RM_VIBRATE;
        }

        if (mode == Constants.INTENT_NEW) {
            ContentValues values = new ContentValues();
            values.put(RulesColumns.NAME, strName);
            values.put(RulesColumns.ACTIVATED, 1);
            values.put(RulesColumns.RULETYPE, RulesColumns.RT_WIFI);
            values.put(RulesColumns.RINGMODE, ringmode);
            values.put(RulesColumns.CONDITION, strCondition);
            values.put(RulesColumns.SECONDCONDITION,"");
            values.put(RulesColumns.DESCRIPTION,"");

            mUri = getContentResolver().insert(RulesColumns.CONTENT_URI,values);
            mode = Constants.INTENT_EDIT;


        } else {
            ContentValues values = new ContentValues();

            values.put(RulesColumns.NAME, strName);
            values.put(RulesColumns.CONDITION, strCondition);
            values.put(RulesColumns.RINGMODE,ringmode);
            getContentResolver().update(mUri, values, null, null);
        }

        bModified = false ;
    }
}
