package gr.auth.csd.firstresponder.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OngoingMissionService extends Service {
    public OngoingMissionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
