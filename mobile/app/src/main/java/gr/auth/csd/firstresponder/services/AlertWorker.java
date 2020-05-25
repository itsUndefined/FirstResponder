package gr.auth.csd.firstresponder.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import gr.auth.csd.firstresponder.AlertActivity;
import gr.auth.csd.firstresponder.DashboardActivity;
import gr.auth.csd.firstresponder.R;
import gr.auth.csd.firstresponder.data.Alert;
import gr.auth.csd.firstresponder.data.AlertData;
import gr.auth.csd.firstresponder.helpers.FirebaseFirestoreInstance;
import gr.auth.csd.firstresponder.helpers.FirebaseFunctionsInstance;

import static gr.auth.csd.firstresponder.AlertActivity.DISPLAY_ALERT;

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
                    future.set(Result.success());
                    return;
                }
                for (final Location location : locationResult.getLocations()) {
                    if (location == null) {
                        future.set(Result.success());
                        return;
                    }
                    Log.d("alert_gps", "GPS accuracy: " + location.getAccuracy());
                    Log.d("alert_gps", "GPS speed: " + location.getSpeed());
                    Log.d("alert_gps", "GPS altitude: " + location.getAltitude());


                    FirebaseFirestoreInstance.Create().collection("alerts").document(alertId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (!task.isSuccessful()) {
                                future.set(Result.success());
                                return;
                            }
                            DocumentSnapshot document = Objects.requireNonNull(task.getResult());
                            if (!document.exists()) {
                                future.set(Result.success());
                                return;
                            }
                            final Alert incomingAlert = Objects.requireNonNull(document.toObject(Alert.class));
                            final GeoPoint alertLocation = incomingAlert.coordinates;

                            Thread thread = new Thread(new Runnable(){
                                @Override
                                public void run(){
                                    try {
                                        Bundle bundle = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA).metaData;
                                        String key = bundle.getString("com.google.android.geo.API_KEY");
                                        String origin = "" + location.getLatitude() + ',' + location.getLongitude();
                                        String destination = "" + alertLocation.getLatitude() + ',' + alertLocation.getLongitude();


                                        URL url = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?departure_time=now&origins=" + origin + "&destinations=" + destination + "&key=" + key);
                                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                                        StringBuilder sb = new StringBuilder();
                                        String output;
                                        while ((output = br.readLine()) != null) {
                                            sb.append(output);
                                        }

                                        final int secondsOfDrivingRequired = new JSONObject(sb.toString())
                                            .getJSONArray("rows")
                                            .getJSONObject(0)
                                            .getJSONArray("elements")
                                            .getJSONObject(0)
                                            .getJSONObject("duration_in_traffic")
                                            .getInt("value");

                                        final boolean shouldAlertUser;

                                        Map<String, Object> data = new HashMap<>();
                                        Map<String, Object> locationJSON = new HashMap<>();
                                        locationJSON.put("latitude", location.getLatitude());
                                        locationJSON.put("longitude", location.getLongitude());
                                        data.put("knownLocation", locationJSON);
                                        data.put("alertId", alertId);
                                        if (secondsOfDrivingRequired < 600) {
                                            data.put("status", "awaiting");
                                            shouldAlertUser = true;
                                        } else {
                                            data.put("status", "too_far");
                                            shouldAlertUser = false;
                                        }
                                        FirebaseFunctions functionsInstance = FirebaseFunctionsInstance.Create();

                                        functionsInstance.getHttpsCallable("updateUserStatus").call(data).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                                if (!task.isSuccessful()) {
                                                    future.set(Result.success());
                                                    return;
                                                }

                                                if (shouldAlertUser) {
                                                    Context context = getApplicationContext();

                                                    Intent alertIntent = new Intent(context, AlertActivity.class);
                                                    AlertData alertData = new AlertData();
                                                    alertData.alert = incomingAlert;
                                                    alertData.alertId = alertId;
                                                    alertData.secondsOfDrivingRequired = secondsOfDrivingRequired;
                                                    alertIntent
                                                        .putExtra(DISPLAY_ALERT, alertData)
                                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

                                                    PendingIntent pendingAlertIntent = PendingIntent.getActivity(context, 0, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                                    Notification.Builder notification = new Notification.Builder(context)
                                                        .setContentTitle("SOMEONE NEEDS HELP RIGHT NOW")
                                                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                                                        .setPriority(Notification.PRIORITY_MAX)
                                                        .setCategory(Notification.CATEGORY_CALL)
                                                        .setOngoing(true)
                                                        .setContentIntent(pendingAlertIntent)
                                                        .setFullScreenIntent(pendingAlertIntent, true);

                                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        notification.setChannelId(createNotificationChannel("alert_channel", "Alerts", true));
                                                    }

                                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                                                        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
                                                        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build();
                                                        notification.setSound(defaultRingtoneUri, audioAttributes);
                                                    }

                                                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                                    manager.notify(1, notification.build());
                                                }

                                                future.set(Result.success());
                                            }
                                        });
                                    } catch (PackageManager.NameNotFoundException | IOException | JSONException e) {
                                        future.set(Result.success());
                                        e.printStackTrace();
                                    }
                                }
                            });
                            thread.start();
                        }
                    });
                }
            }
        };

        new FusedLocationProviderClient(getApplicationContext()).requestLocationUpdates(locationRequest, locationListener, null);

        return future;
    }

    @NonNull
    private ForegroundInfo createForegroundInfo() {

        Context context = getApplicationContext();
        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("gps_fix_notification", "Location service updates", false);
        } else {
            channelId = "";
        }
        String title = "Someone needs help!";

        Notification notification = new NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText("We are confirming your location")
            .setTicker(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build();

        return new ForegroundInfo(42, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName, boolean highPriority) {
        NotificationChannel channel;
        if (highPriority) {
            channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        } else {
            channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        }


        if (highPriority) {
            Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build();
            channel.setSound(defaultRingtoneUri, audioAttributes);
        }





        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);
        return channelId;
    }
}