package gr.auth.csd.firstresponder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class AlertsService extends FirebaseMessagingService {
    public AlertsService() {
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("MESSAGE", remoteMessage.toString());
        return;
    }

    @Override
    public void onNewToken(@NonNull String s) {

    }
}
