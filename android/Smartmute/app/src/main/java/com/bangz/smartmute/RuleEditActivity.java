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

package com.bangz.smartmute;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.bangz.lib.ui.donebar.DoneBarActivity;

import com.bangz.smartmute.content.RulesColumns;

/**
 * Created by royerwang on 15-01-30.
 */
public abstract class RuleEditActivity extends DoneBarActivity
        implements LoaderManager.LoaderCallbacks {

    private static final String TAG = RuleEditActivity.class.getSimpleName();

    /**
     * Indicate is for new rule or edit exist rule
     * If new mode value is {@link Constants#INTENT_NEW}
     * If edit exist value is {@link Constants#INTENT_EDIT}
     */
    private int mMode ;

    /**
     * Current edit content uri.
     */
    private Uri mUri ;

    private Cursor cursor;


    public static final String[] PROJECTS = new String[] {
            RulesColumns._ID,
            RulesColumns.NAME,
            RulesColumns.RULETYPE,
            RulesColumns.CONDITION,
            RulesColumns.LATITUDE,
            RulesColumns.LONGITUDE,
            RulesColumns.RADIUS,
            RulesColumns.SECONDCONDITION,
            RulesColumns.ACTIVATED,
            RulesColumns.RINGMODE,
            RulesColumns.DESCRIPTION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mMode = intent.getIntExtra(Constants.INTENT_EDITORNEW, Constants.INTENT_NEW);
        mUri = intent.getData();

        if (savedInstanceState == null) {
        } else {

        }

        if (mUri == null) {
            mMode = Constants.INTENT_NEW;
        } else {
            mMode = Constants.INTENT_EDIT;
            LoaderManager lm =  getSupportLoaderManager() ;
            lm.initLoader(1,null, this);
        }

        showDoneCancelBar(true);

    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mUri, PROJECTS,"",null,null);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

        cursor = (Cursor)data;
        updateView(cursor);

    }

    @Override
    public void onLoaderReset(Loader loader) {

        cursor = null ;

    }

    @Override
    public void onDoneButtonClicked() {
        if (isModified()) {
            ContentValues values = getContentValues();

            if ( values != null) {
                if (mMode == Constants.INTENT_NEW) {
                    mUri = getContentResolver().insert(RulesColumns.CONTENT_URI, values);
                    mMode = Constants.INTENT_EDIT;
                    Intent intent = getIntent();
                    intent.setData(mUri);
                    intent.putExtra(Constants.INTENT_EDITORNEW, mMode) ;

                } else {
                    getContentResolver().update(mUri, values, null, null);
                }

                onSuccessUpdateDatabase(mUri);
                finish();
            }
        } else {
            finish() ;
        }

    }

    @Override
    public void onCancelButtonClicked() {
        finish();
    }

    public int getMode() {
        return mMode ;
    }

    public Uri getUri() {
        return mUri;
    }

    public Cursor getCursor() { return cursor; }

    public abstract void updateView(Cursor cursor);

    /**
     * Data had changed by user return true or return false
     * @return
     */
    public abstract boolean isModified();

    /**
     * subclass creates the ContentValues and fills it.
     * if input data is invalid return null
     * @return ContentValues
     */
    public abstract ContentValues getContentValues();

    /**
     * Subclass implement this method do something after rule successful write to database.
     * @param uri
     */
    public abstract void onSuccessUpdateDatabase(Uri uri);

}
