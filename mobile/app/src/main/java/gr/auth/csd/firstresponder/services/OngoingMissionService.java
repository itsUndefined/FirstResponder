package gr.auth.csd.firstresponder.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import gr.auth.csd.firstresponder.R;

public class OngoingMissionService extends Service {
    public OngoingMissionService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Notification.Builder notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("asdsada")
                .setSmallIcon(R.drawable.accept_mission_button)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_STATUS)
                .setOngoing(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ongoing_mission", "Ongoing Mission", NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            notification.setChannelId("ongoing_mission");
        }


        startForeground(2, notification.build());

        stopSelf();

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
