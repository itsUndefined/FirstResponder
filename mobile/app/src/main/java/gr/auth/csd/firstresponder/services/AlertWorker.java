package gr.auth.csd.firstresponder.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;

import java.util.Objects;

import gr.auth.csd.firstresponder.R;
import gr.auth.csd.firstresponder.helpers.FirebaseFirestoreInstance;

public class AlertWorker extends ListenableWorker {
    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public AlertWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }



    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        Log.i("alert_gps", "Starting work");
        final SettableFuture<Result> future = SettableFuture.create();

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.i("alert_gps", "No provider");
            future.set(Result.success());
            return future;
        }


        final String alertId = getInputData().getString("alert");
        setForegroundAsync(createForegroundInfo());

        Log.i("alert_gps", "Started foreground");

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);
        locationRequest.setExpirationDuration(30000); // 30 seconds maximum waiting

        final Handler timeout = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                Log.i("alert_gps", "GPS timed out");
                future.set(Result.success());
            }
        };
        timeout.postDelayed(r, 30000);

        LocationCallback locationListener = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                timeout.removeCallbacks(r);
                if (locationResult == null) {
                    Log.d("alert_gps", "GPS location result null");
                    return;
                }
                for (final Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d("alert_gps", "GPS accuracy: " + location.getAccuracy());
                        Log.d("alert_gps", "GPS speed: " + location.getSpeed());
                        Log.d("alert_gps", "GPS altitude: " + location.getAltitude());
                        FirebaseFirestoreInstance.Create().collection("alerts").document(alertId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()) {
                                    DocumentSnapshot document = Objects.requireNonNull(task.getResult());
                                    GeoPoint alertLocation = Objects.requireNonNull(document.getGeoPoint("coordinates"));
                                    try {
                                        Bundle bundle = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA).metaData;
                                        String key = bundle.getString("com.google.android.geo.API_KEY");
                                        GeoApiContext context = new GeoApiContext.Builder().apiKey(key).build();

                                        String[] origin = new String[] {
                                            "" + location.getLatitude() + ',' + location.getLongitude()
                                        };

                                        String[] destination = new String[] {
                                            "" + alertLocation.getLatitude() + ',' + alertLocation.getLongitude()
                                        };

                                        DistanceMatrix distanceMatrix =DistanceMatrixApi.getDistanceMatrix(context, origin, destination).awaitIgnoreError();


                                        future.set(Result.success());
                                    } catch (PackageManager.NameNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    } else {
                        Log.d("alert_gps", "GPS location null");
                    }
                }
            }
        };

        new FusedLocationProviderClient(getApplicationContext()).requestLocationUpdates(locationRequest, locationListener, null);


        /*

        final AtomicInteger count = new AtomicInteger(0);

        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                count.incrementAndGet();
                Log.i("interval", "This function is called every 10 seconds. Yes I am still alive!");
                if(count.get() == 4) {
                    Log.i("interval", "Ι run for 30 seconds while dozing. I bypassed the fucker called android");
                    future.set(Result.success());
                }
            }
        },0,10000);

         */

        return future;
    }

    @NonNull
    private ForegroundInfo createForegroundInfo() {

        Context context = getApplicationContext();
        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("my_important_channel", "My Background Service");
        } else {
            channelId = "";
        }
        String title = "Is this working?";

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText("Is the main content working?")
                .setTicker(title)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build();

        return new ForegroundInfo(42, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);
        return channelId;
    }
}
