package gr.auth.csd.firstresponder.services;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import gr.auth.csd.firstresponder.helpers.UserHelpers;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i("alert_gps", "onMessageReceived: no data yet");
        if (remoteMessage.getData().containsKey("alert")) {
            Log.i("alert_gps", "onMessageReceived: " + remoteMessage.getData().get("alert"));
            Data data = new Data.Builder().putString("alert", remoteMessage.getData().get("alert")).build();
            OneTimeWorkRequest worker = new OneTimeWorkRequest.Builder(AlertWorker.class).setInputData(data).build();
            WorkManager.getInstance(this).enqueue(worker);
        }


        /*

        Intent intent = new Intent(this, MissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Default")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Mission Alert")
                .setContentText("Tap for details")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Default", "Mission Alert", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, builder.build());

         */
    }

    @Override
    public void onNewToken(@NonNull String token) {
        UserHelpers.UpdateFirebaseInstanceId(token);
    }
}
