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

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bangz.smartmute.R;

import com.bangz.smartmute.content.Condition;
import com.bangz.smartmute.content.ConditionFactory;
import com.bangz.smartmute.content.RulesColumns;
import com.bangz.smartmute.content.WifiCondition;
import com.bangz.smartmute.util.LogUtils;

/**
 * Created by royerwang on 15-01-23.
 */
public class RulelistAdapter extends SimpleCursorAdapter {

    private static final String TAG = RulelistAdapter.class.getSimpleName();

    public static interface ActivedButtonListener {
        public void onActivedButtonClick(long id, boolean bActivited);
    }

    private ActivedButtonListener mActivitedButtonListener;

    private static class ViewHolder {
        public ImageView imageType ;
        public TextView  txtRuleName ;
        public TextView  txtDetail;
        public Switch    switchActivited;
    }

    private static final String[] columns = {
            RulesColumns.RULETYPE,
            RulesColumns.NAME,
            RulesColumns.ACTIVATED
    };
    private static final int [] listitemids = {R.id.RuleIcon,R.id.RuleName,R.id.RuleOnOff};


    public RulelistAdapter(Context context, int layout, Cursor c, String[] from, int[] to,
                           int flags, ActivedButtonListener listener) {
        super(context, layout, c, from, to, flags);
        mActivitedButtonListener = listener ;
    }

    public RulelistAdapter(Context context,ActivedButtonListener listener) {


        this(context, R.layout.rulelist_item,null,columns, listitemids, 0,listener);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int idxName = cursor.getColumnIndex(RulesColumns.NAME);
        int idxRuleType = cursor.getColumnIndex(RulesColumns.RULETYPE);
        int idxCondition = cursor.getColumnIndex(RulesColumns.CONDITION);
        int idxSecondCondition = cursor.getColumnIndex(RulesColumns.SECONDCONDITION);
        int idxActivited = cursor.getColumnIndex(RulesColumns.ACTIVATED);

        ViewHolder vh = (ViewHolder)view.getTag();

        String name = cursor.getString(idxName);

        if (name == null || (name != null && name.isEmpty()))
            name = context.getString(R.string.noname);
        vh.txtRuleName.setText(name);

        int[] ruletypeiconids = {
                0,
                R.drawable.ic_location,
                R.drawable.ic_wifi,
                R.drawable.ic_clock};
        int ruletype = cursor.getInt(idxRuleType);

        vh.imageType.setImageResource(ruletypeiconids[ruletype]);

        String strcondition = cursor.getString(idxCondition);
        Condition condition = ConditionFactory.CreateCondition(strcondition);
        vh.txtDetail.setText(Html.fromHtml(condition.description(context)));

        if (ruletype == RulesColumns.RT_WIFI) {
            String ssid = ((WifiCondition)condition).getSSID() ;
            vh.txtRuleName.setText(ssid);
        }


        int activeted = cursor.getInt(idxActivited);
        vh.switchActivited.setChecked(activeted != 0);
        vh.switchActivited.setTag(cursor.getLong(cursor.getColumnIndex(RulesColumns._ID)));
        //super.bindView(view, context, cursor);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View vreturn = super.newView(context, cursor, parent);

        Switch sb = (Switch)vreturn.findViewById(R.id.RuleOnOff);

        ViewHolder vh = new ViewHolder();
        vh.imageType = (ImageView)vreturn.findViewById(R.id.RuleIcon);
        vh.txtRuleName = (TextView)vreturn.findViewById(R.id.RuleName);
        vh.txtDetail = (TextView)vreturn.findViewById(R.id.Detail);
        vh.switchActivited = (Switch)vreturn.findViewById(R.id.RuleOnOff);
        vreturn.setTag(vh);

        sb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long id = (Long)v.getTag();
                boolean b = ((Switch)v).isChecked();

                if (mActivitedButtonListener != null) {
                    mActivitedButtonListener.onActivedButtonClick(id, b);
                }
            }
        });

        return vreturn ;
    }
}
