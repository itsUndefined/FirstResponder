package gr.auth.csd.firstresponder;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Constraints;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

        Button accept = view.findViewById(R.id.missionAccept);
        accept.setOnClickListener(new View.OnClickListener() {
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
                            Toast.makeText(getActivity(), "Mission accept!", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

        Button reject = view.findViewById(R.id.missionReject);
        reject.setOnClickListener(new View.OnClickListener() {
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
                            Toast.makeText(getActivity(), "Mission reject!", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

        missionTimeOut();

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
