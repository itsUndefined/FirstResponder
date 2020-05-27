package gr.auth.csd.firstresponder.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import java.util.Objects;

import gr.auth.csd.firstresponder.AlertActivity;
import gr.auth.csd.firstresponder.R;

import static gr.auth.csd.firstresponder.AlertActivity.DISPLAY_ALERT;

/**
 * This is a foreground service that creates the incoming alert notification. This service will be
 * active for 60 sec or until the user accepts the mission. It is responsible for creating the Alert Activity.
 */
public class IncomingAlertService extends Service {

    private Callback activity;

    public IncomingAlertService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent == null || !Objects.equals(intent.getAction(), START_INCOMING_ALERT)) {
            return START_STICKY;
        }

        Intent alertIntent = new Intent(getApplicationContext(), AlertActivity.class);
        alertIntent
            .putExtra(DISPLAY_ALERT, intent.getParcelableExtra(DISPLAY_ALERT))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        PendingIntent pendingAlertIntent = PendingIntent.getActivity(getApplicationContext(), 0, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notification = new Notification.Builder(getApplicationContext())
            .setContentTitle(getApplicationContext().getString(R.string.incomingAlertServiceSomeoneNeedsHelp))
            .setSmallIcon(R.drawable.icon)
            .setPriority(Notification.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_CALL)
            .setContentIntent(pendingAlertIntent)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingAlertIntent, true);


        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("alert_channel", "Incoming Alert", NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(defaultRingtoneUri, audioAttributes);
            channel.setVibrationPattern(new long[]{1000, 1000});

            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(channel);

            notification.setChannelId("alert_channel");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notification.setSound(defaultRingtoneUri, audioAttributes);
            notification.setVibrate(new long[]{1000, 1000});
        }

        Notification builtNotification = notification.build();
        builtNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        builtNotification.flags |= Notification.FLAG_INSISTENT;

        startForeground(1, builtNotification);

        startTimeoutTimer(60000);

        return START_STICKY;
    }

    public void terminateIncomingCall(boolean withActivity) {
        cancelTimeoutTimer();
        if (activity != null && withActivity) {
            activity.terminate();
        }
        stopSelf();
    }

    public void registerClient(Callback activity){
        this.activity = activity;
    }

    private void startTimeoutTimer(int remainingMS) {
        timeout = new Handler();
        timeout.postDelayed(exitCallback, remainingMS);
    }

    private void cancelTimeoutTimer() {
        timeout.removeCallbacks(exitCallback);
    }

    private Handler timeout;
    private Runnable exitCallback = new Runnable() {
        @Override
        public void run() {
            terminateIncomingCall(true);
        }
    };

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public IncomingAlertService getService() {
            return IncomingAlertService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public interface Callback {
        void terminate();
    }

    public static String START_INCOMING_ALERT = "gr.auth.csd.firstresponder.startIncomingAlert";

}
