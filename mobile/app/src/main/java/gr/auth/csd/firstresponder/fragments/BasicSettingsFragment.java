package gr.auth.csd.firstresponder.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import gr.auth.csd.firstresponder.DashboardActivity;
import gr.auth.csd.firstresponder.R;
import gr.auth.csd.firstresponder.data.Responder;
import gr.auth.csd.firstresponder.helpers.FirebaseFirestoreInstance;

/**
 * Creates the settings screen.
 */
public class BasicSettingsFragment extends Fragment {

    private Responder user;

    private FirebaseUser currentUser;
    private FirebaseFirestore database;

    private TextInputLayout name;
    private TextInputLayout surname;
    private CheckBox heavyBleedingCheckbox;
    private CheckBox treatShockCheckbox;
    private CheckBox cprCheckbox;
    private CheckBox aedCheckbox;
    private Button saveChangesButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basic_settings, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseFirestoreInstance.Create();

        name = view.findViewById(R.id.namePlainText);
        surname = view.findViewById(R.id.surnamePlainText);
        heavyBleedingCheckbox = view.findViewById(R.id.heavyBleedingCheckBox);
        treatShockCheckbox = view.findViewById(R.id.treatShockCheckbox);
        cprCheckbox = view.findViewById(R.id.cprCheckbox);
        aedCheckbox = view.findViewById(R.id.aedCheckbox);
        saveChangesButton = view.findViewById(R.id.settingSaveChangesButton);

        user = new Responder();

        Button returnToLogInActivityArrow = view.findViewById(R.id.basicSettingsBack);
        returnToLogInActivityArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });

        if (savedInstanceState == null) {
            readFieldsFromDatabase();
        } else {
            name.getEditText().setText(savedInstanceState.getString("name"));
            surname.getEditText().setText(savedInstanceState.getString("surname"));
            heavyBleedingCheckbox.setChecked(savedInstanceState.getBoolean("heavyBleedingCheckbox"));
            treatShockCheckbox.setChecked(savedInstanceState.getBoolean("treatShockCheckbox"));
            cprCheckbox.setChecked(savedInstanceState.getBoolean("cprCheckbox"));
            aedCheckbox.setChecked(savedInstanceState.getBoolean("aedCheckbox"));
        }
        initializeButton();

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", name.getEditText().getText().toString());
        outState.putString("surname", surname.getEditText().getText().toString());
        outState.putBoolean("heavyBleedingCheckbox", heavyBleedingCheckbox.isChecked());
        outState.putBoolean("treatShockCheckbox", treatShockCheckbox.isChecked());
        outState.putBoolean("cprCheckbox", cprCheckbox.isChecked());
        outState.putBoolean("aedCheckbox", aedCheckbox.isChecked());
    }

    private void readFieldsFromDatabase(){
        DocumentReference docRef = database.collection("users").document(currentUser.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(Responder.class);
                initializeViews();
            }
        });
    }

    private void initializeViews(){
        name.getEditText().setText(user.getFirstName());
        surname.getEditText().setText(user.getLastName());
        heavyBleedingCheckbox.setChecked(user.getSkills().get("STOP_HEAVY_BLEEDING"));
        treatShockCheckbox.setChecked(user.getSkills().get("TREATING_SHOCK"));
        cprCheckbox.setChecked(user.getSkills().get("CPR"));
        aedCheckbox.setChecked(user.getSkills().get("AED"));
    }

    private void initializeButton() {
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setFirstName(name.getEditText().getText().toString());
                user.setLastName(surname.getEditText().getText().toString());
                HashMap<String, Boolean> skills = new HashMap<>();
                if (heavyBleedingCheckbox.isChecked()) {
                    skills.put("STOP_HEAVY_BLEEDING", true);
                } else {
                    skills.put("STOP_HEAVY_BLEEDING", false);
                }
                if (treatShockCheckbox.isChecked()) {
                    skills.put("TREATING_SHOCK", true);
                } else {
                    skills.put("TREATING_SHOCK", false);
                }
                if (cprCheckbox.isChecked()) {
                    skills.put("CPR", true);
                } else {
                    skills.put("CPR", false);
                }
                if (aedCheckbox.isChecked()) {
                    skills.put("AED", true);
                } else {
                    skills.put("AED", false);
                }
                user.setSkills(skills);
                database.collection("users").document(currentUser.getUid())
                        .set(user);
                Intent intent = new Intent(getActivity(), DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }
}
