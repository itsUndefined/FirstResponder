package gr.auth.csd.firstresponder.fragments;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;

import gr.auth.csd.firstresponder.DashboardActivity;
import gr.auth.csd.firstresponder.services.LocationReceiver;
import gr.auth.csd.firstresponder.MainActivity;
import gr.auth.csd.firstresponder.R;
import gr.auth.csd.firstresponder.SettingsActivity;
import gr.auth.csd.firstresponder.callbacks.DashboardFragmentCallback;
import gr.auth.csd.firstresponder.helpers.FirebaseFirestoreInstance;
import gr.auth.csd.firstresponder.helpers.PermissionsHandler;
import gr.auth.csd.firstresponder.helpers.UserHelpers;

/**
 * Creates the dashboard screen.
 */
public class DashboardFragment extends Fragment implements DashboardFragmentCallback {

    private View view;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Context context;
    private Activity activity;
    private TextView helloMessage;
    private Button pressForPerms;
    private TextView permsText;
    private TextView noGpsText;

    private ResolvableApiException resolvable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        context = getContext();
        activity = getActivity();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestoreInstance.Create();

        Toolbar toolbar = view.findViewById(R.id.toolbarW);
        helloMessage = view.findViewById(R.id.helloUser);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.aboutUsM) {
                    FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.dashboard_activity_fragment_container, new AboutUsFragment());
                    fragmentTransaction.commit();
                } else if (item.getItemId() == R.id.settingsM) {
                    Intent intent = new Intent(context, SettingsActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.logoutM) {
                    mAuth.signOut();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    activity.finishAffinity();
                }
                return false;
            }
        });

        firebaseInstanceId();

        pressForPerms = view.findViewById(R.id.press_for_fix);

        permsText = view.findViewById(R.id.noPermsView);
        noGpsText = view.findViewById(R.id.gpsOfflineView);
        noGpsText.setVisibility(View.GONE);
        pressForPerms.setVisibility(View.GONE);
        permsText.setVisibility(View.GONE);

        if (PermissionsHandler.checkLocationPermissions(context) == PackageManager.PERMISSION_DENIED) {
            pressForPerms.setVisibility(View.VISIBLE);
            permsText.setVisibility(View.VISIBLE);
        } else {
            onStartLocationTracking();
        }

        pressForPerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permsText.getVisibility() == View.VISIBLE) {
                    onStartLocationTracking();
                }
                if (noGpsText.getVisibility() == View.VISIBLE) {
                    try {
                        resolvable.startResolutionForResult(activity, DashboardActivity.REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                }
            }
        });

        if (savedInstanceState == null) {
            getUserName();
        } else {
            helloMessage.setText(savedInstanceState.getString("name"));
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build();

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(context)
            .checkLocationSettings(locationSettingsRequest)
            .addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                        // all good
                    } catch (ApiException exception) {
                        pressForPerms.setVisibility(View.VISIBLE);
                        noGpsText.setVisibility(View.VISIBLE);
                        switch (exception.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    resolvable = (ResolvableApiException) exception;
                                } catch (ClassCastException e) {
                                    // Ignore, should be an impossible error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // TODO
                                break;
                        }
                    }
                }
            });
        return view;
    }

    /**
     * If permissions are granted the request location updates.
     */
    private void onStartLocationTracking() {
        if (PermissionsHandler.checkLocationPermissions(activity) == PackageManager.PERMISSION_DENIED) {
            PermissionsHandler.requestLocationPermissions(activity);
        } else {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(600000);
            locationRequest.setFastestInterval(60000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            Intent locationIntent = new Intent(getContext(), LocationReceiver.class);
            locationIntent.setAction(LocationReceiver.LOCATION_UPDATE);

            PendingIntent locationPendingIntent =  PendingIntent.getBroadcast(getContext(), 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            fusedLocationClient.requestLocationUpdates(locationRequest, locationPendingIntent);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", helloMessage.getText().toString());
    }

    /**
     * Gets the name of the user.
     */
    private void getUserName() {
        db.collection("users").document(currentUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            String text;
                            if (documentSnapshot.exists()) {
                                text = documentSnapshot.get("firstName") +
                                        " " +
                                        documentSnapshot.get("lastName");
                            } else {
                                text = "error!";
                            }
                            helloMessage.setText(text);
                        }
                    }
                });
    }

    /**
     * Updates the current users token.
     */
    private void firebaseInstanceId() {
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (task.isSuccessful()) {
                        UserHelpers.UpdateFirebaseInstanceId(Objects.requireNonNull(task.getResult()).getToken());
                    }
                }
            });
    }

    public void onLocationEnabled() {
        pressForPerms.setVisibility(permsText.getVisibility());
        noGpsText.setVisibility(View.GONE);
    }

    public void onLocationRequestIgnored() {
        //all bad
    }

    @Override
    public void onPermissionsEnabled() {
        pressForPerms.setVisibility(noGpsText.getVisibility());
        permsText.setVisibility(View.GONE);
        onStartLocationTracking();
    }


}
