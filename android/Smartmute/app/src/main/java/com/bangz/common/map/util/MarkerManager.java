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

package com.bangz.common.map.util;

import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is copy from android-maps-utils MarkerManager.
 * https://developers.google.com/maps/documentation/android/utility/
 *
 * change class to a template class for add extra info with a Marker.
 * <p/>
 *
 * Keeps track of collections of markers on the map and customer extra info class T of a marker.
 * Delegates all Marker-related events to each collection's individually managed listeners.
 *
 * <p/>
 * All marker operations (adds and removes) must via Manager class, no long via collection class.

 *
 * Created by wangruoyu on 15-03-06.
 */
public class MarkerManager<T> implements
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.InfoWindowAdapter {

    private final GoogleMap mMap ;

    private final Map<String, Collection> mNamedCollections = new HashMap<String, Collection>();
    private final Map<Marker, Collection> mAllMarkers = new HashMap<Marker, Collection>();

    private final Map<Marker, T> mAllExInfos = new HashMap<Marker, T>();


    public MarkerManager(GoogleMap map) {
        mMap = map ;
    }

    public Collection newCollection() {
        return new Collection() ;
    }

    /**
     * Create a new named collection, which can later be looked up by {@link #getCollection(String)}
     * @param id a unique id for this collection.
     */
    public Collection newCollection(String id) {
        if (mNamedCollections.get(id) != null) {
            throw new IllegalArgumentException("collection id is not unique: " + id);
        }
        Collection collection = new Collection();
        mNamedCollections.put(id, collection);
        return collection;
    }

    /**
     * Gets a named collection that was created by {@link #newCollection(String)}
     * @param id the unique id for this collection.
     */
    public Collection getCollection(String id) {
        return mNamedCollections.get(id);
    }

    public void addMarker(String collectionId, Marker marker, T exinfo) {
        Collection c = mNamedCollections.get(collectionId) ;
        if (c == null) {
            throw new IllegalArgumentException(
                    String.format("collection %s is not exist.",collectionId));
        }

    }

    /**
     * Removes a marker from its collection.
     *
     * @param marker the marker to remove.
     * @return true if the marker was removed.
     */
    public boolean remove(Marker marker) {
        Collection collection = mAllMarkers.get(marker);
        return collection != null && collection.remove(marker);
    }

    public T getExtraInfo(Marker marker) {
        return mAllExInfos.get(marker);
    }

    public Map<Marker, T> getAllExInfos() {
        return Collections.unmodifiableMap(mAllExInfos);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        Collection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mInfoWindowAdapter != null) {
            return collection.mInfoWindowAdapter.getInfoWindow(marker);
        }
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        Collection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mInfoWindowAdapter != null) {
            return collection.mInfoWindowAdapter.getInfoContents(marker);
        }
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Collection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mInfoWindowClickListener != null) {
            collection.mInfoWindowClickListener.onInfoWindowClick(marker);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Collection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mMarkerClickListener != null) {
            return collection.mMarkerClickListener.onMarkerClick(marker);
        }
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Collection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDragStart(marker);
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Collection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDrag(marker);
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Collection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDragEnd(marker);
        }
    }

    public class Collection {
        private final Set<Marker> mMarkers = new HashSet<Marker>();
        private GoogleMap.OnInfoWindowClickListener mInfoWindowClickListener;
        private GoogleMap.OnMarkerClickListener mMarkerClickListener;
        private GoogleMap.OnMarkerDragListener mMarkerDragListener;
        private GoogleMap.InfoWindowAdapter mInfoWindowAdapter;

        protected Collection() {  }

        public Marker addMarker(MarkerOptions opts, T exinfo) {
            Marker marker = mMap.addMarker(opts);
            mMarkers.add(marker);
            mAllMarkers.put(marker, Collection.this);
            mAllExInfos.put(marker, exinfo);
            return marker;
        }

        public boolean remove(Marker marker) {

            if (mMarkers.remove(marker)) {
                mAllMarkers.remove(marker);
                mAllExInfos.remove(marker);
                marker.remove();
                return true;
            }
            return false;
        }

        public void clear() {
            for (Marker marker : mMarkers) {
                marker.remove();
                mAllMarkers.remove(marker);
                mAllExInfos.remove(marker);
            }
            mMarkers.clear();
        }

        public java.util.Collection<Marker> getMarkers() {
            return Collections.unmodifiableCollection(mMarkers);
        }

        public void setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener infoWindowClickListener) {
            mInfoWindowClickListener = infoWindowClickListener;
        }

        public void setOnMarkerClickListener(GoogleMap.OnMarkerClickListener markerClickListener) {
            mMarkerClickListener = markerClickListener;
        }

        public void setOnMarkerDragListener(GoogleMap.OnMarkerDragListener markerDragListener) {
            mMarkerDragListener = markerDragListener;
        }

        public void setOnInfoWindowAdapter(GoogleMap.InfoWindowAdapter infoWindowAdapter) {
            mInfoWindowAdapter = infoWindowAdapter;
        }
    }
}
