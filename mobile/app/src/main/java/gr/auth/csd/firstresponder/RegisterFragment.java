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

import java.util.ArrayList;
import java.util.HashMap;

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
                    HashMap<String, Object> user = new HashMap<>();
                    ArrayList<Integer> skills = new ArrayList<>();
                    user.put("firstName", fName.getText().toString());
                    user.put("lastName", lName.getText().toString());
                    if (hb.isChecked()) {
                        skills.add(0);
                    }
                    if (ts.isChecked()) {
                        skills.add(1);
                    }
                    if (cpr.isChecked()) {
                        skills.add(2);
                    }
                    if (d.isChecked()) {
                        skills.add(3);
                    }
                    user.put("skills", skills);
                    db.collection("users").document(currentUser.getUid())
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                    Intent intent = new Intent(getActivity(), AlertsActivity.class);
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
