package gr.auth.csd.firstresponder.services;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import gr.auth.csd.firstresponder.helpers.UserHelpers;


/**
 * Receives the message and starts the AlertWorker.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().containsKey("alert")) {
            Data data = new Data.Builder().putString("alert", remoteMessage.getData().get("alert")).build();
            OneTimeWorkRequest worker = new OneTimeWorkRequest.Builder(AlertWorker.class).setInputData(data).build();
            WorkManager.getInstance(this).enqueue(worker);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        UserHelpers.UpdateFirebaseInstanceId(token);
    }
}
