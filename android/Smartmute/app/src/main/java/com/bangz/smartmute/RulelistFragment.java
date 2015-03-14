
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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.support.v4.app.LoaderManager;

import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bangz.smartmute.R;

import com.bangz.smartmute.content.RulesColumns;
import com.bangz.smartmute.services.TimeRuleAlarmService;
import com.bangz.smartmute.util.LogUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class RulelistFragment extends ListFragment
    implements LoaderManager.LoaderCallbacks,
    RulelistAdapter.ActivedButtonListener {


    private static final String TAG = "RulelistFragment";


    private int mViewWhich ;
    private static final int VIEW_ALL = 0;
    private static final int VIEW_LOCATION = 1;
    private static final int VIEW_WIFI = 2;
    private static final int VIEW_TIME = 3 ;

    public interface OnRuleItemClickListerner {
        public void onRuleItemSelected(long id, int ruletype);
        //public void onEnableRuleItem(long id, boolean enabled);
    }
    private OnRuleItemClickListerner mRuleItemSelectedListerner ;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";



    private RulelistAdapter mAdapter ;
    private static final String[] PROJECTION = {RulesColumns._ID,
            RulesColumns.NAME,
            RulesColumns.ACTIVATED,
            RulesColumns.RULETYPE,
            RulesColumns.CONDITION,
            RulesColumns.SECONDCONDITION,
            RulesColumns.RINGMODE
    };

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static RulelistFragment newInstance(int sectionNumber) {
        RulelistFragment fragment = new RulelistFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public RulelistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mAdapter = new RulelistAdapter(getActivity(),this);

        Bundle args = getArguments() ;
        mViewWhich = args.getInt(ARG_SECTION_NUMBER) - 1;

        setListAdapter(mAdapter);
        LoaderManager lm = getLoaderManager();

        lm.initLoader(1, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rulelist, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((BaseActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));

        try {
            mRuleItemSelectedListerner = (OnRuleItemClickListerner) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnRuleItemClickListerner.");
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {


        Activity parentActivity = getActivity() ;
        if (!(parentActivity instanceof BaseActivity
                && ((BaseActivity) parentActivity).isDrawerOpened())) {
            menu.clear();

            inflater.inflate(R.menu.menu_rulelist_fragment, menu);

            super.onCreateOptionsMenu(menu, inflater);
        }

    }


    @Override
    public void onDetach() {
        super.onDetach();

        mRuleItemSelectedListerner = null ;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String selection = new String();

        switch(mViewWhich) {
            case VIEW_ALL:
                break;
            case VIEW_LOCATION:
                selection = RulesColumns.RULETYPE + " = " + RulesColumns.RT_LOCATION ;
                break;
            case VIEW_WIFI:
                selection = RulesColumns.RULETYPE + " = " + RulesColumns.RT_WIFI ;
                break;
            case VIEW_TIME:
                selection = RulesColumns.RULETYPE + " = " + RulesColumns.RT_TIME ;
                break;
        }
        return new CursorLoader(getActivity(),RulesColumns.CONTENT_URI,PROJECTION, selection, null,null);
    }



    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mAdapter.swapCursor((Cursor)data);
    }

    @Override
    public void onLoaderReset(Loader loader) {

        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        long rowId = cursor.getLong(cursor.getColumnIndex(RulesColumns._ID));
        int ruletype = cursor.getInt(cursor.getColumnIndex(RulesColumns.RULETYPE));
        //Log.d(TAG,"Param id = " + id + " Get from cursor id = "+ rowId + " Ruletype = " + ruletype);

        mRuleItemSelectedListerner.onRuleItemSelected(id, ruletype);

    }

    @Override
    public void onActivedButtonClick(long id, boolean bActivited) {
        //Cursor cursor = mAdapter.getCursor();

        LogUtils.LOGD(TAG,"Activited Button clicked. id: " + id + " Activited: " + bActivited);

        ContentResolver cr = getActivity().getContentResolver();
        Uri uri = ContentUris.withAppendedId(RulesColumns.CONTENT_URI,id);

        String[] projects = {
          RulesColumns.RULETYPE

        };
        Cursor cursor = cr.query(uri,projects,null,null,null);
        cursor.moveToFirst();
        int ruletype = cursor.getInt(cursor.getColumnIndex(RulesColumns.RULETYPE));


        ContentValues contentValues = new ContentValues();
        contentValues.put(RulesColumns.ACTIVATED, bActivited?1:0);
        cr.update(uri,contentValues,null,null);

        mAdapter.notifyDataSetChanged();

        if (ruletype == RulesColumns.RT_TIME) {
            if (bActivited == false)
                TimeRuleAlarmService.cancelScheduledAlarm(getActivity(), uri);
            else
                TimeRuleAlarmService.startScheduleAlarm(getActivity(),uri);
        } else if (ruletype == RulesColumns.RT_LOCATION) {
            //TODO cancel location mute
        }
    }

}
