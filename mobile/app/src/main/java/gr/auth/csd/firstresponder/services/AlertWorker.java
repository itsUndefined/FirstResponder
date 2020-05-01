package gr.auth.csd.firstresponder.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Location;
import android.os.Build;
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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import gr.auth.csd.firstresponder.R;

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
        final SettableFuture<Result> future = SettableFuture.create();
        String inputData = getInputData().getString("alert");
        setForegroundAsync(createForegroundInfo());

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);

        LocationCallback locationListener = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d("alert_gps", "GPS location result null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d("alert_gps", "GPS accuracy: " + location.getAccuracy());
                        Log.d("alert_gps", "GPS speed: " + location.getSpeed());
                        Log.d("alert_gps", "GPS altitude: " + location.getAltitude());
                    } else {
                        Log.d("alert_gps", "GPS location null");
                    }
                }
                future.set(Result.success());
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
        // Build a notification using bytesRead and contentLength

        Context context = getApplicationContext();
        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("my_important_channel", "My Background Service");
        } else {
            channelId = "";
        }
        String title = "Is this working?";

       // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //    createChannel();
        //}

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
