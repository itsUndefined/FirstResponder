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

/**
 * This class is used after API level 24 and handles the runtime location permissions.
 */
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

    public static void requestLocationPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ||
            ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED)
        ) {
            explainLocationPermissionRequirements(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
        ) {
            explainLocationPermissionRequirements(activity);
        }
    }

    private static void _requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PermissionRequest.LOCATION_WITH_BACKGROUND);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PermissionRequest.LOCATION_ONLY);
        }
    }

    private static void explainLocationPermissionRequirements(final Activity activity) {
        AlertDialog.Builder fineLocationAlertBuilder = new AlertDialog.Builder(activity);
        fineLocationAlertBuilder.setTitle("Προειδοποίηση!");
        fineLocationAlertBuilder.setMessage(
                "Απαιτείται αποδοχή της λήψης τοποθεσίας στο παρασκήνιο για την λειτουργία της εφαρμογής." +
                " Σε περίπτωση που αρνηθείτε τότε η εφαρμογή θα κλείσει.");
        fineLocationAlertBuilder.setCancelable(false);
        fineLocationAlertBuilder.setPositiveButton(
            "Αποδοχή",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    _requestPermission(activity);
                }
            }
        );
        AlertDialog alertDialog = fineLocationAlertBuilder.create();
        alertDialog.show();
    }
}
