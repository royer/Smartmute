
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
//import android.support.v4.app.LoaderManager;
//import android.support.v4.content.CursorLoader;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.bangz.smartmute.R;

import com.bangz.smartmute.content.RulesColumns;
import com.bangz.smartmute.content.WifiCondition;
import com.bangz.smartmute.services.WifiMuteService;


public class WifiEditActivity extends RuleEditActivity {

    private static final String TAG = WifiEditActivity.class.getSimpleName();

    /**
     * The last rule name read from database
     */
    private String strSSID;
    private boolean bActivited ;
    private int    mRingMode ;

    private EditText    mEditSSID;
    private Switch      mSwitchActivited;

    private RadioGroup mViewRingMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wifi_edit);

        mEditSSID = (EditText)findViewById(R.id.SSID);
        mSwitchActivited = (Switch)findViewById(R.id.Activited);

        mViewRingMode = (RadioGroup)findViewById(R.id.ringmode);


        if (savedInstanceState == null) {
            strSSID = "";
            bActivited = false;
            mRingMode = RulesColumns.RM_NORMAL ;
            mSwitchActivited.setChecked(true);
        }

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

    @Override
    public void updateView(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        cursor.moveToFirst();
        int idxName = cursor.getColumnIndex(RulesColumns.NAME);
        int idxCondition = cursor.getColumnIndex(RulesColumns.CONDITION);
        int idxRingMode = cursor.getColumnIndex(RulesColumns.RINGMODE);
        int idxActivited = cursor.getColumnIndex(RulesColumns.ACTIVATED);


        String strCondition = cursor.getString(idxCondition);

        WifiCondition wifiCondition = new WifiCondition(strCondition);

        strSSID = wifiCondition.getSSID();
        mEditSSID.setText(strSSID);

        bActivited = (cursor.getInt(idxActivited) == 1);
        mSwitchActivited.setChecked(bActivited);



        mRingMode = cursor.getInt(idxRingMode);
        setRingMode(mRingMode);

    }



    @Override
    public ContentValues getContentValues() {

        String strssid = mEditSSID.getText().toString().trim();
        if (strssid.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_need_SSID), Toast.LENGTH_SHORT).show();
            return null;
        }
        strSSID = strssid ;


        WifiCondition wifiCondition = new WifiCondition();
        wifiCondition.setSSID(strSSID);
        String strCondition = wifiCondition.BuildConditionString();

        bActivited = mSwitchActivited.isChecked() ;

        int ringmode  = getRingMode();

        ContentValues values = new ContentValues();

        if (getMode() == Constants.INTENT_NEW) {
            values.put(RulesColumns.RULETYPE, RulesColumns.RT_WIFI);
            values.put(RulesColumns.SECONDCONDITION,"");
        }
        values.put(RulesColumns.ACTIVATED, bActivited?1:0);
        values.put(RulesColumns.CONDITION, strCondition);
        values.put(RulesColumns.RINGMODE,ringmode);

        return values;
    }

    @Override
    public void onSuccessUpdateDatabase(Uri uri) {

        if (bActivited == false)
            return ;


        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cm.getActiveNetworkInfo();

        if (ninfo != null && ninfo.isConnected() && ninfo.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String currssid = wifiInfo.getSSID();
                if (currssid.equals(strSSID)) {
                    WifiMuteService.wifiConnected(this, strSSID);
                }
            }
        }

    }
}
