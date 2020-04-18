package gr.auth.csd.firstresponder;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PermissionRequest.LOCATION_ONLY) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //PERM GRANTED
            } else {
                //PERM NOT GRANTED SHOW SOMETHING FOR THE USER HE IS AN IDIOT
            }
        }
        if(requestCode == PermissionRequest.LOCATION_WITH_BACKGROUND) {
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //PERM GRANTED
            } else {
                //PERM NOT GRANTED SHOW SOMETHING FOR THE USER HE IS AN IDIOT
            }
        }
    }

    public void onRequestPermission(View view) {
        if (PermissionsHandler.checkLocationPermissions(this) == PackageManager.PERMISSION_DENIED) {
            PermissionsHandler.requestLocationPermissions(this);
        } else {
            //Location services available
        }
    }
}
