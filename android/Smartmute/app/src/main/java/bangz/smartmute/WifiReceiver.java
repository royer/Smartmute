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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import bangz.smartmute.services.WifiMuteService;
import bangz.smartmute.util.LogUtils;

public class WifiReceiver extends BroadcastReceiver {
    public static final String TAG = WifiReceiver.class.getSimpleName();

    public WifiReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

//        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action) == false)
//            return ;
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) == false)
            return ;

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (networkInfo == null) {
            LogUtils.LOGD(TAG, "get EXTRA_NETWORK_INFO return null");
        }
        if (networkInfo.isConnected()) {
            //WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            LogUtils.LOGD(TAG,"Network is connected. state = " + networkInfo.getDetailedState());
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                String ssid = wifiInfo.getSSID();
                //Toast.makeText(context, "Connected with " + ssid, Toast.LENGTH_SHORT).show();
                LogUtils.LOGD(TAG,"   Connected with " + ssid);
                WifiMuteService.wifiConnected(context, ssid);
            } else {
                LogUtils.LOGD(TAG,"   Connected to this type:" + networkInfo.getTypeName());
            }
        } else {
            //Toast.makeText(context, "Net no connected.", Toast.LENGTH_SHORT).show();
            LogUtils.LOGD(TAG, "NET not connected + state = " + networkInfo.getDetailedState() +
                " Type: " + networkInfo.getTypeName());

            WifiMuteService.wifiDisconnected(context);
        }

    }
}
