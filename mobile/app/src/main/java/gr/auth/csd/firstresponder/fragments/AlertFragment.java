package gr.auth.csd.firstresponder.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

import gr.auth.csd.firstresponder.R;
import gr.auth.csd.firstresponder.data.Alert;
import gr.auth.csd.firstresponder.data.AlertData;
import gr.auth.csd.firstresponder.helpers.FirebaseFirestoreInstance;

import static gr.auth.csd.firstresponder.DashboardActivity.DISPLAY_ALERT;

public class AlertFragment extends Fragment {

    private FirebaseFirestore db;
    private Context context;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_alert, container, false);

        db = FirebaseFirestoreInstance.Create();
        context = getContext();
        activity = getActivity();

        final AlertData alertData = (AlertData) requireArguments().get(DISPLAY_ALERT);

        RadioGroup heavyBleedingRadio = view.findViewById(R.id.heavy_bleeding_radio_group);
        RadioGroup treatShockRadio = view.findViewById(R.id.treat_shock_radio_group);
        RadioGroup cprRadio = view.findViewById(R.id.cpr_radio_group);
        RadioGroup aedRadio = view.findViewById(R.id.aed_radio_group);

       //EditText estimatedTimeText = view.findViewById(R.id.estimated_time_text);
       // estimatedTimeText.setText(Integer.toString(alertData.secondsOfDrivingRequired));

        if (Boolean.TRUE.equals(alertData.alert.requiredSkills.get("STOP_HEAVY_BLEEDING"))) {
            heavyBleedingRadio.check(R.id.heavy_bleeding_yes);
        } else {
            heavyBleedingRadio.check(R.id.heavy_bleeding_no);
        }
        if (Boolean.TRUE.equals(alertData.alert.requiredSkills.get("TREATING_SHOCK"))) {
            treatShockRadio.check(R.id.treat_shock_yes);
        } else {
            treatShockRadio.check(R.id.treat_shock_no);
        }
        if (Boolean.TRUE.equals(alertData.alert.requiredSkills.get("CPR"))) {
            cprRadio.check(R.id.cpr_yes);
        } else {
            cprRadio.check(R.id.cpr_no);
        }
        if (Boolean.TRUE.equals(alertData.alert.requiredSkills.get("AED"))) {
            aedRadio.check(R.id.aed_yes);
        } else {
            aedRadio.check(R.id.aed_no);
        }




        final Button acceptMissionButton = view.findViewById(R.id.button_accept_mission);
        acceptMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*db.collection("pending").document(FirebaseAuth.getInstance().getUid())
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
                            }
                        });*/
                //Opening Google Maps application, navigation by foot (mode = c -> car).
                Uri gmmIntentUri = Uri.parse("google.navigation:q=40.64873,22.9615117&mode=c");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });

        Button rejectMissionButton = view.findViewById(R.id.button_reject_mission);
        rejectMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*db.collection("pending").document(FirebaseAuth.getInstance().getUid())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(0);
                            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.dashboard_activity_fragment_container, new DashboardFragment());
                            fragmentTransaction.commit();
                            Toast.makeText(getActivity(), "Η αποστολή απορρίφθηκε.", Toast.LENGTH_SHORT).show();
                        }
                    });*/
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancelAll();
                activity.finishAffinity();
            }
        });

        Button alertDetailsButton = view.findViewById(R.id.alert_details_button);
        alertDetailsButton.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Λεπτομέρειες Αποστολής");
                builder.setMessage(alertData.alert.notes);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }));

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
                    Toast.makeText(getActivity(), "Η αποστολή είναι κατειλημμένη!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

