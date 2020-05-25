package gr.auth.csd.firstresponder.fragments;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;

import gr.auth.csd.firstresponder.AlertActivity;
import gr.auth.csd.firstresponder.LocationReceiver;
import gr.auth.csd.firstresponder.MainActivity;
import gr.auth.csd.firstresponder.R;
import gr.auth.csd.firstresponder.SettingsActivity;
import gr.auth.csd.firstresponder.data.Alert;
import gr.auth.csd.firstresponder.helpers.FirebaseFirestoreInstance;
import gr.auth.csd.firstresponder.helpers.PermissionRequest;
import gr.auth.csd.firstresponder.helpers.PermissionsHandler;
import gr.auth.csd.firstresponder.helpers.UserHelpers;

public class DashboardFragment extends Fragment {

    private View view;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Context context;
    private Activity activity;
    private TextView helloMessage;

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
                    startActivity(intent);
                }
                return false;
            }
        });

        firebaseInstanceId();

        Button pressForPerms = view.findViewById(R.id.press_for_perms);
        pressForPerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRequestPermission();
            }
        });

        if (savedInstanceState == null) {
            getUserName();
        } else {
            helloMessage.setText(savedInstanceState.getString("name"));
        }
        return view;
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", helloMessage.getText().toString());
    }

    /*@Override
    public void onStart() {
        super.onStart();
        alertForMission();
    }*/

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
                        activity.finishAffinity();
                    }
                }
        );

        AlertDialog alertDialog = fineLocationAlertBuilder.create();
        alertDialog.show();
    }

    private void onRequestPermission() {
        if (PermissionsHandler.checkLocationPermissions(context) == PackageManager.PERMISSION_DENIED) {
            PermissionsHandler.requestLocationPermissions(activity);
        } else {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(60000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            Intent locationIntent = new Intent(getContext(), LocationReceiver.class);
            locationIntent.setAction(LocationReceiver.LOCATION_UPDATE);

            PendingIntent locationPendingIntent =  PendingIntent.getBroadcast(getContext(), 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            fusedLocationClient.requestLocationUpdates(locationRequest, locationPendingIntent);
        }
    }

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

    private void alertForMission() {
        /*
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference documentReference = db.collection("pending").document(FirebaseAuth.getInstance().getUid());
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(Constraints.TAG, "Listen failed.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    alert.setVisibility(View.VISIBLE);
                    FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.dashboard_activity_fragment_container, new AlertFragment());
                    fragmentTransaction.commit();
                } else {
                    alert.setVisibility(View.GONE);
                }
            }
        });
         */
    }
}
