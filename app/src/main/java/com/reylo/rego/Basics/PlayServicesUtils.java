package com.reylo.rego.Basics;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class PlayServicesUtils {

    // ensure google play services are connected
    public static boolean checkGooglePlaySevices(final Activity activity) {

        //get instance of google play services / check if services are available
        final int googlePlayServicesCheck = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);

        //if success, return true, else return false and create dialog to display error
        switch (googlePlayServicesCheck) {

            case ConnectionResult.SUCCESS:
                return true;
            case ConnectionResult.SERVICE_DISABLED:
            case ConnectionResult.SERVICE_INVALID:
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googlePlayServicesCheck, activity, 0);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        activity.finish();
                    }
                });
                dialog.show();

        }

        return false;

    }

}