package gr.auth.csd.firstresponder.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Objects;

import gr.auth.csd.firstresponder.AlertActivity;
import gr.auth.csd.firstresponder.R;
import gr.auth.csd.firstresponder.data.AlertData;
import gr.auth.csd.firstresponder.helpers.PermissionsHandler;

import static gr.auth.csd.firstresponder.AlertActivity.DISPLAY_ALERT;

/**
 * This foreground service will be launched after user accepts the mission. The service gets
 * the current location every 5sec and checks if the user is in a range of 50m of the alert. Then
 * the service alerts the activity in order to show the complete mission button.
 */
public class OngoingMissionService extends Service {
    private OngoingMissionService.Callback activity;
    private LocationCallback callback;
    private FusedLocationProviderClient fusedLocationClient;

    public OngoingMissionService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent == null || !Objects.equals(intent.getAction(), START_MISSION)) {
            return START_STICKY;
        }

        final AlertData alertData = intent.getParcelableExtra(DISPLAY_ALERT);

        Intent alertIntent = new Intent(getApplicationContext(), AlertActivity.class);
        alertIntent
            .putExtra("accepted", true)
            .putExtra(DISPLAY_ALERT, alertData)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        PendingIntent pendingAlertIntent = PendingIntent.getActivity(getApplicationContext(), 0, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getApplicationContext().getString(R.string.ongoingMission))
                .setSmallIcon(R.drawable.icon)
                .setPriority(Notification.PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_STATUS)
                .setContentIntent(pendingAlertIntent)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ongoing_mission", "Ongoing Mission", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(channel);

            notification.setChannelId("ongoing_mission");
        }

        startForeground(2, notification.build());

        if (PermissionsHandler.checkLocationPermissions(getApplicationContext()) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            callback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Location alertLocation = new Location("placeholder");
                        alertLocation.setLatitude(alertData.alert.coordinates.getLatitude());
                        alertLocation.setLongitude(alertData.alert.coordinates.getLongitude());
                        float distanceToAlert = location.distanceTo(alertLocation);
                        if (distanceToAlert < 50) {
                            activity.onArrival();
                        }
                    }
                }
            };
            fusedLocationClient.requestLocationUpdates(locationRequest, callback, null);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(callback);
    }

    public void registerClient(OngoingMissionService.Callback activity){
        this.activity = activity;
    }

    private final IBinder binder = new OngoingMissionService.LocalBinder();

    public class LocalBinder extends Binder {
        public OngoingMissionService getService() {
            return OngoingMissionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public interface Callback {
        void onArrival();
    }

    public static String START_MISSION = "gr.auth.csd.firstresponder.startMission";

}
