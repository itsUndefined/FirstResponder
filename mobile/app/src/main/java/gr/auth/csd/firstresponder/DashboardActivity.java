package gr.auth.csd.firstresponder;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import gr.auth.csd.firstresponder.callbacks.DashboardActivityCallback;
import gr.auth.csd.firstresponder.fragments.DashboardFragment;
import gr.auth.csd.firstresponder.helpers.PermissionRequest;
import gr.auth.csd.firstresponder.helpers.PermissionsHandler;

public class DashboardActivity extends AppCompatActivity implements DashboardActivityCallback {

    private Context context;
    private Button button;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
            return;
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.dashboard_activity_fragment_container, new DashboardFragment());
        fragmentTransaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PermissionRequest.LOCATION_ONLY) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRequestPermission();
            } else {
                showAccessDeniedWarningMessage();
            }
        }
        if(requestCode == PermissionRequest.LOCATION_WITH_BACKGROUND) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                onRequestPermission();
            } else {
                showAccessDeniedWarningMessage();
            }
        }
    }

    private void onRequestPermission() {
        if (PermissionsHandler.checkLocationPermissions(context) == PackageManager.PERMISSION_DENIED) {
            PermissionsHandler.requestLocationPermissions(this);
        } else {
            button.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(60000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            Intent locationIntent = new Intent(getApplicationContext(), LocationReceiver.class);
            locationIntent.setAction(LocationReceiver.LOCATION_UPDATE);

            PendingIntent locationPendingIntent =  PendingIntent.getBroadcast(getApplicationContext(), 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            fusedLocationClient.requestLocationUpdates(locationRequest, locationPendingIntent);
        }
    }

    private void showAccessDeniedWarningMessage(){
        AlertDialog.Builder fineLocationAlertBuilder = new AlertDialog.Builder(context);
        fineLocationAlertBuilder.setTitle("Warning!");
        fineLocationAlertBuilder.setMessage("Background location permission is required for the use of this application." +
                " Application will now exit");
        fineLocationAlertBuilder.setCancelable(false);
        fineLocationAlertBuilder.setPositiveButton(
                "I understand!",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                }
        );

        AlertDialog alertDialog = fineLocationAlertBuilder.create();
        alertDialog.show();
    }

    @Override
    public void getPermissions(Context context) {
        this.context = context;
        onRequestPermission();
    }

    @Override
    public void setPermissionsGUI(Button button, TextView textView) {
        this.button = button;
        this.textView = textView;
    }
}
