package gr.auth.csd.firstresponder.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import gr.auth.csd.firstresponder.callbacks.DashboardActivityCallback;
import gr.auth.csd.firstresponder.MainActivity;
import gr.auth.csd.firstresponder.R;
import gr.auth.csd.firstresponder.SettingsActivity;
import gr.auth.csd.firstresponder.helpers.FirebaseFirestoreInstance;
import gr.auth.csd.firstresponder.helpers.PermissionsHandler;
import gr.auth.csd.firstresponder.helpers.UserHelpers;

public class DashboardFragment extends Fragment {

    private DashboardActivityCallback dashboardActivityCallback;
    private View view;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Context context;
    private Activity activity;
    private TextView helloMessage;
    private Button pressForPerms;
    private TextView permsText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        dashboardActivityCallback = (DashboardActivity) getActivity();

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
                    getActivity().finish();
                }
                return false;
            }
        });

        firebaseInstanceId();

        pressForPerms = view.findViewById(R.id.press_for_perms);
        permsText = view.findViewById(R.id.permsText);
        if (PermissionsHandler.checkLocationPermissions(context) == PackageManager.PERMISSION_GRANTED) {
            pressForPerms.setVisibility(View.GONE);
            permsText.setVisibility(View.GONE);
        }

        pressForPerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dashboardActivityCallback.setPermissionsGUI(pressForPerms, permsText);
                dashboardActivityCallback.getPermissions(context);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", helloMessage.getText().toString());
    }

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
}
