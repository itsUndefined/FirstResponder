package gr.auth.csd.firstresponder;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Constraints;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);

        Button accept = findViewById(R.id.missionAccept);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore.getInstance().collection("pending").document(FirebaseAuth.getInstance().getUid())
                        .update("isActive", true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancel(0);
                                Intent intent = new Intent(MissionActivity.this, AlertsActivity.class);
                                startActivity(intent);
                                Toast.makeText(getApplication(), "Mission accept!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        Button reject = findViewById(R.id.missionReject);
        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore.getInstance().collection("pending").document(FirebaseAuth.getInstance().getUid())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancel(0);
                                Intent intent = new Intent(MissionActivity.this, AlertsActivity.class);
                                startActivity(intent);
                                Toast.makeText(getApplication(), "Mission reject!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        missionTimeOut();
    }

    @Override
    protected void onStart() {
        super.onStart();
        missionTaken();
    }

    private void missionTimeOut() {
        Handler handler = new Handler();
        long delayInMilliseconds = 60000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseFirestore.getInstance().collection("pending").document(FirebaseAuth.getInstance().getUid())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancel(0);
                                Intent intent = new Intent(MissionActivity.this, AlertsActivity.class);
                                startActivity(intent);
                                Toast.makeText(getApplication(), "Alert time out!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }, delayInMilliseconds);
    }

    private void missionTaken() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference documentReference = db.collection("pending").document(FirebaseAuth.getInstance().getUid());
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(Constraints.TAG, "Listen failed.", e);
                    return;
                }
                if (!(documentSnapshot != null && documentSnapshot.exists())) {
                    Intent intent = new Intent(MissionActivity.this, AlertsActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplication(), "Mission taken!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
