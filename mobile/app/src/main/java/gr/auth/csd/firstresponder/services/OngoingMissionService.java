package gr.auth.csd.firstresponder.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import gr.auth.csd.firstresponder.AlertActivity;
import gr.auth.csd.firstresponder.R;

import static gr.auth.csd.firstresponder.AlertActivity.DISPLAY_ALERT;

public class OngoingMissionService extends Service {
    public OngoingMissionService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);


        Intent alertIntent = new Intent(getApplicationContext(), AlertActivity.class);
        alertIntent
            .putExtra("accepted", true)
            .putExtra(DISPLAY_ALERT, intent.getParcelableExtra(DISPLAY_ALERT))
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

        // stopSelf();

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
