/*
 * Copyright (c) 2015 Royer Wang. All rights reserved.
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

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import bangz.smartmute.content.LocationCondition;
import bangz.smartmute.content.RulesColumns;
import bangz.smartmute.util.PrefUtils;


public class LocationRuleEditActivity extends RuleEditActivity {

    private static final String TAG = LocationRuleEditActivity.class.getSimpleName();

    private String strName;
    private String strDescription;

    //private String strLongitude;
    //private String strLatitude;
    private double  mLongitude;
    private double  mLatitude;
    private int     mRadar;
    private boolean bActivited ;
    private int     mRingMode ;


    private EditText mEditName ;
    private EditText mEditDescription;
    private EditText mEditLongitude;
    private EditText mEditLatitude;
    private EditText mEditRadar;
    private Switch   mSwitchActivited;
    private RadioGroup mViewRingMode ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_rule_edit);

        mEditName = (EditText)findViewById(R.id.RuleName);
        mEditDescription = (EditText)findViewById(R.id.txtDescription);
        mEditLatitude = (EditText)findViewById(R.id.txtLatitude);
        mEditLongitude = (EditText)findViewById(R.id.txtLongitude);
        mEditRadar = (EditText)findViewById(R.id.txtRadar);
        mSwitchActivited = (Switch)findViewById(R.id.Activited);

        mViewRingMode = (RadioGroup)findViewById(R.id.ringmode);


        if (savedInstanceState == null) {
            strName = "";
            strDescription = "";

            mLongitude = 0.0;
            mLatitude = 0.0;

            mRadar = 0;
            bActivited = true;
            mRingMode = RulesColumns.RM_NORMAL;
        }
    }

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
        if (cursor == null)
            return ;

        cursor.moveToFirst() ;
        strName = cursor.getString(cursor.getColumnIndex(RulesColumns.NAME));
        mEditName.setText(strName);

        strDescription = cursor.getString(cursor.getColumnIndex(RulesColumns.DESCRIPTION));
        mEditDescription.setText(strDescription);

        bActivited = (cursor.getInt(cursor.getColumnIndex(RulesColumns.ACTIVATED))!=0);
        mSwitchActivited.setChecked(bActivited);

        String strcondition = cursor.getString(cursor.getColumnIndex(RulesColumns.CONDITION));
        LocationCondition condition = new LocationCondition(strcondition);
        int locformat = PrefUtils.getLocatonFormat(this);

        mLongitude = condition.getLongitude();
        String strtemp = Location.convert(mLongitude, locformat);
        mEditLongitude.setText(strtemp);

        mLatitude = condition.getLatitude();
        strtemp = Location.convert(mLatitude, locformat);
        mEditLatitude.setText(strtemp);

        mRadar = condition.getRadar();
        mEditRadar.setText(String.valueOf(mRadar));

        mRingMode = cursor.getInt(cursor.getColumnIndex(RulesColumns.RINGMODE));
        setRingMode(mRingMode);

    }

    @Override
    public boolean isModified() {
        if (mEditName.getText().toString().trim().equals(strName) == false) {
            return true;
        }
        if (mEditDescription.getText().toString().trim().equals(strDescription) == false) {
            return true ;
        }
        String strtemp = mEditLongitude.getText().toString().trim();
        if (strtemp.isEmpty() == false) {
            try {
                double dtemp = Location.convert(strtemp);
                if (dtemp != mLongitude)
                    return true;
            } catch (NullPointerException e) {
                if (mLongitude != 0.0)
                    return true;
            } catch (IllegalArgumentException e) {
                // wrong format, it must  modified by user, so return true.
                return true;
            }
        } else {
            if (mLongitude != 0.0d)
                return true ;
        }

        strtemp = mEditLatitude.getText().toString().trim();
        if (strtemp .isEmpty() == false) {
            try {
                double dtemp = Location.convert(strtemp);
                if (dtemp != mLatitude)
                    return true;
            } catch (NullPointerException e) {
                if (mLatitude != 0.0)
                    return true;
            } catch (IllegalArgumentException e) {
                return true;
            }
        } else {
            if (mLatitude != 0.0d)
                return true ;
        }

        strtemp = mEditRadar.getText().toString().trim();
        if ((strtemp.isEmpty() && mRadar != 0) ||
                (strtemp.isEmpty() == false && strtemp.equals(String.valueOf(mRadar)) == false))
            return true ;

        if (mSwitchActivited.isChecked() != bActivited)
            return true;

        if (getRingMode() != mRingMode)
            return true ;

        return false;
    }

    @Override
    public ContentValues getContentValues() {

        String strname = mEditName.getText().toString().trim();
        if (strname.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_need_rule_name), Toast.LENGTH_SHORT).show();
            return null ;
        }

        String strdescription = mEditDescription.getText().toString().trim() ;

        String strLongitude = mEditLongitude.getText().toString().trim();
        if (strLongitude.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_location_cannot_empty), Toast.LENGTH_SHORT).show();
            return null ;
        }
        String strLatitude = mEditLatitude.getText().toString().trim();
        if (strLatitude.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_location_cannot_empty),Toast.LENGTH_SHORT).show();
            return null ;
        }
        double longtitude, latitude ;
        try {
            longtitude = Location.convert(strLongitude);
            latitude = Location.convert(strLatitude);
        }catch (NullPointerException e) {
            Toast.makeText(this, getString(R.string.toast_location_cannot_empty), Toast.LENGTH_SHORT).show();
            return null ;

        }catch (IllegalArgumentException e) {
            Toast.makeText(this, getString(R.string.toast_location_format_error), Toast.LENGTH_SHORT).show();
            return null ;
        }
        String strradar = mEditRadar.getText().toString().trim();
        int radar ;
        try {
            radar = Integer.parseInt(strradar) ;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format in radar.", Toast.LENGTH_SHORT).show();
            return null;
        }
        LocationCondition condition = new LocationCondition(longtitude, latitude, radar);
        String strcondition = condition.BuildConditionString() ;

        int activited = mSwitchActivited.isChecked()?1:0;
        int ringmode = getRingMode() ;


        ContentValues values = new ContentValues() ;
        if (getMode() == Constants.INTENT_NEW) {
            values.put(RulesColumns.RULETYPE, RulesColumns.RT_LOCATION);
            values.put(RulesColumns.SECONDCONDITION, "");
        }

        values.put(RulesColumns.NAME, strname);
        values.put(RulesColumns.DESCRIPTION, strdescription);
        values.put(RulesColumns.CONDITION, strcondition);
        values.put(RulesColumns.ACTIVATED, activited);
        values.put(RulesColumns.RINGMODE, ringmode);
        return values;
    }

    @Override
    public void onSuccessUpdateDatabase(Uri uri) {

        strName = mEditName.getText().toString().trim();
        strDescription = mEditDescription.getText().toString().trim();
        bActivited = mSwitchActivited.isChecked() ;
        mRingMode = getRingMode() ;
        mLongitude = Location.convert(mEditLongitude.getText().toString().trim());
        mLatitude = Location.convert(mEditLatitude.getText().toString().trim());
        mRadar = Integer.parseInt(mEditRadar.getText().toString().trim());

        //TODO add this location to monition

    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_location_rule_edit, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
