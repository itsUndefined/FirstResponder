package gr.auth.csd.firstresponder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import gr.auth.csd.firstresponder.data.Responder;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class RegisterFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        final EditText fName = view.findViewById(R.id.firstNameR);
        final EditText lName = view.findViewById(R.id.lastNameR);
        final CheckBox hb = view.findViewById(R.id.hbR);
        final CheckBox ts = view.findViewById(R.id.tsR);
        final CheckBox cpr = view.findViewById(R.id.cprR);
        final CheckBox d = view.findViewById(R.id.dR);
        Button submit = view.findViewById(R.id.registerB);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fName.getText().toString().matches("") || lName.getText().toString().matches("")) {
                    if (fName.getText().toString().matches("")) {
                        Toast.makeText(getActivity(), "Invalid first name", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Invalid last name", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    HashMap<String, Boolean> skills = new HashMap<>();
                    // User can stop heavy bleeding.
                    if (hb.isChecked()) { skills.put("STOP_HEAVY_BLEEDING", true); } else {skills.put("STOP_HEAVY_BLEEDING", false); }
                    // User can treat shock.
                    if (ts.isChecked()) { skills.put("TREATING_SHOCK", true); } else {skills.put("TREATING_SHOCK", false) ; }
                    // User knows how to perform CPR.
                    if (cpr.isChecked()) { skills.put("CPR", true); } else {skills.put("CPR", false); }
                    // User knows how to use a defibrillator.
                    if (d.isChecked()) { skills.put("AED", true); } else {skills.put("AED", false); }

                    Responder responder = new Responder(fName.getText().toString(), lName.getText().toString(), skills);

                    db.collection("users").document(currentUser.getUid())
                            .set(responder)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                    Intent intent = new Intent(getActivity(), DashboardActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_activity_fragment_container, new RegisterFragment());
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
