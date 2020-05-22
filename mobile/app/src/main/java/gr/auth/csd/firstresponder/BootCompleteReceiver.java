package gr.auth.csd.firstresponder;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import gr.auth.csd.firstresponder.helpers.PermissionsHandler;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

                if (PermissionsHandler.checkLocationPermissions(context) == PackageManager.PERMISSION_GRANTED) {

                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(5000);
                    locationRequest.setFastestInterval(5000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                    Intent locationIntent = new Intent(context, LocationReceiver.class);
                    locationIntent.setAction(LocationReceiver.LOCATION_UPDATE);

                    PendingIntent locationPendingIntent = PendingIntent.getBroadcast(context, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    final PendingResult asyncTask = goAsync();

                    fusedLocationClient.requestLocationUpdates(locationRequest, locationPendingIntent).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            asyncTask.finish();
                        }
                    });
                }
            }
        }
    }
}
