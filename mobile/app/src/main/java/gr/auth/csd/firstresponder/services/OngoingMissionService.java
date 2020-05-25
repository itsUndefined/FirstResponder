package gr.auth.csd.firstresponder.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import gr.auth.csd.firstresponder.AlertActivity;
import gr.auth.csd.firstresponder.LocationReceiver;
import gr.auth.csd.firstresponder.R;
import gr.auth.csd.firstresponder.data.AlertData;
import gr.auth.csd.firstresponder.helpers.PermissionsHandler;

import static gr.auth.csd.firstresponder.AlertActivity.DISPLAY_ALERT;

public class OngoingMissionService extends Service {
    public OngoingMissionService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        final AlertData alertData = intent.getParcelableExtra(DISPLAY_ALERT);

        Intent alertIntent = new Intent(getApplicationContext(), AlertActivity.class);
        alertIntent
            .putExtra("accepted", true)
            .putExtra(DISPLAY_ALERT, alertData)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        PendingIntent pendingAlertIntent = PendingIntent.getActivity(getApplicationContext(), 0, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("asdsada")
                .setSmallIcon(R.drawable.accept_mission_button)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_STATUS)
                .setContentIntent(pendingAlertIntent)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ongoing_mission", "Ongoing Mission", NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            notification.setChannelId("ongoing_mission");
        }


        startForeground(2, notification.build());


        if (PermissionsHandler.checkLocationPermissions(getApplicationContext()) == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Location alertLocation = new Location("placeholder");
                        alertLocation.setLatitude(alertData.alert.coordinates.getLatitude());
                        alertLocation.setLongitude(alertData.alert.coordinates.getLongitude());
                        float apostasi = location.distanceTo(alertLocation);
                        return;
                    }
                }
            }, null);
        }
        // stopSelf();

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
