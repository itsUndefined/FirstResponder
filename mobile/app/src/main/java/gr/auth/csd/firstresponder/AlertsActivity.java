package gr.auth.csd.firstresponder;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Constraints;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;

import gr.auth.csd.firstresponder.helpers.PermissionRequest;
import gr.auth.csd.firstresponder.helpers.PermissionsHandler;

public class AlertsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        mAuth = FirebaseAuth.getInstance();
        Button logout = findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(AlertsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        firebaseInstanceId();

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PermissionRequest.LOCATION_ONLY) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //PERM GRANTED
            } else {
                showAccessDeniedWarningMessage();
            }
        }
        if(requestCode == PermissionRequest.LOCATION_WITH_BACKGROUND) {
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //PERM GRANTED
            } else {
                showAccessDeniedWarningMessage();
            }
        }
    }

    private void showAccessDeniedWarningMessage(){
        AlertDialog.Builder fineLocationAlertBuilder = new AlertDialog.Builder(AlertsActivity.this);
        fineLocationAlertBuilder.setTitle("Warning!");
        fineLocationAlertBuilder.setMessage("Background location permission is required for the use of this application." +
                " Application will now exit");
        fineLocationAlertBuilder.setCancelable(false);
        fineLocationAlertBuilder.setPositiveButton(
            "I understand!",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertsActivity.this.finishAffinity();
                }
            }
        );

        AlertDialog alertDialog = fineLocationAlertBuilder.create();
        alertDialog.show();
    }


    public void onRequestPermission(View view) {
        if (PermissionsHandler.checkLocationPermissions(this) == PackageManager.PERMISSION_DENIED) {
            PermissionsHandler.requestLocationPermissions(this);
        } else {
            //Location services available
        }
    }

    private void firebaseInstanceId() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(Constraints.TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        HashMap<String, Object> user = new HashMap<>();
                        user.put("token", task.getResult().getToken());
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(FirebaseAuth.getInstance().getUid())
                                .update(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(Constraints.TAG, "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(Constraints.TAG, "Error writing document", e);
                                    }
                                });
                    }
                });
    }
}
