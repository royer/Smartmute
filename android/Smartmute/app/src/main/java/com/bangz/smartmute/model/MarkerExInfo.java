/*
 * Copyright (c) 2015 Royer Wang. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bangz.smartmute.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by wangruoyu on 15-03-08.
 */
public class MarkerExInfo implements Parcelable {
    private int m_collectionid;
    private long m_databaseid ;
    private int  m_cursor_pos ;

    private LatLng m_point ;  // save for restoreInstance

    public MarkerExInfo(LatLng point, int collectionid, long databaseid, int curpostion) {
        m_point = point ;
        m_collectionid = collectionid ;
        m_databaseid = databaseid ;
        m_cursor_pos = curpostion ;
    }

    public MarkerExInfo(LatLng point, int collectionid) {
        this(point, collectionid,0, 0);
    }

    private MarkerExInfo(Parcel in) {
        //m_point = in.readParcelable(m_point.getClass().getClassLoader());
        m_collectionid = in.readInt();
        m_databaseid = in.readLong();
        m_cursor_pos = in.readInt();

        double lat = in.readDouble();
        double lng = in.readDouble();
        m_point = new LatLng(lat, lng);



    }

    public int getCollectionID() {
        return m_collectionid;
    }
    public long getDatabaseId() {
        return m_databaseid ;
    }
    public int getCursorPosition() {
        return m_cursor_pos ;
    }

    public LatLng getPoint() {
        return m_point ;
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass().equals(this.getClass()) == false)
            return false;



        if (m_point.equals(((MarkerExInfo)o).m_point) == false)
            return false;
        if (m_collectionid != ((MarkerExInfo)o).m_collectionid)
            return false ;
        if (m_collectionid != ((MarkerExInfo)o).m_collectionid)
            return false ;
        if (m_databaseid != ((MarkerExInfo)o).m_databaseid)
            return false ;
        return true ;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //m_point.writeToParcel(dest,flags);
        dest.writeInt(m_collectionid);
        dest.writeLong(m_databaseid);
        dest.writeInt(m_cursor_pos);
        dest.writeDouble(m_point.latitude);
        dest.writeDouble(m_point.longitude);

    }

    public static final Parcelable.Creator<MarkerExInfo> CREATOR =
            new Parcelable.Creator<MarkerExInfo>() {
                @Override
                public MarkerExInfo createFromParcel(Parcel source) {
                    return new MarkerExInfo(source);
                }

                @Override
                public MarkerExInfo[] newArray(int size) {
                    return new MarkerExInfo[size];
                }
            };

}
