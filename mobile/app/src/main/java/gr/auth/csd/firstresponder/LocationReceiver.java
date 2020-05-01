package gr.auth.csd.firstresponder;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import gr.auth.csd.firstresponder.helpers.PermissionsHandler;

public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setHost("10.0.2.2:8080")
                .setSslEnabled(false)
                .setPersistenceEnabled(false)
                .build();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);



        if (intent.getAction() != null && intent.getAction().equals(LOCATION_UPDATE)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            if(locationResult != null) {
                Location location = locationResult.getLastLocation();
                if (location != null && currentUser != null) {
                    final PendingResult asyncTask = goAsync();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Map<String, Object> data = new HashMap<>();
                    data.put("location", geoPoint);
                    data.put("time", Calendar.getInstance().getTime());
                    db.collection("users")
                        .document(currentUser.getUid())
                        .update("lastKnownLocation", data)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                asyncTask.finish();
                            }
                        });
                }
            }
        }
    }

    public static String LOCATION_UPDATE = "gr.auth.csd.firstresponder.action.LOCATION_UPDATE";
}
