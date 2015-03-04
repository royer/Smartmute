package com.bangz.smartmute.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by wangruoyu on 15-02-09.
 */
public class PlayServicesUtils {

    /**
     * Request code to attempt to resolve Google Play Services connection failures
     */
    public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /**
     * Timeout for makeing a connection to GoogleApiClient. ( in milliseconds )
     */
    public static final long CONNECTION_TIME_OUT_MS = 100;


    public static boolean checkGooglePlayServices(final Activity activity) {

        final int checkresult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity) ;
        switch (checkresult) {
            case ConnectionResult.SUCCESS:
                return true;
            case ConnectionResult.SERVICE_DISABLED:
            case ConnectionResult.SERVICE_INVALID:
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(checkresult, activity, 0);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        activity.finish();
                    }
                });
                dialog.show();
        }
        return false;
    }
}
