package gr.auth.csd.firstresponder.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import gr.auth.csd.firstresponder.AlertsActivity;

public final class PermissionsHandler {
    public static int checkLocationPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) |
                    context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermissions(Activity activity, boolean accessGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if((ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED)
            && !accessGranted) {
                showAccessDeniedWarningMessage(activity);
                return;
            }
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PermissionRequest.LOCATION_WITH_BACKGROUND);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
            && !accessGranted) {
                showAccessDeniedWarningMessage(activity);
                return;
            }
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PermissionRequest.LOCATION_ONLY);
            return;
        }
    }

    private static void showAccessDeniedWarningMessage(final Activity activity) {
        AlertDialog.Builder fineLocationAlertBuilder = new AlertDialog.Builder(activity);
        fineLocationAlertBuilder.setTitle("Warning!");
        fineLocationAlertBuilder.setMessage(
                "Location permission is required for the use of this application." +
                " In case you deny permission, the app will exit.");
        fineLocationAlertBuilder.setCancelable(false);
        fineLocationAlertBuilder.setPositiveButton(
                "I understand",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestLocationPermissions(activity, true);
                    }
                });

        AlertDialog alertDialog = fineLocationAlertBuilder.create();
        alertDialog.show();
    }
}
