package gr.auth.csd.firstresponder;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Constraints;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import gr.auth.csd.firstresponder.helpers.PermissionRequest;
import gr.auth.csd.firstresponder.helpers.PermissionsHandler;

public class DashboardFragment extends Fragment {

    private FirebaseAuth mAuth;
    private Button allert;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mAuth = FirebaseAuth.getInstance();
        Button logout = view.findViewById(R.id.logout);
        allert = view.findViewById(R.id.alertButton);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });
        firebaseInstanceId();

        Button settings = view.findViewById(R.id.settingsButton);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        alertForMission();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.dashboard_activity_fragment_container, new DashboardFragment());
            fragmentTransaction.commit();
        }
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
        AlertDialog.Builder fineLocationAlertBuilder = new AlertDialog.Builder(getActivity());
        fineLocationAlertBuilder.setTitle("Warning!");
        fineLocationAlertBuilder.setMessage("Background location permission is required for the use of this application." +
                " Application will now exit");
        fineLocationAlertBuilder.setCancelable(false);
        fineLocationAlertBuilder.setPositiveButton(
                "I understand!",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finishAffinity();
                    }
                }
        );

        AlertDialog alertDialog = fineLocationAlertBuilder.create();
        alertDialog.show();
    }

    public void onRequestPermission(View view) {
        if (PermissionsHandler.checkLocationPermissions(getActivity()) == PackageManager.PERMISSION_DENIED) {
            PermissionsHandler.requestLocationPermissions(getActivity());
        } else {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(60000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            Intent locationIntent = new Intent(this, LocationReceiver.class);
            locationIntent.setAction(LocationReceiver.LOCATION_UPDATE);

            PendingIntent locationPendingIntent =  PendingIntent.getBroadcast(this, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            fusedLocationClient.requestLocationUpdates(locationRequest, locationPendingIntent);
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
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(FirebaseAuth.getInstance().getUid())
                                .update("token", task.getResult().getToken())
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

    private void alertForMission() {
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
                    allert.setVisibility(View.VISIBLE);
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.dashboard_activity_fragment_container, new AlertFragment());
                    fragmentTransaction.commit();
                } else {
                    allert.setVisibility(View.GONE);
                }
            }
        });
    }
}
