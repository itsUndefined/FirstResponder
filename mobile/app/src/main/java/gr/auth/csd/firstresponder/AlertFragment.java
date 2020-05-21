package gr.auth.csd.firstresponder;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Constraints;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.content.pm.PackageManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import gr.auth.csd.firstresponder.helpers.FirebaseFirestoreInstance;

public class AlertFragment extends Fragment {

    private FirebaseFirestore db;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alert, container, false);

        db = FirebaseFirestoreInstance.Create();
        context = getContext();

        final Button acceptMissionButton = view.findViewById(R.id.button_accept_mission);
        acceptMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("pending").document(FirebaseAuth.getInstance().getUid())
                        .update("isActive", true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancel(0);
                                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.dashboard_activity_fragment_container, new MissionFragment());
                                fragmentTransaction.commit();
                                Toast.makeText(getActivity(), "Mission accepted!", Toast.LENGTH_SHORT).show();

                                //Opening Google Maps application, navigation by foot (mode = w -> walking).
                                Uri gmmIntentUri = Uri.parse("google.navigation:q=46.414382,10.013988&mode=w");
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");
                                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                    acceptMissionButton.setEnabled(false);
                                }
                            }
                        });
            }
        });

        Button rejectMissionButton = view.findViewById(R.id.button_reject_mission);
        rejectMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("pending").document(FirebaseAuth.getInstance().getUid())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancel(0);
                                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.dashboard_activity_fragment_container, new DashboardFragment());
                                fragmentTransaction.commit();
                                Toast.makeText(getActivity(), "Mission rejected.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        //missionTimeOut();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.dashboard_activity_fragment_container, new AlertFragment());
            fragmentTransaction.commit();
        }
    }

    private void missionTimeOut() {
        Handler handler = new Handler();
        long delayInMilliseconds = 60000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                db.collection("pending").document(FirebaseAuth.getInstance().getUid())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancel(0);
                                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.dashboard_activity_fragment_container, new DashboardFragment());
                                fragmentTransaction.commit();
                                Toast.makeText(getActivity(), "Alert time out!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }, delayInMilliseconds);
    }

    private void missionTaken() {
        final DocumentReference documentReference = db.collection("pending").document(FirebaseAuth.getInstance().getUid());
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(Constraints.TAG, "Listen failed.", e);
                    return;
                }
                if (!(documentSnapshot != null && documentSnapshot.exists())) {
                    FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.dashboard_activity_fragment_container, new DashboardFragment());
                    fragmentTransaction.commit();
                    Toast.makeText(getActivity(), "Mission taken!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

