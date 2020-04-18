package gr.auth.csd.firstresponder.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;

public final class PermissionsHandler {
    public static int checkLocationPermissions(Context context) {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) |
                context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermissions(Activity activity) {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PermissionRequest.LOCATION_WITH_BACKGROUND);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PermissionRequest.LOCATION_ONLY);
            return;
        }
    }
}

